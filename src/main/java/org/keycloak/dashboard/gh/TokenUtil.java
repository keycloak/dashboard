package org.keycloak.dashboard.gh;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenUtil {

    private static final String[] AUTH_ENV_KEYS = { "GITHUB_TOKEN", "GH_TOKEN", "GITHUB_OAUTH" };
    private static final String PRIVATE_TOKEN_KEY = "GITHUB_PRIVATE_TOKEN";

    public static String token() throws IOException {
        String token = tokenFromEnv();
        if (token != null) {
            return token;
        }
        token = tokenFromGhCli();
        if (token != null) {
            return token;
        }

        throw new RuntimeException("Failed to get token for GitHub APIs");
    }

    public static String privateToken() {
        return System.getenv(PRIVATE_TOKEN_KEY);
    }

    private static String tokenFromEnv() {
        for (String k : AUTH_ENV_KEYS) {
            String v = System.getenv(k);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private static String tokenFromGhCli() throws IOException {
        return new String(Runtime.getRuntime().exec("gh auth token").getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

}
