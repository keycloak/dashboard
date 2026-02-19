package org.keycloak.dashboard.beans.filters;

import org.keycloak.dashboard.rep.GitHubIssue;

import java.util.function.Predicate;

class OpenCveFilter implements IssueFilter {

    @Override
    public Predicate<GitHubIssue> predicate() {
        return gitHubIssue -> gitHubIssue.isOpen() && gitHubIssue.hasLabel("kind/cve");
    }

    @Override
    public String ghQuery() {
        return "is:open is:issue label:kind/cve";
    }

}
