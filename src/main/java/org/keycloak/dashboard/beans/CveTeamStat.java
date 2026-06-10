package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

public class CveTeamStat {

    private String team;
    private final FilteredIssues issues;

    private List<BugStat> columns = new LinkedList<>();

    public CveTeamStat(String team, FilteredIssues issues, String nextRelease) {
        this.team = team;
        this.issues = issues;

        columns.add(BugStat.team("Triage")
                .issues(issues.clone().triage(true)));

        columns.add(BugStat.team("Triage Overdue")
                .issues(issues.clone().triage(true).ready(false)
                        .createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));

        columns.add(BugStat.team("Open").warnErrorKey("CveOpen")
                .issues(issues.clone().triage(false)));

        columns.add(BugStat.team("Open Overdue").warnErrorKey("CveOpenOverdue")
                .issues(issues.clone().triage(false).ready(false)
                        .createdBefore(DateUtil.minusdays(Config.getInt("bugs.CveOverdue.days")))));

        columns.add(BugStat.team("Blocked External").warnErrorKey("CveBlockedExternal")
                .issues(issues.clone().blockedExternal(true)));

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
