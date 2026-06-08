package org.keycloak.dashboard.gh;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GitHubCli {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GHWorkflowRuns getWorkflowRuns(String status, String branch, String created) throws IOException, InterruptedException {
        List<GHWorkflowRuns> ghWorkflowRuns = apiGet(GHWorkflowRuns.class, "actions/workflows/ci.yml/runs", "--paginate", "-f", "status=" + status, "-f", "branch=" + branch, "-f", "created=" + created);
        GHWorkflowRuns runs = new GHWorkflowRuns();
        runs.setTotalCount(ghWorkflowRuns.get(0).getTotalCount());
        runs.setWorkflowRuns(new LinkedList<>());
        for (GHWorkflowRuns r : ghWorkflowRuns) {
            runs.getWorkflowRuns().addAll(r.getWorkflowRuns());
        }
        return runs;
    }

    public <T> List<T> apiGet(Class<T> clazz, String endpoint, String... args) throws IOException, InterruptedException {
        List<String> cmdarray = new LinkedList<>();
        cmdarray.add("gh");
        cmdarray.add("api");
        cmdarray.add("-X");
        cmdarray.add("GET");
        cmdarray.add("--cache");
        cmdarray.add("1h");
        cmdarray.add(endpoint.startsWith("repos/") ? endpoint : ("repos/keycloak/keycloak/" + endpoint));
        cmdarray.addAll(Arrays.asList(args));

        File output = new File("ghcli-out.log");

        ProcessBuilder pb = new ProcessBuilder(cmdarray);
        pb.environment().put("GH_DEBUG", "1");
        pb.redirectError(new File("ghcli-err.log"));
        pb.redirectOutput(output);

//        System.out.println(String.join(" ", cmdarray));

        Process process = pb.start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            throw new IOException("Timed out, see ghcli-err.log for details");
        }

        if (process.exitValue() != 0) {
            throw new IOException("Returned with " + process.exitValue() + ", see ghcli-err.log for details");
        }

        // gh api --paginate returns multiple json documents
        JsonParser parser = jsonFactory.createParser(output);
        parser.setCodec(objectMapper);
        List<T> list = new LinkedList<>();

        JsonToken jsonToken = parser.nextToken();
        if (jsonToken == JsonToken.START_ARRAY) {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                list.add(parser.readValueAs(clazz));
            }
        } else {
            while (parser.hasCurrentToken()) {
                list.add(parser.readValueAs(clazz));
                parser.nextToken();
            }
        }
        return list;
    }

    public void download(File output, String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().put("GH_DEBUG", "1");
        pb.redirectError(new File("ghcli-dl-err.log"));
        pb.redirectOutput(output);

        Process process = pb.start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            throw new IOException("Timed out, see ghcli-dl-err.log for details");
        }

        if (process.exitValue() != 0) {
            throw new IOException("Returned with " + process.exitValue() + ", see ghcli-err.log for details");
        }
    }

}
