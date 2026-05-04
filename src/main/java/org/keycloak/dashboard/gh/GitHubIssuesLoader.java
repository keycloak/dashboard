package org.keycloak.dashboard.gh;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.util.DateUtil;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHReaction;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GitHubIssuesLoader {

    private GitHub gitHub;

    public GitHubIssuesLoader(GitHub gitHub) {
        this.gitHub = gitHub;
    }

    public List<GitHubIssue> loadIssues(String... query) throws IOException {
        return loadIssues(false, query);
    }

    public List<GitHubIssue> updateIssues(List<GitHubIssue> issues, String query) throws IOException {
        return updateIssues(false, issues, query);
    }

    public List<GitHubIssue> loadPRs(String... query) throws IOException {
        return loadIssues(true, query);
    }

    public List<GitHubIssue> updatePRs(List<GitHubIssue> issues, String query) throws IOException {
        return updateIssues(true, issues, query);
    }
    
    private List<GitHubIssue> loadIssues(boolean pr, String... query) throws IOException {
        List<GitHubIssue> issues = new LinkedList<>();

        System.out.print("Fetching " + (pr ? "prs" : "issues") + ": ");
        for (String q : query) {
            issues.addAll(query(q));
        }
        System.out.println();

        return issues;
    }

    private List<GitHubIssue> updateIssues(boolean pr, List<GitHubIssue> issues, String query) throws IOException {
        java.util.Date mostRecent = issues.stream().map(GitHubIssue::getUpdatedAt).max(java.util.Date::compareTo).get();

        System.out.print("Fetching updated " + (pr ? "prs" : "issues") + " since " + DateUtil.toString(mostRecent) + ": ");
        List<GitHubIssue> updates = query(query + " updated:>=" + DateUtil.toString(mostRecent));
        System.out.println();

        issues = issues.stream()
                .filter(i -> i.getClosedAt() == null || i.getClosedAt().after(Config.EXPIRATION_OLD_ISSUES))
                .filter(i -> updates.stream().filter(j -> j.number == i.number).findFirst().isEmpty())
                .collect(Collectors.toList());
        issues.addAll(updates);

        return issues;
    }

    private List<GitHubIssue> query(String q) throws IOException {
        List<GitHubIssue> issues = new LinkedList<>();

        PagedSearchIterable<GHIssue> list = gitHub.searchIssues().q(q).list();

        int pageSize = 100;
        list.withPageSize(pageSize);

        int fetchCount = 0;
        for (GHIssue i : list) {
            GitHubIssue issue = convert(i);
            issues.add(issue);

            if (fetchCount % pageSize == 0) {
                System.out.print(".");
            }
            fetchCount++;
        }

        return issues;
    }

    private GitHubIssue convert(GHIssue i) throws IOException {
        GitHubIssue issue = new GitHubIssue();
        issue.setTitle(i.getTitle());
        issue.setUserLogin(i.getUser().getLogin());
        issue.setCreatedAt(i.getCreatedAt());
        issue.setUpdatedAt(i.getUpdatedAt());
        issue.setClosedAt(i.getClosedAt());
        issue.setNumber(i.getNumber());
        issue.setCommentsCount(i.getCommentsCount());
        issue.setMilestone(i.getMilestone() != null ? i.getMilestone().getTitle() : null);

        List<String> labels = new LinkedList<>();
        for (GHLabel l : i.getLabels()) {
            labels.add(l.getName());
        }
        issue.setLabels(labels);

        if (i.getClosedAt() == null && labels.contains("kind/enhancement")) {
            try {
                issue.setReactionsCount(i.listReactions().toList().size());
            } catch (Exception e) {
                issue.setReactionsCount(0);
            }
        }

        return issue;
    }

}
