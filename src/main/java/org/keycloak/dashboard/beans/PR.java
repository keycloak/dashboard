package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.rep.Teams;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PR {

    private List<PRStat> stats;
    private List<PRStat> teamStats;
    private final Teams teams;

    public PR(GitHubData data, Teams teams) {
        this.teams = teams;
        List<GitHubIssue> prs = data.getPrs();

        int open = (int) prs.stream().filter(i -> i.isOpen()).count();
        int priority = (int) prs.stream().filter(i -> i.isOpen() && i.hasLabel("priority/important", "priority/blocker")).count();

        int olderThan6Months = (int) prs.stream().filter(i -> i.isOpen() && i.getCreatedAt().before(DateUtil.MINUS_6_MONTHS)).count();
        int olderThan12Months = (int) prs.stream().filter(i -> i.isOpen() && i.getCreatedAt().before(DateUtil.MINUS_12_MONTHS)).count();
        int olderThan18Months = (int) prs.stream().filter(i -> i.isOpen() && i.getCreatedAt().before(DateUtil.MINUS_18_MONTHS)).count();

        int createdLast7Days = (int) prs.stream().filter(i -> i.getCreatedAt().after(DateUtil.MINUS_7_DAYS)).count();
        int closedLast7Days = (int) prs.stream().filter(i -> i.getClosedAt() != null && i.getClosedAt().after(DateUtil.MINUS_7_DAYS)).count();
        int createdLast30Days = (int) prs.stream().filter(i -> i.getCreatedAt().after(DateUtil.MINUS_30_DAYS)).count();
        int closedLast30Days = (int) prs.stream().filter(i -> i.getClosedAt() != null && i.getClosedAt().after(DateUtil.MINUS_30_DAYS)).count();
        int createdLast90Days = (int) prs.stream().filter(i -> i.getCreatedAt().after(DateUtil.MINUS_90_DAYS)).count();
        int closedLast90Days = (int) prs.stream().filter(i -> i.getClosedAt() != null && i.getClosedAt().after(DateUtil.MINUS_90_DAYS)).count();

        stats = new LinkedList<>();

        stats.add(new PRStat("Open", open, Config.PR_OPEN_WARN, Config.PR_OPEN_ERROR, "is:open"));
        stats.add(new PRStat("Priority", priority, Config.PR_PRIORITY_WARN, Config.PR_PRIORITY_ERROR, "is:open label:priority/important,priority/blocker"));

        stats.add(new PRStat("Older than 6 months", olderThan6Months, Config.PR_OLD_6_WARN, Config.PR_OLD_6_ERROR, "is:open created:<=" + DateUtil.MINUS_6_MONTHS_STRING));
        stats.add(new PRStat("Older than 12 months", olderThan12Months, Config.PR_OLD_12_WARN, Config.PR_OLD_12_ERROR, "is:open created:<=" + DateUtil.MINUS_12_MONTHS_STRING));
        stats.add(new PRStat("Older than 18 months", olderThan18Months, Config.PR_OLD_18_WARN, Config.PR_OLD_18_ERROR, "is:open created:<=" + DateUtil.MINUS_18_MONTHS_STRING));

        stats.add(new PRStat("Last 7 days",
                createdLast7Days, 0, createdLast7Days > closedLast7Days ? 1 : 999, "created:>=" + DateUtil.MINUS_7_DAYS_STRING,
                closedLast7Days, 0, createdLast7Days > closedLast7Days ? 1 : 999, "is:closed closed:>=" + DateUtil.MINUS_7_DAYS_STRING));

        stats.add(new PRStat("Last 30 days",
                createdLast30Days, 0, createdLast30Days > closedLast30Days ? 1 : 999, "created:>=" + DateUtil.MINUS_30_DAYS_STRING,
                closedLast30Days, 0, createdLast30Days > closedLast30Days ? 1 : 999, "is:closed closed:>=" + DateUtil.MINUS_30_DAYS_STRING));

        stats.add(new PRStat("Last 90 days",
                createdLast90Days, 0, createdLast90Days > closedLast90Days ? 1 : 999, "created:>=" + DateUtil.MINUS_90_DAYS_STRING,
                closedLast90Days, 0, createdLast90Days > closedLast90Days ? 1 : 999, "is:closed closed:>=" + DateUtil.MINUS_90_DAYS_STRING));

        teamStats = new LinkedList<>();

        for (String team : teams.keySet()) {
            int teamCount = (int) prs.stream().filter(i -> i.hasLabel(team) && i.isOpen()).count();
            int teamOverdueCount = (int) prs.stream().filter(i -> i.hasLabel(team) && i.isOpen() && i.getCreatedAt().before(DateUtil.MINUS_30_DAYS)).count();
            if (teamCount > 0) {
                teamStats.add(new PRStat(team.replace("team/", ""), teamCount, 25, 50, "is:open label:" + team, teamOverdueCount, 1, 5, "is:open label:" + team + " created:<=" + DateUtil.MINUS_30_DAYS_STRING));
            }
        }

        int noTeamStat = (int) prs.stream().filter(i -> i.isOpen() && !i.hasLabel(teams.keySet().toArray(new String[0]))).count();
        int noTeamOverDueStat = (int) prs.stream().filter(i -> i.isOpen() && !i.hasLabel(teams.keySet().toArray(new String[0])) && i.getCreatedAt().before(DateUtil.MINUS_30_DAYS)).count();
        String noTeamQuery = "is:open -label:" + String.join(",", teams.keySet());
        String noTeamQueryOverDue = noTeamQuery + " created:<=" + DateUtil.MINUS_30_DAYS_STRING;
        teamStats.add(new PRStat("No team", noTeamStat, 5, 10, noTeamQuery, noTeamOverDueStat, 1, 5, noTeamQueryOverDue));

    }

    public List<PRStat> getStats() {
        return stats;
    }

    public List<PRStat> getTeamStats() {
        return teamStats;
    }

}
