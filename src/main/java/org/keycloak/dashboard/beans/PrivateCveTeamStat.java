package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * CVE statistics per team for keycloak-private with severity-based triage overdue rules.
 */
public class PrivateCveTeamStat {

    private final String team;
    private final FilteredIssues allIssues;
    private final List<BugStat> columns = new LinkedList<>();

    public PrivateCveTeamStat(String team, FilteredIssues allIssues) {
        this.team = team;
        this.allIssues = allIssues;

        int importantBizDays = Config.getInt("private.triage.ImportantOverdue.businessDays");
        int moderateBizDays = Config.getInt("private.triage.ModerateOverdue.businessDays");
        int lowDays = Config.getInt("private.triage.LowOverdue.days");

        FilteredIssues triageBase = allIssues.clone().openIssue().triage(true);

        columns.add(BugStat.team("Triage").warnErrorKey("PrivateTriage")
                .issues(triageBase.clone()));

        columns.add(BugStat.team("Triage Overdue").warnErrorKey("PrivateTriageOverdue")
                .issues(triageBase.clone().ready(false).severityTriageOverdue(importantBizDays, moderateBizDays, lowDays)));

        FilteredIssues cveOpen = allIssues.clone().openCve().triage(false);

        columns.add(BugStat.team("Open").warnErrorKey("CveOpen")
                .issues(cveOpen.clone()));

        columns.add(BugStat.team("Open Overdue").warnErrorKey("CveOpenOverdue")
                .issues(cveOpen.clone().ready(false).createdBefore(
                        DateUtil.minusdays(Config.getInt("bugs.CveOverdue.days")))));

        columns.add(BugStat.team("Blocked External").warnErrorKey("CveBlockedExternal")
                .issues(allIssues.clone().openIssue().blockedExternal(true)));
    }

    public String getTitle() {
        return team.replace("team/", "");
    }

    public List<BugStat> getColumns() {
        return columns;
    }

    public String getTeamGhLink() {
        return allIssues.ghLink();
    }

}
