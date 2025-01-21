package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

public class BugTeamStat {

    private String team;
    private final FilteredIssues issues;

    private List<BugStat> columns = new LinkedList<>();

    public BugTeamStat(String team, FilteredIssues issues, String nextRelease) {
        this.team = team;
        this.issues = issues;

//        columns.add(BugStat.team(nextRelease)
//                .issues(issues.clone().milestone(nextRelease))
//                .warnErrorKey("Milestone"));

        columns.add(BugStat.team("Triage")
                .issues(issues.clone().triage(true)));

        columns.add(BugStat.team("Triage Overdue")
                .issues(issues.clone().triage(true).createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));

        columns.add(BugStat.team("Blocker")
                .issues(issues.clone().triage(false).priority("blocker")));

        columns.add(BugStat.team("Blocker Overdue")
                .issues(issues.clone().triage(false).priority("blocker").createdBefore(DateUtil.minusdays(Config.getInt("bugs.BlockerOverdue.days")))));

        columns.add(BugStat.team("Important")
                .issues(issues.clone().triage(false).priority("important")));

        columns.add(BugStat.team("Important Overdue")
                .issues(issues.clone().triage(false).priority("important").createdBefore(DateUtil.minusdays(Config.getInt("bugs.ImportantOverdue.days")))));

        columns.add(BugStat.team("Blocked External")
                .issues(issues.clone().triage(false).priority("blocker", "important").blockedExternal(true)));

        columns.add(BugStat.team("Normal")
                .issues(issues.clone().triage(false).priority("normal")));

        columns.add(BugStat.team("Low")
                .issues(issues.clone().triage(false).priority("low")));
    }

    public String getTitle() {
        return team.replace("team/", "");
    }

    public List<BugStat> getColumns() {
        return columns;
    }

    public String getTeamGhLink() {
        return issues.ghLink();
    }

}
