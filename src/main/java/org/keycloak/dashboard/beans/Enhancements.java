package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.rep.Teams;
import org.keycloak.dashboard.util.DateUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Enhancements {

    private final List<GitHubIssue> issues;

    private List<BugStat> stats;
    private List<EnhancementTeamStat> teamStats;
    private List<GitHubIssue> topReacted;

    public Enhancements(GitHubData data, Teams teams) {
        issues = data.getIssues().stream()
                .filter(i -> i.getLabels().contains("kind/enhancement"))
                .collect(Collectors.toList());

        stats = convertToStats(issues, data, teams);
        teamStats = convertToTeamStats(issues, data, teams);
        topReacted = buildTopReacted(issues);
    }

    private List<BugStat> convertToStats(List<GitHubIssue> issues, GitHubData data, Teams teams) {
        List<BugStat> stats = new LinkedList<>();
        FilteredIssues filteredIssues = FilteredIssues.create(issues);

        stats.add(BugStat.global("Open")
                .issues(filteredIssues.clone().openEnhancement().triage(false).missingInformation(false))
                .warnCount(-1).errorCount(-1));

        stats.add(BugStat.global("Triage")
                .issues(filteredIssues.clone().openEnhancement().triage(true).missingInformation(false))
                .warnCount(-1).errorCount(-1));

        stats.add(BugStat.global("Last 7 days")
                .issues(filteredIssues.clone().label("kind/enhancement").createdAfter(DateUtil.MINUS_7_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/enhancement").closedAfter(DateUtil.MINUS_7_DAYS))
                .warnCount(-1).errorCount(-1));

        stats.add(BugStat.global("Last 30 days")
                .issues(filteredIssues.clone().label("kind/enhancement").createdAfter(DateUtil.MINUS_30_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/enhancement").closedAfter(DateUtil.MINUS_30_DAYS))
                .warnCount(-1).errorCount(-1));

        stats.add(BugStat.global("Last 90 days")
                .issues(filteredIssues.clone().label("kind/enhancement").createdAfter(DateUtil.MINUS_90_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/enhancement").closedAfter(DateUtil.MINUS_90_DAYS))
                .warnCount(-1).errorCount(-1));

        stats.add(BugStat.global("Missing Area")
                .issues(filteredIssues.clone().openEnhancement().missingArea(data.getAreas()).missingInformation(false))
                .warnCount(-1).errorCount(1));

        stats.add(BugStat.global("Missing Team")
                .issues(filteredIssues.clone().openEnhancement().missingTeam(teams))
                .warnCount(-1).errorCount(1));

        stats.add(BugStat.global("Missing Information")
                .issues(filteredIssues.clone().openEnhancement().missingInformation(true))
                .warnCount(-1).errorCount(-1));

        return stats;
    }

    private List<EnhancementTeamStat> convertToTeamStats(List<GitHubIssue> issues, GitHubData data, Teams teams) {
        FilteredIssues filteredIssues = FilteredIssues.create(issues).openEnhancement();
        List<EnhancementTeamStat> teamStats = new LinkedList<>();

        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                FilteredIssues teamIssues = filteredIssues.clone().team(team).excludeAssignedToSubTeam(team, teams);
                teamStats.add(new EnhancementTeamStat(team, teamIssues, data.getAreas()));
            }
        }

        teamStats.sort(Comparator.comparing(EnhancementTeamStat::getTitle));

        return teamStats;
    }

    private List<GitHubIssue> buildTopReacted(List<GitHubIssue> issues) {
        return issues.stream()
                .filter(GitHubIssue::isOpen)
                .filter(i -> i.getReactionsCount() > 0)
                .sorted(Comparator.comparingInt(GitHubIssue::getReactionsCount).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<BugStat> getStats() {
        return stats;
    }

    public List<EnhancementTeamStat> getTeamStats() {
        return teamStats;
    }

    public List<GitHubIssue> getTopReacted() {
        return topReacted;
    }
}
