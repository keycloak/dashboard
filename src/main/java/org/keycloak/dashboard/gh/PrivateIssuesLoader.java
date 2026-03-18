package org.keycloak.dashboard.gh;

import org.keycloak.dashboard.rep.GitHubIssue;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Fetches open issues from keycloak/keycloak-private directly into memory. Never persists data to disk.
 */
public class PrivateIssuesLoader {

    public static List<GitHubIssue> load() {
        String token = TokenUtil.privateToken();
        if (token == null) {
            return Collections.emptyList();
        }

        try {
            GitHubIssuesLoader loader = new GitHubIssuesLoader(new GitHubBuilder().withOAuthToken(token).build());
            return loader.loadIssues(
                    "repo:keycloak/keycloak-private is:issue is:open label:kind/cve",
                    "repo:keycloak/keycloak-private is:issue is:open label:status/triage -label:kind/cve"
            );
        } catch (IOException e) {
            System.err.println("Failed to load private issues: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}
