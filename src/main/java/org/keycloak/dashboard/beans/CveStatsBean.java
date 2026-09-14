package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.Releases;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class CveStatsBean {

    private final FilteredIssues issues;
    private final String ref;

    public static List<CveStatsBean> createList(GitHubData data) {
        Releases releases = data.getReleases();

        FilteredIssues filteredIssues = FilteredIssues.create(data.issues).label("kind/cve").label("area/dependencies");

        List<CveStatsBean> list = new LinkedList<>();

        list.add(new CveStatsBean(filteredIssues.clone().openIssue(), "main"));

        for (String activeRelease : releases.getActiveReleaseStreams().stream().sorted(Comparator.reverseOrder()).toList()) {
            list.add(new CveStatsBean(filteredIssues.clone().label("backport/" + activeRelease), "Branch: " + activeRelease));
        }

        String nextRelease = releases.getNextRelease();
        String nextReleaseStream = nextRelease.substring(0, nextRelease.lastIndexOf('.'));

        list.add(new CveStatsBean(filteredIssues.clone().label("release/" + nextRelease, "backport/" + nextReleaseStream), "Next release: " + releases.getNextRelease()));

        return list;
    }

    public CveStatsBean(FilteredIssues issues, String ref) {
        this.issues = issues;
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    public String getRefLink() {
        return getLink(null);
    }

    private String getLink(String severity) {
        FilteredIssues filteredIssues = issues.clone();
        if (severity != null) {
            filteredIssues.label("severity/" + severity);
        }
        return filteredIssues.ghLink();
    }

    public int getCriticial() {
        return issues.clone().label("severity/critical").count();
    }

    public String getCriticalClass() {
        return getClass(getCriticial(), 1, 1);
    }

    public String getCriticialLink() {
        return getLink("critical");
    }

    public int getHigh() {
        return issues.clone().label("severity/high").count();
    }

    public String getHighClass() {
        return getClass(getHigh(), 5, 1);
    }

    public String getHighLink() {
        return getLink("high");
    }

    public int getMedium() {
        return issues.clone().label("severity/medium").count();
    }

    public String getMediumClass() {
        return getClass(getMedium(), 10, 5);
    }

    public String getMediumLink() {
        return getLink("medium");
    }

    public int getLow() {
        return issues.clone().label("severity/low").count();
    }

    public String getLowClass() {
        return getClass(getLow(), 20, 10);
    }

    public String getLowLink() {
        return getLink("low");
    }

    private String getClass(int count, int errorCount, int warnCount) {
        if (count >= errorCount) {
            return "error";
        } else if (count >= warnCount) {
            return "warn";
        } else {
            return "";
        }
    }

}
