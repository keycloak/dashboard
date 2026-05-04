package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

public class BugTeamStat {

    private String team;
    private final FilteredIssues issues;

    private List<IssueStat> columns = new LinkedList<>();

    public BugTeamStat(String team, FilteredIssues issues, String nextRelease) {
        this.team = team;
        this.issues = issues;

//        columns.add(IssueStat.team(nextRelease)
//                .issues(issues.clone().milestone(nextRelease))
//                .warnErrorKey("Milestone"));

        columns.add(IssueStat.team("Triage")
                .issues(issues.clone().triage(true)));

        columns.add(IssueStat.team("Triage Overdue")
                .issues(issues.clone().triage(true).createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));

        columns.add(IssueStat.team("Blocker")
                .issues(issues.clone().triage(false).priority("blocker")));

        columns.add(IssueStat.team("Blocker Overdue")
                .issues(issues.clone().triage(false).priority("blocker").createdBefore(DateUtil.minusdays(Config.getInt("bugs.BlockerOverdue.days")))));

        columns.add(IssueStat.team("Important")
                .issues(issues.clone().triage(false).priority("important")));

        columns.add(IssueStat.team("Important Overdue")
                .issues(issues.clone().triage(false).priority("important").createdBefore(DateUtil.minusdays(Config.getInt("bugs.ImportantOverdue.days")))));

        columns.add(IssueStat.team("Blocked External")
                .issues(issues.clone().triage(false).priority("blocker", "important").blockedExternal(true)));

        columns.add(IssueStat.team("Normal")
                .issues(issues.clone().triage(false).priority("normal")));

        columns.add(IssueStat.team("Low")
                .issues(issues.clone().triage(false).priority("low")));
    }

    public String getTitle() {
        return team.replace("team/", "");
    }

    public List<IssueStat> getColumns() {
        return columns;
    }

    public String getTeamGhLink() {
        return issues.ghLink();
    }

}
