package org.keycloak.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.dashboard.gh.GitHubLoader;
import org.keycloak.dashboard.rep.GitHubData;

import java.io.File;

public class LoadGitHubData {

    public static void main(String[] args) throws Exception {
        LoadGitHubData data = new LoadGitHubData();
        data.createData();
    }

    public void createData() throws Exception {
        GitHubLoader gitHubLoader = new GitHubLoader();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        File dataFile = new File("data.json");

        GitHubData data;
        if (dataFile.isFile()) {
            data = objectMapper.readValue(dataFile, GitHubData.class);
            data = gitHubLoader.update(data);
        } else {
            data = gitHubLoader.load();
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, data);
    }

}
