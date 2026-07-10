package org.keycloak.dashboard.gh;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.vertx.core.Vertx;
import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.rep.PullRequestWait;
import org.keycloak.dashboard.util.DateUtil;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.keycloak.dashboard.Config.EXPIRATION_OLD_ISSUES;

public class WorkflowRuntimeLoader {


    public List<PullRequestWait> update(List<PullRequestWait> currentList) throws Exception {
        currentList = currentList.stream().filter(c -> c.getMergedAt().after(EXPIRATION_OLD_ISSUES)).collect(Collectors.toList());

        Optional<PullRequestWait> mostRecent = currentList.stream().max(Comparator.comparing(PullRequestWait::getMergedAt));
        String fromDate = DateUtil.toString(mostRecent.get().getMergedAt());

        System.out.print("Updating pull request wait times since " + fromDate + ": ");

        List<PullRequestWait> updatedList = load(">=" + fromDate);

        System.out.println(".");

        return merge(currentList, updatedList);
    }

    public List<PullRequestWait> load() throws Exception {
        System.out.print("Loading pull request wait times for last " + Config.MAX_HISTORY_WORKFLOWS + " days: ");

        String fromDate = DateUtil.minusDaysString(Config.MAX_HISTORY_WORKFLOWS);
        return load(">=" + fromDate);
    }

    private List<PullRequestWait> merge(List<PullRequestWait> first, List<PullRequestWait> second) {
        Map<Integer, PullRequestWait> map = new HashMap<>();
        first.stream().forEach(c -> map.put(c.getNumber(), c));
        second.stream().filter(c -> !map.containsKey(c.getNumber())).forEach(c -> map.put(c.getNumber(), c));
        return map.values().stream().toList();
    }

    private List<PullRequestWait> load(String fromDate) throws Exception {
        Vertx vertx = Vertx.vertx();
        DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .vertx(vertx)
                .url("https://api.github.com/graphql")
                .header("Authorization", "bearer " + TokenUtil.token()).build();
        List<PullRequestWait> waitTimes = new LinkedList<>();

        try {
            int first = 100;

            String query = new String(WorkflowRuntimeLoader.class.getResourceAsStream("workflow-runtimes-query").readAllBytes(), StandardCharsets.UTF_8)
                    .replace("$$FROM_DATE$$", fromDate)
                    .replace("$$FIRST$$", Integer.toString(first));

            JsonObject jsonObject = client.executeSync(query).getData();

            Iterator<JsonValue> itr = jsonObject
                    .getJsonObject("search")
                    .getJsonArray("edges")
                    .iterator();

            while (itr.hasNext()) {

                JsonObject pullRequest = itr.next().asJsonObject()
                        .getJsonObject("node")
                        .getJsonObject("commits")
                        .getJsonArray("edges").getJsonObject(0)
                        .getJsonObject("node")
                        .getJsonObject("pullRequest");

                int number = pullRequest.getInt("number");
                String author = pullRequest.getJsonObject("author").getString("login");
                Date mergedAt = DateUtil.fromJson(pullRequest.getString("mergedAt"));
                String baseRef = pullRequest.getString("baseRefName");

                Iterator<JsonValue> checkSuiteItr = pullRequest.getJsonObject("commits")
                        .getJsonArray("edges").getJsonObject(0)
                        .getJsonObject("node")
                        .getJsonObject("commit")
                        .getJsonObject("checkSuites")
                        .getJsonArray("edges").iterator();

                Date startedAt = null;
                Date completedAt = null;

                while (checkSuiteItr.hasNext()) {
                    JsonObject checkSuite = checkSuiteItr.next().asJsonObject()
                            .getJsonObject("node");

                    String workflowName = "";
                    if (!checkSuite.get("workflowRun").getValueType().equals(JsonValue.ValueType.NULL)) {
                        workflowName = checkSuite.getJsonObject("workflowRun").getJsonObject("workflow").getString("name");
                    }

                    if (!workflowName.equals("Labeller")) {
                        Date createdAt = DateUtil.fromJson(checkSuite.getString("createdAt"));
                        Date updatedAt = DateUtil.fromJson(checkSuite.getString("updatedAt"));

                        if (startedAt == null || createdAt.before(startedAt)) {
                            startedAt = createdAt;
                        }

                        if (completedAt == null || updatedAt.after(completedAt)) {
                            completedAt = updatedAt;
                        }
                    }
                }

                if (completedAt != null && startedAt != null) {
                    int minutes = (int) ((completedAt.getTime() - startedAt.getTime()) / (60 * 1000));
                    waitTimes.add(new PullRequestWait(number, author, minutes, mergedAt, completedAt, baseRef));
                }
            }
        } finally {
            client.close();
            vertx.close();
        }

        return waitTimes;
    }

}
