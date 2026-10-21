package org.keycloak.dashboard.ci;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class FailedJob {

    private FailedRun failedRun;
    private String name;
    private JobConclusion conclusion;
    static final String NO_ERROR_PREFIX = "No error parsed: ";

    private ResolvedIssue resolvedBy;
    private String failedStep;

    public String getFailedStep() {
        return failedStep;
    }

    public void setFailedStep(String failedStep) {
        this.failedStep = failedStep;
    }

    // Groups equal failures: first failing test or error line without line numbers, else the job and step
    public String getErrorKey() {
        for (String l : errorLog) {
            String line = l.replaceAll("^.*?\\d{4}-\\d{2}-\\d{2}T[\\d:.]+Z\\s*", "").replace("##[error]", "").trim();
            if (line.isEmpty() || line.matches("(Failed|Errors|Failures):.*") || line.startsWith("mvn ")
                    || line.startsWith("The project ") || line.startsWith("Process completed")) {
                continue;
            }
            line = line.replaceAll("(?<=\\w):\\d+(?=\\s|$)", "");
            return line.length() > 200 ? line.substring(0, 200) + "..." : line;
        }
        String fallback = failedGoal != null ? failedGoal.replaceFirst("^Failed to execute goal ", "") : fallbackError;
        if (fallback != null) {
            fallback = fallback.replaceAll("^.*?\\d{4}-\\d{2}-\\d{2}T[\\d:.]+Z\\s*", "").replace("##[error]", "").trim();
            return fallback.length() > 200 ? fallback.substring(0, 200) + "..." : fallback;
        }
        return NO_ERROR_PREFIX + name + (failedStep != null ? " / " + failedStep : "");
    }

    private String fallbackError;

    public void setFallbackError(String fallbackError) {
        this.fallbackError = fallbackError;
    }

    public FailedJob(FailedRun failedRun, String name, JobConclusion conclusion) {
        this.failedRun = failedRun;
        this.name = name;
        this.conclusion = conclusion;
    }

    private String failedGoal;

    private List<String> errorLog = new LinkedList<>();

    public String getName() {
        return name;
    }

    public String getJobName() {
        return name.contains("(") ? name.substring(0, name.indexOf('(') - 1) : name;
    }

    public ResolvedIssue getResolvedBy() {
        if (failedRun.getResolvedBy() != null) {
            return failedRun.getResolvedBy();
        }
        return resolvedBy;
    }

    public void setResolvedBy(ResolvedIssue resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getProfileName() {
        return name.contains("(") ? name.substring(name.indexOf('(') + 1, name.indexOf(')')) : null;
    }

    public String getAnchor() {
        return getJobName().toLowerCase(Locale.ROOT).replaceAll(" ", "-").replaceAll("\\(", "-").replaceAll("\\)", "");
    }

    public JobConclusion getConclusion() {
        return conclusion;
    }

    public String getFailedGoal() {
        return failedGoal;
    }

    public FailedRun getFailedRun() {
        return failedRun;
    }

    public void setFailedGoal(String failedGoal) {
        this.failedGoal = failedGoal;
    }

    public List<String> getErrorLog() {
        return errorLog;
    }

    public void addErrorLog(String errorLog) {
        this.errorLog.add(errorLog);
    }
}
