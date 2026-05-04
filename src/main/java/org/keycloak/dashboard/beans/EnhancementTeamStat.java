package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;

import java.util.LinkedList;
import java.util.List;


public class EnhancementTeamStat {

    private String team;
    private final FilteredIssues issues;

    private List<IssueStat> columns = new LinkedList<>();

    public EnhancementTeamStat(String team, FilteredIssues issues) {
        this.team = team;
        this.issues = issues;

        columns.add(IssueStat.enhancementTeam("Open")
                .issues(issues.clone()));
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
