package org.keycloak.dashboard.beans.filters;

import org.keycloak.dashboard.rep.GitHubIssue;

import java.util.function.Predicate;

class OpenIssueFilter implements IssueFilter {

    @Override
    public Predicate<GitHubIssue> predicate() {
        return GitHubIssue::isOpen;
    }

    @Override
    public String ghQuery() {
        return "is:open is:issue";
    }

}
