package org.keycloak.dashboard.gh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhCodeScanning(Rule rule) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rule(String id, String severity, String description, String security_severity_level) {}

}
