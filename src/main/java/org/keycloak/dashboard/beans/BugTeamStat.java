package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

public class BugTeamStat {

    private String team;
    private final FilteredIssues issues;
    private final String configNamespace;

    private List<BugStat> columns = new LinkedList<>();

    public BugTeamStat(String team, FilteredIssues issues, String nextRelease) {
        this(team, issues, nextRelease, "bugs");
    }

    protected BugTeamStat(String team, FilteredIssues issues, String nextRelease, String configNamespace) {
        this.team = team;
        this.issues = issues;
        this.configNamespace = configNamespace;

//        columns.add(BugStat.team(nextRelease)
//                .issues(issues.clone().milestone(nextRelease))
//                .warnErrorKey("Milestone"));

        columns.add(column("Triage")
                .issues(issues.clone().triage(true)));

        columns.add(column("Triage Overdue")
                .issues(issues.clone().triage(true).createdBefore(DateUtil.minusdays(Config.getInt("bugs.TriageOverdue.days")))));

        columns.add(column("Blocker")
                .issues(issues.clone().triage(false).priority("blocker")));

        columns.add(column("Blocker Overdue")
                .issues(issues.clone().triage(false).priority("blocker").createdBefore(DateUtil.minusdays(Config.getInt("bugs.BlockerOverdue.days")))));

        columns.add(column("Important")
                .issues(issues.clone().triage(false).priority("important")));

        columns.add(column("Important Overdue")
                .issues(issues.clone().triage(false).priority("important").createdBefore(DateUtil.minusdays(Config.getInt("bugs.ImportantOverdue.days")))));

        columns.add(column("Blocked External")
                .issues(issues.clone().triage(false).priority("blocker", "important").blockedExternal(true)));

        columns.add(column("Normal")
                .issues(issues.clone().triage(false).priority("normal")));

        columns.add(column("Low")
                .issues(issues.clone().triage(false).priority("low")));
    }

    private BugStat column(String label) {
        String key = configNamespace + ".team." + label.replaceAll(" ", "");
        return BugStat.team(label)
                .warnCount(Config.getInt(key + ".warn"))
                .errorCount(Config.getInt(key + ".error"));
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
