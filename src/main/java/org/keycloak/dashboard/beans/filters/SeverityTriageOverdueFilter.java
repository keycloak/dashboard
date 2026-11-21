package org.keycloak.dashboard.beans.filters;

import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.util.DateUtil;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Matches triage issues that are overdue based on their severity label, each with its own SLA in business days.
 *
 * <p>SLAs (all business days):
 * <ul>
 *   <li>severity/kev — 3 days</li>
 *   <li>severity/critical — 7 days</li>
 *   <li>severity/high — 21 days</li>
 *   <li>severity/medium — 60 days</li>
 *   <li>severity/low — 90 days</li>
 * </ul>
 *
 * <p>Issues without a severity label are treated as KEV (shortest SLA).
 */
class SeverityTriageOverdueFilter implements IssueFilter {

    private final Date kevDeadline;
    private final Date criticalDeadline;
    private final Date highDeadline;
    private final Date mediumDeadline;
    private final Date lowDeadline;

    SeverityTriageOverdueFilter(int kevBusinessDays, int criticalBusinessDays, int highBusinessDays, int mediumBusinessDays, int lowBusinessDays) {
        this.kevDeadline = DateUtil.minusBusinessDays(kevBusinessDays);
        this.criticalDeadline = DateUtil.minusBusinessDays(criticalBusinessDays);
        this.highDeadline = DateUtil.minusBusinessDays(highBusinessDays);
        this.mediumDeadline = DateUtil.minusBusinessDays(mediumBusinessDays);
        this.lowDeadline = DateUtil.minusBusinessDays(lowBusinessDays);
    }

    @Override
    public Predicate<GitHubIssue> predicate() {
        return issue -> {
            Date created = issue.getCreatedAt();
            if (issue.hasLabel("severity/kev") && created.before(kevDeadline)) {
                return true;
            }
            if (issue.hasLabel("severity/critical") && created.before(criticalDeadline)) {
                return true;
            }
            if (issue.hasLabel("severity/high") && created.before(highDeadline)) {
                return true;
            }
            if (issue.hasLabel("severity/medium") && created.before(mediumDeadline)) {
                return true;
            }
            if (issue.hasLabel("severity/low") && created.before(lowDeadline)) {
                return true;
            }
            boolean hasSeverity = issue.hasLabel("severity/kev", "severity/critical", "severity/high", "severity/medium", "severity/low");
            return !hasSeverity && created.before(kevDeadline);
        };
    }

    @Override
    public String ghQuery() {
        return "created:<" + DateUtil.toString(kevDeadline);
    }

}
