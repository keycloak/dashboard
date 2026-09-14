package org.keycloak.dashboard.rep;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Releases {

    @JsonProperty
    public List<String> activeReleaseStreams;

    @JsonProperty
    public List<String> recentReleases;

    public List<String> getRecentReleases() {
        return recentReleases;
    }

    public void setRecentReleases(List<String> recentReleases) {
        this.recentReleases = recentReleases;
    }

    public List<String> getActiveReleaseStreams() {
        return activeReleaseStreams;
    }

    public void setActiveReleaseStreams(List<String> activeReleaseStreams) {
        this.activeReleaseStreams = activeReleaseStreams;
    }

    @JsonIgnore
    public String getNextRelease() {
        String[] split = recentReleases.get(0).split("\\.");
        return split[0] + "." + split[1] + "." + (Integer.parseInt(split[2]) + 1);
    }

}
