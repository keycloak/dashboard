package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;

import java.util.LinkedList;
import java.util.List;

public class EnhancementTeamStat {

    private String team;
    private final FilteredIssues issues;

    private List<BugStat> columns = new LinkedList<>();

    public EnhancementTeamStat(String team, FilteredIssues issues, List<String> areas) {
        this.team = team;
        this.issues = issues;

        columns.add(BugStat.team("Open")
                .issues(issues.clone())
                .warnCount(-1).errorCount(-1));

        columns.add(BugStat.team("Triage")
                .issues(issues.clone().triage(true))
                .warnCount(10).errorCount(25));

        columns.add(BugStat.team("Missing Area")
                .issues(issues.clone().missingArea(areas).missingInformation(false))
                .warnCount(-1).errorCount(1));
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
