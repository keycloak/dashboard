package org.keycloak.dashboard.beans.filters;

import org.keycloak.dashboard.rep.GitHubIssue;

import java.util.function.Predicate;

class OpenBugFilter implements IssueFilter {

    @Override
    public Predicate<GitHubIssue> predicate() {
        return gitHubIssue -> gitHubIssue.isOpen() && gitHubIssue.isBugOrCve();
    }

    @Override
    public String ghQuery() {
        return "is:open is:issue (label:kind/bug OR label:kind/cve)";
    }

}
