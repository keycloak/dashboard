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

    private List<IssueStat> stats;
    private List<EnhancementTeamStat> teamStats;
    private List<GitHubIssue> topReacted;
    private List<GitHubIssue> missingLabelsIssues;

    public Enhancements(GitHubData data, Teams teams) {
        issues = data.getIssues().stream()
                .filter(i -> i.getLabels().contains("kind/enhancement"))
                .collect(Collectors.toList());

        stats = convertToStats(issues, data, teams);
        teamStats = convertToTeamStats(issues, teams);
        topReacted = buildTopReacted(issues);
        missingLabelsIssues = buildMissingLabelsIssues(issues);
    }

    private List<IssueStat> convertToStats(List<GitHubIssue> issues, GitHubData data, Teams teams) {
        List<IssueStat> stats = new LinkedList<>();
        FilteredIssues filteredIssues = FilteredIssues.create(issues);

        stats.add(IssueStat.enhancement("Open")
                .issues(filteredIssues.clone().openEnhancement()));

        stats.add(IssueStat.enhancement("Missing Labels")
                .issues(filteredIssues.clone().openEnhancement().missingArea(data.getAreas()).missingTeam(teams)));

        stats.add(IssueStat.enhancement("Missing Area")
                .issues(filteredIssues.clone().openEnhancement().missingArea(data.getAreas())));

        stats.add(IssueStat.enhancement("Help Wanted")
                .issues(filteredIssues.clone().openEnhancement().helpWanted(true)));

        stats.add(IssueStat.enhancement("Last 7 days")
                .issues(filteredIssues.clone().label("kind/enhancement").createdAfter(DateUtil.MINUS_7_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/enhancement").closedAfter(DateUtil.MINUS_7_DAYS)));

        stats.add(IssueStat.enhancement("Last 30 days")
                .issues(filteredIssues.clone().label("kind/enhancement").createdAfter(DateUtil.MINUS_30_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/enhancement").closedAfter(DateUtil.MINUS_30_DAYS)));

        stats.add(IssueStat.enhancement("Last 90 days")
                .issues(filteredIssues.clone().label("kind/enhancement").createdAfter(DateUtil.MINUS_90_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/enhancement").closedAfter(DateUtil.MINUS_90_DAYS)));

        return stats;
    }

    private List<EnhancementTeamStat> convertToTeamStats(List<GitHubIssue> issues, Teams teams) {
        FilteredIssues filteredIssues = FilteredIssues.create(issues).openEnhancement();
        List<EnhancementTeamStat> teamStats = new LinkedList<>();

        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                FilteredIssues teamIssues = filteredIssues.clone().team(team).excludeAssignedToSubTeam(team, teams);
                teamStats.add(new EnhancementTeamStat(team, teamIssues));
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

    private List<GitHubIssue> buildMissingLabelsIssues(List<GitHubIssue> issues) {
        return issues.stream()
                .filter(GitHubIssue::isOpen)
                .filter(i -> i.getLabels().stream().noneMatch(l -> l.startsWith("area/") || l.startsWith("team/")))
                .sorted(Comparator.comparingInt(GitHubIssue::getReactionsCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<IssueStat> getStats() {
        return stats;
    }

    public List<EnhancementTeamStat> getTeamStats() {
        return teamStats;
    }

    public List<GitHubIssue> getTopReacted() {
        return topReacted;
    }

    public List<GitHubIssue> getMissingLabelsIssues() {
        return missingLabelsIssues;
    }
}
