package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.rep.Teams;
import org.keycloak.dashboard.util.DateUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Bugs {

    private final List<GitHubIssue> issues;
    private String nextRelease;

    private List<BugStat> stats;

    private List<BugAreaStat> areaStats;
    private List<BugTeamStat> teamStats;

    private List<FlakyTest> flakyTests;

    private Map<String, Integer> flakyTestCountsByTeam;

    public Bugs(GitHubData data, Teams teams) {
        issues = data.getIssues().stream().filter(i -> i.getLabels().contains("kind/bug")).collect(Collectors.toList());

        issues.stream().filter(i -> i.isOpen() && i.getMilestone() != null && i.getMilestone().endsWith(".0.0"))
                .map(i -> i.getMilestone()).sorted().findFirst().ifPresent(s -> nextRelease = s);

        flakyTests = issues.stream()
                .filter(i -> i.hasLabel("flaky-test") && i.isOpen() && i.getTitle().startsWith("Flaky test:")).map(FlakyTest::new)
                .sorted(Comparator.comparing(FlakyTest::getUpdatedAt).reversed())
                .collect(Collectors.toList());

        flakyTestCountsByTeam = convertToTeamCount(flakyTests, teams);

        stats = convertToBugStat(issues, data, teams);
        areaStats = convertToAreaStats(issues);
        teamStats = convertToTeamStats(issues, teams);
    }

    private Map<String, Integer> convertToTeamCount(List<FlakyTest> flakyTests, Teams teams) {
        Map<String, Integer> counts = new TreeMap<>();
        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                counts.put(team.substring("team/".length()), 0);
            }
        }
        for (FlakyTest f : flakyTests) {
            for (String t : f.getTeams()) {
                counts.put(t, counts.get(t) + 1);
            }
        }
        return counts;
    }

    private List<BugStat> convertToBugStat(List<GitHubIssue> issues, GitHubData data, Teams teams) {
        List<BugStat> stats = new LinkedList<>();
        FilteredIssues filteredIssues = FilteredIssues.create(issues);

        stats.add(BugStat.global("With PR").issues(data.getIssuesWithPr(), "is:open label:kind/bug linked:pr"));

        stats.add(BugStat.global("Open")
                .issues(filteredIssues.clone().openBug().triage(false).missingInformation(false)));
        stats.add(BugStat.global("Triage")
                .issues(filteredIssues.clone().openBug().triage(true).missingInformation(false)));
        stats.add(BugStat.global("Triage Overdue")
                .issues(filteredIssues.clone().openBug().triage(true).missingInformation(false).createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));
        stats.add(BugStat.global("Weakness")
                .issues(filteredIssues.clone().openBug().label("kind/weakness")));
        stats.add(BugStat.global("Blocker")
                .issues(filteredIssues.clone().openBug().priority("blocker")));
        stats.add(BugStat.global("Blocker Overdue")
                .issues(filteredIssues.clone().openBug().priority("blocker").createdBefore(DateUtil.minusdays(Config.getInt("bugs.BlockerOverdue.days")))));
        stats.add(BugStat.global("Important")
                .issues(filteredIssues.clone().openBug().priority("important")));
        stats.add(BugStat.global("Important Overdue")
                .issues(filteredIssues.clone().openBug().priority("important").createdBefore(DateUtil.minusdays(Config.getInt("bugs.ImportantOverdue.days")))));
        stats.add(BugStat.global("Blocked External")
                .issues(filteredIssues.clone().openBug().priority("blocker", "important").blockedExternal(true)));
        stats.add(BugStat.global("Normal")
                .issues(filteredIssues.clone().openBug().priority("normal")));
        stats.add(BugStat.global("Low")
                .issues(filteredIssues.clone().openBug().priority("low")));

        stats.add(BugStat.global("Last 7 days")
                .issues(filteredIssues.clone().label("kind/bug").createdAfter(DateUtil.MINUS_7_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/bug").closedAfter(DateUtil.MINUS_7_DAYS))
                .errorIfClosedLessThanOpened());

        stats.add(BugStat.global("Last 30 days")
                .issues(filteredIssues.clone().label("kind/bug").createdAfter(DateUtil.MINUS_30_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/bug").closedAfter(DateUtil.MINUS_30_DAYS))
                .errorIfClosedLessThanOpened());

        stats.add(BugStat.global("Last 90 days")
                .issues(filteredIssues.clone().label("kind/bug").createdAfter(DateUtil.MINUS_90_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/bug").closedAfter(DateUtil.MINUS_90_DAYS))
                .errorIfClosedLessThanOpened());

        issues.stream().filter(i -> i.getMilestone() != null)
                .collect(Collectors.groupingBy(GitHubIssue::getMilestone, Collectors.toList())).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach((e) -> {
                    FilteredIssues openIssues = filteredIssues.clone().openBug().milestone(e.getKey());
                    if (openIssues.count() > 0) {
                        stats.add(BugStat.global("Milestone: " + e.getKey())
                                .warnErrorKey("Milestone")
                                .issues(openIssues)
                                .closedIssues(filteredIssues.clone().closedBug().milestone(e.getKey())));
                    }
                });

        stats.add(BugStat.global("Missing Area")
                .issues(filteredIssues.clone().openBug().missingArea(data.getAreas()).missingInformation(false)));
        stats.add(BugStat.global("Missing Priority")
                .issues(filteredIssues.clone().openBug().triage(false).missingPriority().missingInformation(false)));
        stats.add(BugStat.global("Missing Team")
                .issues(filteredIssues.clone().openBug().missingTeam(teams)));
        stats.add(BugStat.global("Missing Information")
                .issues(filteredIssues.clone().openBug().missingInformation(true)));

        return stats;
    }

    private List<BugAreaStat> convertToAreaStats(List<GitHubIssue> issues) {
        FilteredIssues filteredIssues = FilteredIssues.create(issues).openBug();
        List<BugAreaStat> areaStats = new LinkedList<>();
        Set<String> allAreas = issues.stream().map(GitHubIssue::getLabels).flatMap(List::stream).filter(l -> l.startsWith("area/")).collect(Collectors.toSet());
        for (String area : allAreas) {
            FilteredIssues areaIssues = filteredIssues.clone().area(area);
            int openCount = areaIssues.count();
            if (areaIssues.count() > 0) {
                areaStats.add(new BugAreaStat(area, areaIssues, openCount, nextRelease));
            }
        }

        areaStats.sort(Comparator.comparing(BugAreaStat::getOpenCount).reversed());

        return areaStats;
    }

    private List<BugTeamStat> convertToTeamStats(List<GitHubIssue> issues, Teams teams) {
        FilteredIssues filteredIssues = FilteredIssues.create(issues).openBug();
        List<BugTeamStat> teamStats = new LinkedList<>();

        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                FilteredIssues teamIssues = filteredIssues.clone().team(team).excludeAssignedToSubTeam(team, teams);
                teamStats.add(new BugTeamStat(team, teamIssues, nextRelease));
            }
        }

        teamStats.sort(Comparator.comparing(BugTeamStat::getTitle));

        return teamStats;
    }

    public String getNextRelease() {
        return nextRelease;
    }

    public List<BugStat> getStats() {
        return stats;
    }

    public List<BugAreaStat> getAreaStats() {
        return areaStats;
    }

    public List<BugTeamStat> getTeamStats() {
        return teamStats;
    }

    public List<FlakyTest> getFlakyTests() {
        return flakyTests;
    }

    public Map<String, Integer> getFlakyTestCountsByTeam() {
        return flakyTestCountsByTeam;
    }
}
