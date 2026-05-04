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
    private final List<String> activeStreams;
    private String nextRelease;

    private List<IssueStat> stats;

    private List<BugAreaStat> areaStats;
    private List<BugTeamStat> teamStats;
    private List<CveTeamStat> teamCveStats;
    private List<PrivateCveTeamStat> privateTeamCveStats;
    private final List<BugTeamBackportStat> teamBackportStats;

    private List<FlakyTest> flakyTests;

    private Map<String, Integer> flakyTestCountsByTeam;

    public Bugs(GitHubData data, Teams teams) {
        issues = data.getIssues().stream().filter(i -> (i.getLabels().contains("kind/bug") || i.getLabels().contains("kind/cve"))).collect(Collectors.toList());

        issues.stream().filter(i -> i.isOpen() && i.getMilestone() != null && i.getMilestone().endsWith(".0.0"))
                .map(i -> i.getMilestone()).sorted().findFirst().ifPresent(s -> nextRelease = s);

        activeStreams = data.getBranches().stream().filter(b -> b.startsWith("release/") || b.equals("main")).map(l -> l.replace("release/", "")).sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        flakyTests = issues.stream()
                .filter(i -> i.hasLabel("flaky-test") && i.isOpen() && i.getTitle().startsWith("Flaky test:")).map(FlakyTest::new)
                .sorted(Comparator.comparing(FlakyTest::getUpdatedAt).reversed())
                .collect(Collectors.toList());

        flakyTestCountsByTeam = convertToTeamCount(flakyTests, teams);

        stats = convertToIssueStat(issues, data, teams);
        areaStats = convertToAreaStats(issues);
        teamStats = convertToTeamStats(issues, teams);
        teamCveStats = convertToTeamCveStats(issues, teams);
        privateTeamCveStats = convertToPrivateTeamCveStats(data.getPrivateIssues(), teams);
        teamBackportStats = convertToTeamBackportStats(issues, teams);
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

    private List<IssueStat> convertToIssueStat(List<GitHubIssue> issues, GitHubData data, Teams teams) {
        List<IssueStat> stats = new LinkedList<>();
        FilteredIssues filteredIssues = FilteredIssues.create(issues);

        stats.add(IssueStat.global("With PR").issues(data.getIssuesWithPr(), "is:open label:kind/bug linked:pr"));

        stats.add(IssueStat.global("Open")
                .issues(filteredIssues.clone().openBug().triage(false).missingInformation(false)));
        stats.add(IssueStat.global("Triage")
                .issues(filteredIssues.clone().openBug().triage(true).missingInformation(false)));
        stats.add(IssueStat.global("Triage Overdue")
                .issues(filteredIssues.clone().openBug().triage(true).missingInformation(false).createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));
        stats.add(IssueStat.global("CVE")
                .issues(filteredIssues.clone().openCve()));
        stats.add(IssueStat.global("Weakness")
                .issues(filteredIssues.clone().openBug().label("area/weakness")));
        stats.add(IssueStat.global("Blocker")
                .issues(filteredIssues.clone().openBug().priority("blocker")));
        stats.add(IssueStat.global("Blocker Overdue")
                .issues(filteredIssues.clone().openBug().priority("blocker").createdBefore(DateUtil.minusdays(Config.getInt("bugs.BlockerOverdue.days")))));
        stats.add(IssueStat.global("Important")
                .issues(filteredIssues.clone().openBug().priority("important")));
        stats.add(IssueStat.global("Important Overdue")
                .issues(filteredIssues.clone().openBug().priority("important").createdBefore(DateUtil.minusdays(Config.getInt("bugs.ImportantOverdue.days")))));
        stats.add(IssueStat.global("Blocked External")
                .issues(filteredIssues.clone().openBug().priority("blocker", "important").blockedExternal(true)));
        stats.add(IssueStat.global("Normal")
                .issues(filteredIssues.clone().openBug().priority("normal")));
        stats.add(IssueStat.global("Low")
                .issues(filteredIssues.clone().openBug().priority("low")));

        stats.add(IssueStat.global("Last 7 days")
                .issues(filteredIssues.clone().label("kind/bug").createdAfter(DateUtil.MINUS_7_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/bug").closedAfter(DateUtil.MINUS_7_DAYS))
                .errorIfClosedLessThanOpened());

        stats.add(IssueStat.global("Last 30 days")
                .issues(filteredIssues.clone().label("kind/bug").createdAfter(DateUtil.MINUS_30_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/bug").closedAfter(DateUtil.MINUS_30_DAYS))
                .errorIfClosedLessThanOpened());

        stats.add(IssueStat.global("Last 90 days")
                .issues(filteredIssues.clone().label("kind/bug").createdAfter(DateUtil.MINUS_90_DAYS))
                .closedIssues(filteredIssues.clone().label("kind/bug").closedAfter(DateUtil.MINUS_90_DAYS))
                .errorIfClosedLessThanOpened());

        issues.stream().filter(i -> i.getMilestone() != null)
                .collect(Collectors.groupingBy(GitHubIssue::getMilestone, Collectors.toList())).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach((e) -> {
                    FilteredIssues openIssues = filteredIssues.clone().openBug().milestone(e.getKey());
                    if (openIssues.count() > 0) {
                        stats.add(IssueStat.global("Milestone: " + e.getKey())
                                .warnErrorKey("Milestone")
                                .issues(openIssues)
                                .closedIssues(filteredIssues.clone().closedBug().milestone(e.getKey())));
                    }
                });

        activeStreams.forEach(l -> {
            FilteredIssues openIssues = filteredIssues.clone().label("backport/" + l);
            stats.add(IssueStat.global("Backport: " + l).warnErrorKey("Backports").issues(openIssues).closedIssues(filteredIssues.clone().label(l)));
        });

        stats.add(IssueStat.global("Missing Area")
                .issues(filteredIssues.clone().openBug().missingArea(data.getAreas()).missingInformation(false)));
        stats.add(IssueStat.global("Missing Priority")
                .issues(filteredIssues.clone().openBug().triage(false).missingPriority().missingInformation(false)));
        stats.add(IssueStat.global("Missing Team")
                .issues(filteredIssues.clone().openBug().missingTeam(teams)));
        stats.add(IssueStat.global("Missing Information")
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

    private List<CveTeamStat> convertToTeamCveStats(List<GitHubIssue> issues, Teams teams) {
        FilteredIssues filteredIssues = FilteredIssues.create(issues).openCve();
        List<CveTeamStat> teamStats = new LinkedList<>();

        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                FilteredIssues teamIssues = filteredIssues.clone().team(team).excludeAssignedToSubTeam(team, teams);
                if (teamIssues.count() > 0) {
                    teamStats.add(new CveTeamStat(team, teamIssues, nextRelease));
                }
            }
        }

        teamStats.sort(Comparator.comparing(CveTeamStat::getTitle));

        return teamStats;
    }

    private List<PrivateCveTeamStat> convertToPrivateTeamCveStats(List<GitHubIssue> privateIssues, Teams teams) {
        if (privateIssues == null || privateIssues.isEmpty()) {
            return Collections.emptyList();
        }

        FilteredIssues filteredIssues = FilteredIssues.createPrivate(privateIssues);
        List<PrivateCveTeamStat> stats = new LinkedList<>();

        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                FilteredIssues teamIssues = filteredIssues.clone().team(team).excludeAssignedToSubTeam(team, teams);
                if (teamIssues.count() > 0) {
                    stats.add(new PrivateCveTeamStat(team, teamIssues));
                }
            }
        }

        stats.sort(Comparator.comparing(PrivateCveTeamStat::getTitle));
        return stats;
    }

    private List<BugTeamBackportStat> convertToTeamBackportStats(List<GitHubIssue> issues, Teams teams) {
        FilteredIssues filteredIssues = FilteredIssues.create(issues);
        List<BugTeamBackportStat> teamStats = new LinkedList<>();

        for (String team : teams.keySet()) {
            if (!team.equals("no-team")) {
                FilteredIssues teamIssues = filteredIssues.clone().team(team).excludeAssignedToSubTeam(team, teams);
                teamStats.add(new BugTeamBackportStat(team, teamIssues, activeStreams));
            }
        }

        teamStats.sort(Comparator.comparing(BugTeamBackportStat::getTitle));

        return teamStats;
    }

    public String getNextRelease() {
        return nextRelease;
    }

    public List<IssueStat> getStats() {
        return stats;
    }

    public List<BugAreaStat> getAreaStats() {
        return areaStats;
    }

    public List<BugTeamStat> getTeamStats() {
        return teamStats;
    }

    public List<CveTeamStat> getTeamCveStats() {
        return teamCveStats;
    }

    public List<PrivateCveTeamStat> getPrivateTeamCveStats() {
        return privateTeamCveStats;
    }

    public List<BugTeamBackportStat> getTeamBackportStats() {
        return teamBackportStats;
    }

    public List<FlakyTest> getFlakyTests() {
        return flakyTests;
    }

    public Map<String, Integer> getFlakyTestCountsByTeam() {
        return flakyTestCountsByTeam;
    }
}
