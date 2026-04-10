package org.keycloak.dashboard.beans.filters;

import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.util.DateUtil;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Matches triage issues that are overdue based on their severity label, each with its own threshold.
 */
class SeverityTriageOverdueFilter implements IssueFilter {

    private final Date importantDeadline;
    private final Date moderateDeadline;
    private final Date lowDeadline;

    SeverityTriageOverdueFilter(int importantBusinessDays, int moderateBusinessDays, int lowCalendarDays) {
        this.importantDeadline = DateUtil.minusBusinessDays(importantBusinessDays);
        this.moderateDeadline = DateUtil.minusBusinessDays(moderateBusinessDays);
        this.lowDeadline = DateUtil.minusdays(lowCalendarDays);
    }

    @Override
    public Predicate<GitHubIssue> predicate() {
        return issue -> {
            Date created = issue.getCreatedAt();
            if (issue.hasLabel("severity/important") && created.before(importantDeadline)) {
                return true;
            }
            if (issue.hasLabel("severity/moderate") && created.before(moderateDeadline)) {
                return true;
            }
            if (issue.hasLabel("severity/low") && created.before(lowDeadline)) {
                return true;
            }
            boolean hasSeverity = issue.hasLabel("severity/important", "severity/moderate", "severity/low");
            return !hasSeverity && created.before(importantDeadline);
        };
    }

    @Override
    public String ghQuery() {
        return "created:<" + DateUtil.toString(importantDeadline);
    }

}
