package org.keycloak.dashboard;

import org.keycloak.dashboard.util.DateUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public class Config {

    private static final Properties CONFIG = loadProperties();

    public static final boolean PUBLISH = System.getProperties().containsKey("publish");

    public static final int MAX_HISTORY = 60;
    public static final int MAX_HISTORY_WORKFLOWS = 90;

    public static final Date EXPIRATION_OLD_ISSUES = DateUtil.minusMonths(MAX_HISTORY);

    public static final int PR_OPEN_WARN = 150;
    public static final int PR_OPEN_ERROR = 200;
    public static final int PR_PRIORITY_WARN = 1;
    public static final int PR_PRIORITY_ERROR = 10;

    public static final int PR_OLD_6_WARN = 50;
    public static final int PR_OLD_6_ERROR = 100;
    public static final int PR_OLD_12_WARN = 1;
    public static final int PR_OLD_12_ERROR = 10;
    public static final int PR_OLD_18_WARN = 1;
    public static final int PR_OLD_18_ERROR = 1;
    public static final int PR_WAIT_TIME_MAX_SLOW = 10;
    public static final int PR_WAIT_TIME_SLOW_THRESHOLD = 90;

    public static int getBugsWarn(String title) {
        return getInt("bugs." + title.replaceAll(" ", "") + ".warn");
    }

    public static int getBugsError(String title) {
        return getInt("bugs." + title.replaceAll(" ", "") + ".error");
    }

    public static int getBugsTeamWarn(String title) {
        return getInt("bugs.team." + title.replaceAll(" ", "") + ".warn");
    }

    public static int getBugsTeamError(String title) {
        return getInt("bugs.team." + title.replaceAll(" ", "") + ".error");
    }

    public static int getBugsAreaWarn(String title) {
        return getInt("bugs.area." + title.replaceAll(" ", "") + ".warn");
    }

    public static int getBugsAreaError(String title) {
        return getInt("bugs.area." + title.replaceAll(" ", "") + ".error");
    }

    public static int getEnhancementsWarn(String title) {
        return getInt("enhancements." + title.replaceAll(" ", "") + ".warn");
    }

    public static int getEnhancementsError(String title) {
        return getInt("enhancements." + title.replaceAll(" ", "") + ".error");
    }

    public static int getEnhancementsTeamWarn(String title) {
        return getInt("enhancements.team." + title.replaceAll(" ", "") + ".warn");
    }

    public static int getEnhancementsTeamError(String title) {
        return getInt("enhancements.team." + title.replaceAll(" ", "") + ".error");
    }

    public static int getInt(String key) {
        String value = CONFIG.getProperty(key);
        if (value == null) {
            throw new RuntimeException(key + " not found");
        }
        return Integer.parseInt(value);
    }

    public static String getConfigContents() throws IOException {
        return new String(Config.class.getResourceAsStream("/config.properties").readAllBytes(), StandardCharsets.UTF_8);
    }

    static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(Config.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
