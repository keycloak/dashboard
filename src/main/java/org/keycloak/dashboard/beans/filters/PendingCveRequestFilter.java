package org.keycloak.dashboard.beans.filters;

import org.keycloak.dashboard.rep.GitHubIssue;

import java.util.function.Predicate;

/**
 * Excludes issues that have status/cve-request, which are waiting for secalert
 * to assign a CVE ID and should not count against teams.
 */
class PendingCveRequestFilter implements IssueFilter {

    @Override
    public Predicate<GitHubIssue> predicate() {
        return issue -> !issue.hasLabel("status/cve-request");
    }

    @Override
    public String ghQuery() {
        return "-label:status/cve-request";
    }

}
