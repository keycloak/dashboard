package org.keycloak.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.dashboard.gh.GHRunAttempt;
import org.keycloak.dashboard.gh.GHWorkflowRun;
import org.keycloak.dashboard.gh.GHWorkflowRuns;
import org.keycloak.dashboard.gh.GitHubCli;
import org.keycloak.dashboard.util.DateUtil;
import org.kohsuke.github.GHWorkflowJob;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RetriedPrsLoader {

    private static final Set<String> WORKFLOWS = Set.of("ci.yml", "js-ci.yml", "operator-ci.yml");

    private static final int MAX_DAYS = 14;
    private static final int DAYS = 7;
    private final GitHub gh;
    private final GitHubCli ghCli;

    public RetriedPrsLoader(GitHub gh, GitHubCli ghCli) {
        this.gh = gh;
        this.ghCli = ghCli;
    }

    public void load() throws IOException, InterruptedException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.print("Fetching retried-prs: ");

        File logsDir = new File("logs");
        File cachedRunsFile = new File(logsDir, "retried-prs.json");

        GHWorkflowRuns cachedRuns;
        if (cachedRunsFile.isFile()) {
            cachedRuns = objectMapper.readValue(cachedRunsFile, GHWorkflowRuns.class);
        } else {
            cachedRuns = new GHWorkflowRuns();
            cachedRuns.setWorkflowRuns(new LinkedList<>());
        }

        Date expirationDate = DateUtil.minusdays(MAX_DAYS);
        cachedRuns.setWorkflowRuns(cachedRuns.getWorkflowRuns().stream().filter(r -> DateUtil.fromJson(r.getCreatedAt()).after(expirationDate)).toList());

        String to = DateUtil.toString(new Date());
        Optional<GHWorkflowRun> lastRun = cachedRuns.getWorkflowRuns().stream().max(Comparator.comparing(GHWorkflowRun::getCreatedAt));
        String from = lastRun.isPresent() ? DateUtil.toString(DateUtil.fromJson(lastRun.get().getCreatedAt())) : DateUtil.minusDaysString(DAYS);

        List<GHWorkflowRuns> l = new LinkedList<>();
        l.add(cachedRuns);
        for (String workflow : WORKFLOWS) {
            l.addAll(ghCli.apiGet(GHWorkflowRuns.class, "actions/workflows/" + workflow + "/runs", "--paginate", "-f", "status=success", "-f", "event=pull_request", "-f", "created=" + from + ".." + to, "-f", "per_page=10"));
            System.out.print(".");
        }

        GHWorkflowRuns runs = GHWorkflowRuns.combine(l);

        runs.setWorkflowRuns(runs.getWorkflowRuns().stream().filter(r -> r.getRunAttempt() > 1).toList());
        runs.setTotalCount(runs.getWorkflowRuns().size());

        objectMapper.writeValue(cachedRunsFile, runs);

        List<String> warnings = new ArrayList<>();

        for (GHWorkflowRun r : runs.getWorkflowRuns()) {
            File jobsFile = new File(logsDir, "pr-jobs-" + r.getId());
            File jobsLog = new File(logsDir, "pr-log-" + r.getId());

            if (!jobsFile.isFile()) {
                GHRunAttempt ghRunAttempt = ghCli.apiGet(GHRunAttempt.class, "actions/runs/" + r.getId() + "/attempts/" + (r.getRunAttempt() - 1) + "?exclude_pull_requests=true").get(0);
                PrintStream jobsOutput = new PrintStream(new FileOutputStream(jobsFile));

                String workflow = r.getPath().substring(r.getPath().lastIndexOf('/') + 1);

                jobsOutput.println("# " + workflow + " " + r.getCreatedAt() + " " + r.getEvent() + " " + (r.getRunAttempt() - 1) + " " + ghRunAttempt.getConclusion());

                PagedIterable<GHWorkflowJob> ghWorkflowJobs = gh.getRepository("keycloak/keycloak").getWorkflowRun(r.getId()).listAllJobs();
                System.out.print(".");
                for (GHWorkflowJob j : ghWorkflowJobs.toList()) {
                    if (j.getRunAttempt() == 1) {
                        jobsOutput.println(j.getName() + ": [" + j.getConclusion() + "]");
                    }
                }

                jobsOutput.close();
            }

            if (!jobsLog.isFile()) {
                BufferedReader br = new BufferedReader(new FileReader(jobsFile));
                String jobsFileHeader = br.readLine();
                br.close();

                String conclusion = jobsFileHeader.split(" ")[5];

                if ("failure".equals(conclusion)) {
                    try {
                        ghCli.download(jobsLog, "gh", "run", "view", "-R", "keycloak/keycloak", Long.toString(r.getId()), "--attempt", Integer.toString(r.getRunAttempt() - 1), "--log-failed");
                        System.out.print(".");
                    } catch (IOException e) {
                        warnings.add("Failed to download log for run " + r.getId() + ": " + e.getMessage());
                        jobsLog.delete();
                    }
                }
            }
        }

        System.out.println();
        for (String warning : warnings) {
            System.out.println("[WARN] " + warning);
        }

        System.out.print("Deleting expired retried-prs: ");

        List<String> retriedPrIds = runs.getWorkflowRuns().stream().map(r -> r.getId().toString()).toList();

        for (File f : logsDir.listFiles(f -> f.getName().startsWith("pr-"))) {
            String runId = f.getName().split("-")[2];
            if (!retriedPrIds.contains(runId)) {
                f.delete();
                System.out.print(".");
            }
        }

        System.out.println();
    }

}
