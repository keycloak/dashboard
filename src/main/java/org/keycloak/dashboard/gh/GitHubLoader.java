package org.keycloak.dashboard.gh;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.FailedJobsLoader;
import org.keycloak.dashboard.RetriedPrsLoader;
import org.keycloak.dashboard.WorkflowStatusLoader;
import org.keycloak.dashboard.rep.CveStat;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.util.DateUtil;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GitHubLoader {

    private final GitHubCli ghCli;
    private GitHub gitHub;

    private GitHubIssuesLoader issuesLoader;

    private WorkflowRuntimeLoader workflowRuntimeLoader;
    private FailedJobsLoader failedJobsLoader;
    private final RetriedPrsLoader retriedPrsLoader;

    private final WorkflowStatusLoader workflowStatusLoader;

    public GitHubLoader() throws IOException {
        gitHub = GitHubBuilder.fromEnvironment().withJwtToken(TokenUtil.token()).build();
        ghCli = new GitHubCli();
        issuesLoader = new GitHubIssuesLoader(gitHub);
        workflowRuntimeLoader = new WorkflowRuntimeLoader();
        failedJobsLoader = new FailedJobsLoader(gitHub, ghCli);
        retriedPrsLoader = new RetriedPrsLoader(gitHub, ghCli);
        workflowStatusLoader = new WorkflowStatusLoader(ghCli);
    }

    public GitHubData load() throws Exception {
        GitHubData data = new GitHubData();
        data.setAreas(queryAreas());

        // GitHub Action token doesn't have access to list team members, maintained manually in team-members.yml for now
        // data.setKeycloakDevelopers(queryDevTeam());

        data.setIssues(loadIssues());
        data.setPrivateIssues(loadPrivateIssues());
        data.setPrs(loadPRs());
        data.setIssuesWithPr(queryIssuesWithPr());
        data.setPullRequestWaits(workflowRuntimeLoader.load());
        data.setBranches(listBranches());

        data.setUpdatedDate(new Date());

        data.setCveStats(loadCveStats());

        System.out.println("Created data.json");

        failedJobsLoader.load();
        retriedPrsLoader.load();
        workflowStatusLoader.load();

        return data;
    }

    private List<String> listBranches() throws IOException {
        return gitHub.getRepository("keycloak/keycloak").getBranches().values().stream().map(GHBranch::getName).toList();
    }

    public GitHubData update(GitHubData data) throws Exception {
        List<String> update = System.getProperty("update") != null && !System.getProperty("update").equals("all") ? Arrays.stream(System.getProperty("update").split(",")).toList() : null;
        boolean reload = Boolean.parseBoolean(System.getProperty("reload", "false"));

        if (update == null || update.contains("areas")) {
            data.setAreas(queryAreas());
        }

        if (update == null || update.contains("issues")) {
            data.setIssues(reload ? loadIssues() : updateIssues(data.getIssues()));
        }

        if (update == null || update.contains("private-issues")) {
            data.setPrivateIssues(loadPrivateIssues());
        }

        if (update == null || update.contains("prs")) {
            data.setPrs(updatePRs(data.getPrs()));
            data.setIssuesWithPr(queryIssuesWithPr());
        }

        if (update == null || update.contains("prs-wait")) {
            if (data.getPullRequestWaits() == null || data.getPullRequestWaits().isEmpty()) {
                data.setPullRequestWaits(workflowRuntimeLoader.load());
            } else {
                data.setPullRequestWaits(workflowRuntimeLoader.update(data.getPullRequestWaits()));
            }
        }

        if (update == null || update.contains("failed-jobs")) {
            failedJobsLoader.load();
        }

        if (update == null || update.contains("retried-prs")) {
            retriedPrsLoader.load();
        }

        if (update == null || update.contains("workflow-status")) {
            workflowStatusLoader.load();
        }

        if (update == null || update.contains("branches")) {
            data.setBranches(listBranches());
        }

        if (update == null || update.contains("cve-stats")) {
            data.setCveStats(loadCveStats());
        }

        if (update == null) {
            data.setUpdatedDate(new Date());
        }

        return data;
    }

    private List<String> queryAreas() throws IOException {
        System.out.print("Fetching areas: ");
        List<String> areas = new LinkedList<>();
        GHRepository repository = gitHub.getRepository("keycloak/keycloak");
        for (GHLabel l : repository.listLabels()) {
            if (l.getName().startsWith("area/")) {
                areas.add(l.getName());
            }
        }
        System.out.println(".");
        return areas;
    }

    public List<String> queryTeam(String team) throws IOException {
        System.out.print("Fetching " + team + " team members: ");
        List<String> members = gitHub.getOrganization("keycloak").getTeamByName(team).getMembers().stream().map(GHPerson::getLogin).collect(Collectors.toList());
        System.out.println(".");
        return members;
    }

    private List<GitHubIssue> loadIssues() throws IOException {
        List<String> queries = new LinkedList<>();
        queries.add("repo:keycloak/keycloak is:issue is:open label:kind/bug");
        queries.add("repo:keycloak/keycloak is:issue is:open label:kind/cve");
        queries.add("repo:keycloak/keycloak is:issue is:open label:kind/weakness");
        for (String month : DateUtil.monthStrings(Config.MAX_HISTORY)) {
            queries.add("repo:keycloak/keycloak is:issue is:closed closed:" + month);
        }
        return issuesLoader.loadIssues(queries.toArray(new String[0]));
    }

    private List<GitHubIssue> updateIssues(List<GitHubIssue> issues) throws IOException {
        List<GitHubIssue> updateIssues = issuesLoader.updateIssues(issues, "repo:keycloak/keycloak is:issue");
        return updateIssues.stream().filter(i -> i.getLabels().contains("kind/bug") || i.getLabels().contains("kind/cve") || i.getLabels().contains("kind/weakness")).collect(Collectors.toList());
    }

    private List<GitHubIssue> loadPrivateIssues() throws IOException {
        return issuesLoader.loadIssues("repo:keycloak/keycloak-private is:issue is:open")
                .stream()
                .filter(issue -> issue.getLabels().stream().anyMatch(s -> s.startsWith("team/")))
                .map(GitHubLoader::sanitize).sorted().toList();
    }

    public List<CveStat> loadCveStats() throws IOException, InterruptedException {
        System.out.print("Loading cve stats: ");

        List<String> refs = new LinkedList<>();
        refs.add("refs/heads/main");

        gitHub.getRepository("keycloak/keycloak").getBranches().keySet().stream().filter(s -> s.startsWith("release/")).forEach(b -> refs.add("refs/heads/" + b));
        System.out.print(".");

        PagedIterator<GHRelease> releaseIterator = gitHub.getRepository("keycloak/keycloak").listReleases().iterator();
        String release = releaseIterator.next().getName();
        if (release.equals("nightly")) {
            release = releaseIterator.next().getName();
        }
        refs.add("refs/tags/" + release);
        System.out.print(".");

        List<CveStat> cveStats = new LinkedList<>();

        for (String ref : refs) {
            CveStat cveStat = new CveStat(ref);

            List<GhCodeScanning> ghCodeScannings = ghCli.apiGet(GhCodeScanning.class, "repos/keycloak/keycloak/code-scanning/alerts", "-F", "tool_name=Trivy", "-F", "ref=" + ref, "-F", "state=open", "--paginate");

            List<String> scanningIds = ghCodeScannings.stream().map(s -> s.rule().id()).distinct().toList();

            for (String id : scanningIds) {
                GhCodeScanning ghCodeScanning = ghCodeScannings.stream().filter(s -> s.rule().id().equals(id)).findFirst().get();
                cveStat.increase(ghCodeScanning.rule().security_severity_level());
            }

            cveStats.add(cveStat);

            System.out.print(".");
        }

        System.out.println();

        return cveStats;
    }


    private List<GitHubIssue> loadPRs() throws IOException {
        List<String> queries = new LinkedList<>();
        queries.add("repo:keycloak/keycloak is:pr is:open");
        for (String month : DateUtil.monthStrings(Config.MAX_HISTORY)) {
            queries.add("repo:keycloak/keycloak is:pr is:closed closed:" + month);
        }
        return issuesLoader.loadPRs(queries.toArray(new String[0]));
    }

    private List<GitHubIssue> updatePRs(List<GitHubIssue> issues) throws IOException {
        return issuesLoader.updatePRs(issues, "repo:keycloak/keycloak is:pr");
    }

    private int queryIssuesWithPr() throws IOException {
        System.out.print("Fetching bugs with PRs: ");
        int totalCount = gitHub.searchIssues().q("repo:keycloak/keycloak is:issue is:open label:kind/bug linked:pr").list().withPageSize(1).getTotalCount();
        System.out.println(".");
        return totalCount;
    }

    private static GitHubIssue sanitize(GitHubIssue issue) {
        GitHubIssue sanitized = new GitHubIssue();
        sanitized.setNumber(issue.getNumber());
        sanitized.setClosedAt(issue.getClosedAt());
        sanitized.setCreatedAt(issue.getCreatedAt());
        sanitized.setLabels(issue.getLabels().stream().filter(l -> l.startsWith("team/") || l.startsWith("kind/") || l.startsWith("severity/") || l.equals("status/triage") || l.equals("status/blocked-external") || l.equals("status/missing-information") || l.equals("status/ready")).toList());
        sanitized.setCommentsCount(issue.getCommentsCount());
        sanitized.setUpdatedAt(issue.getUpdatedAt());
        return sanitized;
    }

}
