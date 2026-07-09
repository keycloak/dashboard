package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;

public class WeaknessTeamStat extends BugTeamStat {

    public WeaknessTeamStat(String team, FilteredIssues issues, String nextRelease) {
        super(team, issues, nextRelease, "weaknesses");
    }

}
