package org.keycloak.dashboard;

import org.keycloak.dashboard.gh.TokenUtil;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHReaction;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.github.PagedSearchIterable;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FindOldIssues {

    public static void main(String[] args) throws IOException {
        GitHub gitHub = GitHubBuilder.fromEnvironment().withJwtToken(TokenUtil.token()).build();
        PagedSearchIterable<GHIssue> issues = gitHub.searchIssues().q("repo:keycloak/keycloak is:issue is:open (label:kind/bug OR label:kind/cve) created:<=2022-01-01").list();
        PagedIterator<GHIssue> itr = issues.iterator();
        while (itr.hasNext()) {
            GHIssue next = itr.next();
            System.out.println(next.getHtmlUrl());
            System.out.println("Title: " + next.getTitle());
            System.out.println("Labels: " + next.getLabels().stream().map(GHLabel::getName).collect(Collectors.joining(", ")));
            System.out.println("Reporter: " + next.getUser().getLogin());
            System.out.println();

            List<GHReaction> reactions = next.listReactions().toList();
            System.out.println("Reactions: (" + reactions.size() + ") " + reactions.stream().map(r -> r.getUser().getLogin() + " (" + r.getContent().getContent() + ")").collect(Collectors.joining(", ")));


            Set<String> users = new HashSet<>();

            if (next.getCommentsCount() >= 1) {
                List<GHIssueComment> comments = next.getComments();
                for (GHIssueComment c : comments) {
                    users.add(c.getUser().getLogin());
                }
            }

            System.out.println("Comments: (" + users.size() + ") " + String.join(", ", users));

            Set<String> all = new LinkedHashSet<>();
            all.add(next.getUser().getLogin());
            all.addAll(users);
            all.addAll(reactions.stream().map(r -> r.getUser().getLogin()).collect(Collectors.toSet()));

            System.out.println("Users: (" + all.size() + ") " + String.join(", ", all));

            System.out.println("-------------------------------------------------------------------------------------");
        }
    }

    private static String trim(String s) {
        s = s.length() > 40 ? s.substring(0, 39) : s;
        return s.trim().replace("\n", " ");
    }

}
