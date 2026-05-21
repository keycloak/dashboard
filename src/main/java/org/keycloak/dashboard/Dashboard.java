package org.keycloak.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freemarker.template.TemplateException;
import org.keycloak.dashboard.beans.*;
import org.keycloak.dashboard.ci.LogFailedParser;
import org.keycloak.dashboard.ci.ResolvedIssues;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.TeamMembers;
import org.keycloak.dashboard.rep.Teams;
import org.keycloak.dashboard.util.FreeMarker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class Dashboard {

    public static void main(String[] args) throws IOException, TemplateException, ParseException {
        Dashboard dashboard = new Dashboard();
        dashboard.createDashboard();
    }

    public void createDashboard() throws IOException, TemplateException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();
        GitHubData data = objectMapper.readValue(new File("data.json"), GitHubData.class);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Teams teams = yamlMapper.readValue(new URL("https://raw.githubusercontent.com/keycloak/keycloak/main/.github/teams.yml"), Teams.class);
        TeamMembers teamMembers = yamlMapper.readValue(new File("team-members.yml"), TeamMembers.class);

        PR pr = new PR(data, teams);
        Bugs bugs = new Bugs(data, teams);

        ResolvedIssues resolvedIssues = ResolvedIssues.load(data);

        LogFailedParser logFailedParser = new LogFailedParser(data, resolvedIssues);
        logFailedParser.parseAll();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("publish", Config.PUBLISH);
        attributes.put("updatedDate", data.getUpdatedDate());
        attributes.put("workflowStatus", new WorkflowStatus());
        attributes.put("prStats", pr.getStats());
        attributes.put("prTeamStats", pr.getTeamStats());
        attributes.put("bugStats", bugs.getStats());
        attributes.put("bugAreaStats", bugs.getAreaStats());
        attributes.put("bugTeamStats", bugs.getTeamStats());
        attributes.put("cveTeamStats", bugs.getTeamCveStats());
        attributes.put("privateCveTeamStats", bugs.getPrivateTeamCveStats());
        attributes.put("bugTeamBackportStats", bugs.getTeamBackportStats());
        attributes.put("failedRuns", logFailedParser.getFailedRuns());
        attributes.put("resolvedRuns", logFailedParser.getResolvedRuns());
        attributes.put("failedJobs", logFailedParser.getUnlinkedFailedJobs());
        attributes.put("linkedFailedJobs", logFailedParser.getLinkedFailedJobs());
        attributes.put("flakyTests", bugs.getFlakyTests());
        attributes.put("flakyTestCountsByTeam", bugs.getFlakyTestCountsByTeam());
        attributes.put("nextRelease", bugs.getNextRelease());
        attributes.put("workflowWaitTimes", new WorkflowWaitTimes(data, teamMembers).getWorkFlowWaitPerMonthList());
        attributes.put("configContents", Config.getConfigContents());
        attributes.put("stars", new Stars());

        File output = new File("docs/index.html");
        FreeMarker freeMarker = new FreeMarker(attributes);
        freeMarker.template("index.ftl", output);
        freeMarker.template("bugs.ftl", new File("docs/bugs.html"));
        freeMarker.template("prs.ftl", new File("docs/prs.html"));
        freeMarker.template("workflows.ftl", new File("docs/workflows.html"));
        freeMarker.template("tests.ftl", new File("docs/tests.html"));
        freeMarker.template("config.ftl", new File("docs/config.html"));
        freeMarker.template("stars.ftl", new File("docs/stars.html"));

        System.out.println("Created dashboard: " + output.toURI());
    }

}
