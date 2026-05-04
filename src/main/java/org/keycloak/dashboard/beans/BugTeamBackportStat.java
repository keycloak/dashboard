package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;

import java.util.LinkedList;
import java.util.List;

public class BugTeamBackportStat {

    private String team;
    private final FilteredIssues issues;

    private List<IssueStat> columns = new LinkedList<>();

    public BugTeamBackportStat(String team, FilteredIssues issues, List<String> activeStreams) {
        this.team = team;
        this.issues = issues;

        for(String stream : activeStreams) {
            columns.add(IssueStat.team(stream).warnErrorKey("Backport").issues(issues.clone().label("backport/" + stream)));
        }
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
