package org.keycloak.dashboard.ci;

import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.util.DateUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogFailedParser {

    final Pattern TEST_CASE_PATTERN = Pattern.compile("([\\w]*\\.[\\w]*)");

    static final Set<String> IGNORED_JOBS = new HashSet<>();
    static {
        IGNORED_JOBS.add("Set check conclusion");
        IGNORED_JOBS.add("Status Check - Keycloak CI");
    };

    private List<FailedRun> failedRuns = new LinkedList<>();

    private List<FailedRun> resolvedRuns = new LinkedList<>();
    private GitHubData data;
    private ResolvedIssues resolvedIssues;

    public LogFailedParser(GitHubData data, ResolvedIssues resolvedIssues) {
        this.data = data;
        this.resolvedIssues = resolvedIssues;
    }

    public List<FailedJob> getRecentFailedJobs() {
        Date fromDate = DateUtil.MINUS_7_DAYS;
        List<FailedJob> recentFailedJobs = new LinkedList<>();
        for (FailedRun run : failedRuns) {
            if (run.getDate().after(fromDate)) {
                recentFailedJobs.addAll(run.getFailedJobs());
            }
        }
        recentFailedJobs.sort((j1, j2) -> j2.getFailedRun().getDate().compareTo(j1.getFailedRun().getDate()));
        return recentFailedJobs;
    }

    public List<FailedRun> getFailedRuns() {
        return failedRuns;
    }

    public List<FailedRun> getResolvedRuns() {
        return resolvedRuns;
    }

    public Map<String, List<FailedJob>> getFailedJobs() {
        Map<String, List<FailedJob>> failedJobs = new TreeMap<>();
        for (FailedRun run : failedRuns) {
            for (FailedJob job : run.getFailedJobs()) {
                if (!failedJobs.containsKey(job.getJobName())) {
                    failedJobs.put(job.getJobName(), new LinkedList<>());
                }
                failedJobs.get(job.getJobName()).add(job);
            }
        }
        return failedJobs;
    }

    public void parseAll() throws IOException, ParseException {
        List<String> runs = Arrays.stream(new File("logs").listFiles(file -> file.getName().startsWith("jobs-")))
                .map(file -> file.getName().replaceAll("jobs-", ""))
                .collect(Collectors.toList());
        for (String run : runs) {
            parse(run, false);
        }

        runs = Arrays.stream(new File("logs").listFiles(file -> file.getName().startsWith("pr-jobs-")))
                .map(file -> file.getName().replaceAll("pr-jobs-", ""))
                .collect(Collectors.toList());
        for (String run : runs) {
            parse(run, true);
        }

        filterResolved();
    }

    public void filterResolved() {
        Iterator<FailedRun> runItr = failedRuns.iterator();
        while (runItr.hasNext()) {
            FailedRun failedRun = runItr.next();
            failedRun.setResolvedBy(resolvedIssues.getResolved(failedRun));
            if (failedRun.getResolvedBy() != null && failedRun.getResolvedBy().isResolved()) {
                System.out.println("Found resolved run: " + failedRun.getRunId());
                runItr.remove();
                resolvedRuns.add(failedRun);
            } else if (!failedRun.getFailedJobs().isEmpty()) {
                Iterator<FailedJob> jobItr = failedRun.getFailedJobs().iterator();
                List<FailedJob> resolvedJobs = new LinkedList<>();
                while (jobItr.hasNext()) {
                    FailedJob job = jobItr.next();
                    job.setResolvedBy(resolvedIssues.getResolved(job));
                    if (job.getResolvedBy() != null && job.getResolvedBy().isResolved()) {
                        resolvedJobs.add(job);
                        jobItr.remove();
                        System.out.println("Found resolved job: " + failedRun.getRunId() + "/" + job.getName());
                    }
                }

                if (failedRun.getFailedJobs().isEmpty()) {
                    failedRun.setFailedJobs(resolvedJobs);
                    System.out.println("Marking run as fully resolved: " + failedRun.getRunId());
                    resolvedRuns.add(failedRun);
                    runItr.remove();
                }
            }
        }
    }

    public void parse(String runId, boolean pr) throws IOException, ParseException {
        File conclusionFile = new File("logs/" + (pr ? "pr-" : "") + "jobs-" + runId);
        File logFile = new File("logs/" + (pr ? "pr-" : "") + "log-" + runId);

        FailedRun failedRun = new FailedRun(runId);
        failedRuns.add(failedRun);

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(conclusionFile)));
        List<FailedJob> jobs = new LinkedList<>();

        String[] header = br.readLine().substring(2).split(" ");

        String jsonDate = header[0];
        Date date = DateUtil.fromJson(jsonDate);
        failedRun.setDate(date);

        String event = header.length > 1 ? header[1] : null;
        failedRun.setEvent(event);

        String attempt = header.length > 2 ? header[2] : null;
        failedRun.setAttempt(attempt);

        for (String l = br.readLine(); l != null; l = br.readLine()) {
            String[] split = l.split(": ");
            String name = split[0];
            JobConclusion conclusion = JobConclusion.fromLog(split[1]);
            if (conclusion != JobConclusion.SUCCESS && conclusion != JobConclusion.SKIPPED && !IGNORED_JOBS.contains(name)) {
                jobs.add(new FailedJob(failedRun, name, conclusion));
            }
        }

        for (FailedJob job : jobs) {
            if (job.getConclusion() == JobConclusion.FAILURE) {
                if (job.getName().equals("Store Model Tests")) {
                    failedRun.addAll(parseModelTest(failedRun, job, logFile));
                } else {
                    failedRun.add(parseTest(job, logFile));
                }
            } else {
                failedRun.add(job);
            }
        }

        failedRuns.sort(Comparator.comparing(FailedRun::getDate).reversed());
    }

    public void print() {
        for (FailedRun failedRun : failedRuns) {
            System.out.println("==============================================================================================================");
            System.out.println("https://github.com/keycloak/keycloak/actions/runs/" + failedRun.getRunId());

            for (FailedJob job : failedRun.getFailedJobs()) {
                System.out.println("--------------------------------------------------------------------------------------------------------------");
                System.out.println(job.getName() + " - " + job.getConclusion());
                if (!job.getErrorLog().isEmpty()) {
                    System.out.println("--------------------------------------------------------------------------------------------------------------");
                    System.out.println(job.getFailedGoal());
                    System.out.println();
                    for (String l : job.getErrorLog()) {
                        System.out.println(l);
                    }
                }
            }
        }

    }

    private FailedJob parseTest(FailedJob job, File logFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
        List<String> errorLines = new LinkedList<>();
        for (String l = br.readLine(); l != null; l = br.readLine()) {
            if (l.startsWith(job.getName()) && l.contains(" [ERROR] ")) {
                String[] split = l.split("\\[ERROR] ");
                if (split.length == 2) {
                    errorLines.add(split[1]);
                }
            }
        }
        parse(job, errorLines);
        return job;
    }


    private List<FailedJob> parseModelTest(FailedRun failedRun, FailedJob job, File logFile) throws IOException {
        List<FailedJob> failedJobs = new LinkedList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
        List<String> errorLines = null;

        FailedJob failedJob = null;
        for (String l = br.readLine(); l != null; l = br.readLine()) {
            if (l.contains("======== Start of Profile")) {
                String profile = l.split("======== Start of Profile")[1];
                failedJob = new FailedJob(failedRun, job.getName() + " (" + profile.trim() + ")", job.getConclusion());
                errorLines = new LinkedList<>();
            } else if (l.contains("======== End of Profile")) {
                if (!errorLines.isEmpty()) {
                    parse(failedJob, errorLines);
                    failedJobs.add(failedJob);
                    failedJob = null;
                }
            } else if (failedJob != null && l.startsWith(job.getName()) && l.contains(" [ERROR] ")) {
                String[] split = l.split("\\[ERROR] ");
                if (split.length == 2) {
                    errorLines.add(split[1]);
                }
            }
        }

        return failedJobs;
    }

    private void parse(FailedJob job, List<String> errorLines) {
        for (String l : errorLines) {
            if (l.startsWith("Errors:")) {
                job.addErrorLog(l);
            } else if (l.startsWith("Failures:")) {
                job.addErrorLog(l);
            } else if (l.startsWith("  ")) {
                job.addErrorLog(l);
            } else if (l.startsWith("Failed to execute goal")) {
                job.setFailedGoal(l);
            }
        }
    }

}
