package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;

import java.util.LinkedList;
import java.util.List;

public class BugAreaStat {

    private List<IssueStat> columns = new LinkedList<>();

    int openCount;
    String area;
    private final FilteredIssues issues;

    public BugAreaStat(String area, FilteredIssues issues, int openCount, String nextRelease) {
        this.area = area;
        this.issues = issues;
        this.openCount = openCount;

//        columns.add(IssueStat.area(nextRelease)
//                .issues(issues.clone().milestone(nextRelease))
//                .warnErrorKey("Milestone"));

        columns.add(IssueStat.area("Triage")
                .issues(issues.clone().triage(true)));

        columns.add(IssueStat.area("Blocker")
                .issues(issues.clone().triage(false).priority("blocker")));

        columns.add(IssueStat.area("Important")
                .issues(issues.clone().triage(false).priority("important")));

        columns.add(IssueStat.area("Blocked External")
                .issues(issues.clone().triage(false).priority("blocker", "important").blockedExternal(true)));

        columns.add(IssueStat.area("Normal")
                .issues(issues.clone().triage(false).priority("normal")));

        columns.add(IssueStat.area("Low")
                .issues(issues.clone().triage(false).priority("low")));
    }

    public int getOpenCount() {
        return openCount;
    }

    public String getTitle() {
        return area.replaceFirst("area/", "");
    }

    public String getArea() {
        return area;
    }

    public List<IssueStat> getColumns() {
        return columns;
    }

    public String getAreaGhLink() {
        return issues.ghLink();
    }

}
