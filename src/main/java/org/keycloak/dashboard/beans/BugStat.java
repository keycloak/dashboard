package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.beans.filters.FilteredIssues;
import org.keycloak.dashboard.util.Css;
import org.keycloak.dashboard.util.GHQuery;

public class BugStat {

    private String label;
    private final BugStatType type;
    FilteredIssues filteredIssues;
    FilteredIssues closedFilteredIssues;
    private String warnLabel;
    private Integer warnCount;
    private Integer errorCount;
    private int count;
    private String query;

    boolean errorIfClosedLessThanOpened;

    public static BugStat global(String label) {
        return new BugStat(label, BugStatType.GLOBAL);
    }
    public static BugStat team(String label) {
        return new BugStat(label, BugStatType.TEAM);
    }

    public static BugStat area(String label) {
        return new BugStat(label, BugStatType.AREA);
    }

    public BugStat issues(FilteredIssues filteredIssues) {
        this.filteredIssues = filteredIssues;
        return this;
    }

    public BugStat closedIssues(FilteredIssues filteredIssues) {
        this.closedFilteredIssues = filteredIssues;
        return this;
    }

    public BugStat issues(int count, String query) {
        this.count = count;
        this.query = query;
        return this;
    }

    public BugStat warnErrorKey(String warnErrorKey) {
        this.warnLabel = warnErrorKey;
        return this;
    }

    public BugStat errorIfClosedLessThanOpened() {
        this.errorIfClosedLessThanOpened = true;
        return this;
    }

    public BugStat warnCount(int warnCount) {
        this.warnCount = warnCount;
        return this;
    }

    public BugStat errorCount(int errorCount) {
        this.errorCount = errorCount;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public String getGhLink() {
        if (filteredIssues != null) {
            return filteredIssues.ghLink();
        } else {
            return FilteredIssues.ISSUES_LINK + "?q=" + GHQuery.encode(query);
        }
    }
    public String getClosedGhLink() {
        return closedFilteredIssues.ghLink();
    }

    public int getCount() {
        return filteredIssues != null ? filteredIssues.count() : count;
    }

    public Integer getClosedCount() {
        return closedFilteredIssues != null ? closedFilteredIssues.count() : null;
    }

    public String getCssClasses() {
        if (errorIfClosedLessThanOpened) {
            int opened = getCount();
            int closed = getClosedCount();
            return Css.getCountClass(closed < opened ? 1 : 0, -1, 1, false);
        }


        String warnLabel = this.warnLabel != null ? this.warnLabel : label;
        Integer warnCount = this.warnCount;
        Integer errorCount = this.errorCount;

        if (warnCount == null) {
            warnCount = switch (type) {
                case TEAM -> Config.getBugsTeamWarn(warnLabel);
                case AREA -> Config.getBugsAreaWarn(warnLabel);
                case GLOBAL -> Config.getBugsWarn(warnLabel);
            };
        }

        if (errorCount == null) {
            errorCount = switch (type) {
                case TEAM -> Config.getBugsTeamError(warnLabel);
                case AREA -> Config.getBugsAreaError(warnLabel);
                case GLOBAL -> Config.getBugsError(warnLabel);
            };
        }

        return Css.getCountClass(getCount(), warnCount, errorCount, true);
    }

    public String getClosedCssClasses() {
        if (errorIfClosedLessThanOpened) {
            int opened = getCount();
            int closed = getClosedCount();
            return Css.getCountClass(closed < opened ? 1 : 0, -1, 1, true);
        }

        return Css.getCountClass(getClosedCount(), -1, -1, true);
    }

    private BugStat(String label, BugStatType type) {
        this.label = label;
        this.type = type;
    }

    public enum BugStatType {
        GLOBAL,
        TEAM,
        AREA
    }

}
