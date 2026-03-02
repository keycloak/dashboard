package org.keycloak.dashboard.beans;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.dashboard.gh.GHWorkflowRun;
import org.keycloak.dashboard.util.GHQuery;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorkflowStatus {

    private static final List<String> FILTERED_BRANCH_PREFIXES = List.of(
            "copilot/"
    );

    private List<String> branches;

    private List<Workflow> workflows;

    public static void main(String[] args) {
        List<String> l = new LinkedList<>();
        l.add("release/22.0");
        l.add("main");
        l.add("release/24.0");
        l.add("next");
        l.add("quarkus-next");
        l.add("release/23.0");

        l.sort((o1, o2) -> {
            if (o1.equals("main")) {
                return -1000;
            } else if (o2.equals("main")) {
                return 1000;
            }

            if (o1.startsWith("release/")) {
                if (o2.startsWith("release/")) {
                    return o2.compareTo(o1);
                } else {
                    return -1000;
                }
            }

            if (o2.startsWith("release/")) {
                return 1000;
            }

            else return o1.compareTo(o2);
        });

        for (String s : l) {
            System.out.println(s);
        }
    }

    public WorkflowStatus() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GHWorkflowRun> runs = objectMapper.readValue(new File("workflow-status.json"), new TypeReference<>() {});

        branches = runs.stream()
                .map(GHWorkflowRun::getHeadBranch)
                .filter(branch -> FILTERED_BRANCH_PREFIXES.stream().noneMatch(branch::startsWith))
                .distinct()
                .sorted(new BranchComparator())
                .toList();

        workflows = new LinkedList<>();

        for (GHWorkflowRun run : runs) {
            Optional<Workflow> f = workflows.stream().filter(w -> w.getName().equals(run.getName())).findFirst();
            Workflow w;
            if (f.isPresent()) {
                w = f.get();
            } else {
                w = new Workflow(run.getRepository().getFullName(), run.getName(), run.getPath());
                workflows.add(w);
            }

            w.getBranchStatus().put(run.getHeadBranch(), run);
        }

        workflows.sort(Comparator.comparing(Workflow::getName));
    }

    public List<String> getBranches() {
        return branches;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public static final class Workflow {

        private final String repository;
        private final String name;
        private final String path;
        private final Map<String, GHWorkflowRun> branchStatus = new HashMap<>();

        public Workflow(String repository, String name, String path) {
            this.repository = repository;
            this.name = name;
            this.path = path;
        }

        public String getRepository() {
            return repository.substring(repository.lastIndexOf('/') + 1);
        }

        public String getName() {
            return name;
        }

        public String getFile() {
            return path.substring(path.lastIndexOf('/') + 1);
        }

        public Map<String, GHWorkflowRun> getBranchStatus() {
            return branchStatus;
        }

        public String getBranchUrl(String branch) {
            GHWorkflowRun run = branchStatus.get(branch);
            if (run == null) {
                return null;
            }
            return "https://github.com/" + run.getRepository().getFullName() + "/actions/workflows/" + getFile() + "?query=" + GHQuery.encode("branch:" + branch);
        }

        public String getWorkflowUrl() {
            return "https://github.com/" + repository + "/actions/workflows/" + getFile();
        }

    }

    private class BranchComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.equals("main")) {
                return -1000;
            } else if (o2.equals("main")) {
                return 1000;
            }

            if (o1.startsWith("release/")) {
                if (o2.startsWith("release/")) {
                    return o2.compareTo(o1);
                } else {
                    return -1000;
                }
            }

            if (o2.startsWith("release/")) {
                return 1000;
            }

            else return o1.compareTo(o2);
        }
    }

}
