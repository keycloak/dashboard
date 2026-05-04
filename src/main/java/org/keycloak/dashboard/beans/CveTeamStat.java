package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

public class CveTeamStat {

    private String team;
    private final FilteredIssues issues;

    private List<IssueStat> columns = new LinkedList<>();

    public CveTeamStat(String team, FilteredIssues issues, String nextRelease) {
        this.team = team;
        this.issues = issues;

        columns.add(IssueStat.team("Triage")
                .issues(issues.clone().triage(true)));

        columns.add(IssueStat.team("Triage Overdue")
                .issues(issues.clone().triage(true).createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));

        columns.add(IssueStat.team("Open").warnErrorKey("CveOpen")
                .issues(issues.clone().triage(false)));

        columns.add(IssueStat.team("Open Overdue").warnErrorKey("CveOpenOverdue")
                .issues(issues.clone().triage(false).createdBefore(DateUtil.minusdays(Config.getInt("bugs.CveOverdue.days")))));

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
