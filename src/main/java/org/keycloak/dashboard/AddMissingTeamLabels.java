package org.keycloak.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.keycloak.dashboard.gh.TokenUtil;
import org.keycloak.dashboard.rep.Teams;
import org.kohsuke.github.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AddMissingTeamLabels {

    public static void main(String[] args) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Teams teams = yamlMapper.readValue(new URL("https://raw.githubusercontent.com/keycloak/keycloak/main/.github/teams.yml"), Teams.class);

        String teamLabels = teams.keySet().stream().collect(Collectors.joining(","));

        System.out.println(teamLabels);

        GitHub gh = GitHubBuilder.fromEnvironment().withJwtToken(TokenUtil.token()).build();

        PagedSearchIterable<GHIssue> issues = gh.searchIssues().q("repo:keycloak/keycloak is:issue is:open -label:area/docs -label:area/dependencies (label:kind/bug OR label:kind/cve) -label:" + teamLabels).list();
        PagedIterator<GHIssue> itr = issues.iterator();
        for (int i = 0; i < 100 && itr.hasNext(); i++) {
            GHIssue next = issues.iterator().next();
            System.out.println(next.getHtmlUrl());
            System.out.println(next.getLabels().stream().map(GHLabel::getName).collect(Collectors.joining(", ")));

            if (next.getLabels().stream().map(GHLabel::getName).filter(l -> l.startsWith("team/")).findFirst().isEmpty()) {
                List<String> areas = next.getLabels().stream().map(GHLabel::getName).filter(l -> l.startsWith("area/")).collect(Collectors.toList());
                Set<String> addToTeams = new HashSet<>();
                for (String area : areas) {
                    teams.entrySet().stream().filter(e -> e.getValue() != null && e.getValue().contains(area)).forEach(e -> addToTeams.add(e.getKey()));
                }

                System.out.println(addToTeams.stream().collect(Collectors.joining(", ")));

                for (String addToTeam : addToTeams) {
                    next.addLabels(addToTeam);
                }
            } else {
                System.out.println("team label exists");
            }
        }

    }

}
