package org.keycloak.dashboard;

import org.keycloak.dashboard.gh.GHWorkflowRun;
import org.keycloak.dashboard.gh.GHWorkflowRuns;
import org.keycloak.dashboard.gh.GitHubCli;
import org.keycloak.dashboard.util.DateUtil;
import org.kohsuke.github.GHWorkflowJob;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FailedJobsLoader {

    private static final int DAYS = 30;
    private final GitHub gh;
    private final GitHubCli ghCli;

    public FailedJobsLoader(GitHub gh, GitHubCli ghCli) {
        this.gh = gh;
        this.ghCli = ghCli;
    }

    public void load() throws IOException, InterruptedException {
        System.out.print("Fetching failed-jobs: ");

        String from = DateUtil.minusDaysString(DAYS);
        String to = DateUtil.toString(new Date());

        File logsDir = new File("logs");

        List<GHWorkflowRuns> l = new LinkedList<>();
        l.addAll(ghCli.apiGet(GHWorkflowRuns.class, "actions/workflows/ci.yml/runs", "--paginate", "-f", "status=failure", "-f", "branch=main", "-f", "created=" + from + ".." + to));
        l.addAll(ghCli.apiGet(GHWorkflowRuns.class, "actions/workflows/js-ci.yml/runs", "--paginate", "-f", "status=failure", "-f", "branch=main", "-f", "created=" + from + ".." + to));
        l.addAll(ghCli.apiGet(GHWorkflowRuns.class, "actions/workflows/operator-ci.yml/runs", "--paginate", "-f", "status=failure", "-f", "branch=main", "-f", "created=" + from + ".." + to));

        GHWorkflowRuns runs = GHWorkflowRuns.combine(l);

        for (GHWorkflowRun r : runs.getWorkflowRuns()) {
            if (!r.getEvent().equals("pull_request")) {
                File jobsFile = new File(logsDir, "jobs-" + r.getId());
                File jobsLog = new File(logsDir, "log-" + r.getId());
                if (!jobsFile.isFile()) {
                    PrintStream jobsOutput = new PrintStream(new FileOutputStream(jobsFile));

                    String workflow = r.getPath().substring(r.getPath().lastIndexOf('/') + 1);

                    jobsOutput.println("# " + workflow + " " + r.getCreatedAt() + " " + r.getEvent() + " " + r.getRunAttempt() + " " + r.getConclusion());

                    PagedIterable<GHWorkflowJob> ghWorkflowJobs = gh.getRepository("keycloak/keycloak").getWorkflowRun(r.getId()).listAllJobs();

                    for (GHWorkflowJob j : ghWorkflowJobs.toList()) {
                        if (j.getRunAttempt() == 1) {
                            jobsOutput.println(j.getName() + ": [" + j.getConclusion() + "]");
                        }
                    }

                    ghCli.download(jobsLog, "gh", "run", "view", "-R", "keycloak/keycloak", Long.toString(r.getId()), "--log-failed");

                    System.out.print(".");
                }
            }
        }

        System.out.println();

        System.out.print("Deleting expired failed-jobs: ");

        Set<String> runIds = runs.getWorkflowRuns().stream().map(r -> r.getId().toString()).collect(Collectors.toSet());

        File[] jobFiles = logsDir.listFiles((dir, name) -> name.startsWith("jobs-"));
        for (File jobFile : jobFiles) {
            String runId = jobFile.getName().split("-")[1];
            if (!runIds.contains(runId)) {
                jobFile.delete();
                System.out.print(".");
            }
        }

        File[] logFiles = logsDir.listFiles((dir, name) -> name.startsWith("logs-"));
        for (File logFile : logFiles) {
            String runId = logFile.getName().split("-")[1];
            if (!runIds.contains(runId)) {
                logFile.delete();
                System.out.print(".");
            }
        }

        System.out.println();
    }

}
