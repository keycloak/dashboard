package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.util.DateUtil;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlakyTest {

    private GitHubIssue issue;

    public FlakyTest(GitHubIssue issue) {
        this.issue = issue;
    }

    public String getTitle() {
        return issue.getTitle().replace("Flaky test: ", "");
    }

    public String getPackage() {
        String packageName = getTitle();
        int lastDot = packageName.lastIndexOf('.');
        return lastDot == -1 ? "" : packageName.substring(0, lastDot);
    }

    public String getTestClass() {
        String testClass = getTitle();
        return testClass.substring(testClass.lastIndexOf('.') + 1).split("#")[0];
    }

    public List<String> getTeams() {
        return issue.getLabels().stream().filter(l -> l.startsWith("team/")).map(l -> l.substring("team/".length())).toList();
    }

    public String getStatus() {
        if (issue.getLabels().contains("status/triage")) {
            return "triage";
        }
        return issue.getLabels().stream().filter(l -> l.startsWith("priority/")).map(l -> l.substring("priority/".length())).collect(Collectors.joining(", "));
    }

    public String getCountClass() {
        int c = getCount();
        if (c < 10) {
            return "blank";
        } else if (c < 50) {
            return "warn";
        } else {
            return "error";
        }
    }

    public String getCreatedAtClass() {
        Date d = getCreatedAt();
        if (d.before(DateUtil.minusdays(60))) {
            return "error";
        } else if (d.before(DateUtil.minusdays(30))) {
            return "warn";
        } else {
            return "blank";
        }
    }

    public String getUpdatedAtClass() {
        Date d = getUpdatedAt();
        if (d.after(DateUtil.minusdays(1))) {
            return "error";
        } else if (d.after(DateUtil.minusdays(7))) {
            return "warn";
        } else {
            return "blank";
        }
    }

    public List<String> getLabels() {
        return issue.getLabels().stream()
                .filter(Predicate.isEqual("area/ci").negate())
                .filter(Predicate.isEqual("kind/bug").negate())
                .filter(Predicate.isEqual("flaky-test").negate())
                .collect(Collectors.toList());
    }

    public String getMilestone() {
        return issue.getMilestone();
    }

    public String getTestMethod() {
        String[] split = getTitle().split("#");
        return split.length > 1 ? split[1] : "";
    }

    public int getCount() {
        return 1 + issue.getCommentsCount();
    }

    public int getNumber() {
        return issue.getNumber();
    }

    public Date getCreatedAt() {
        return issue.createdAt;
    }

    public Date getUpdatedAt() {
        return issue.getUpdatedAt();
    }


}
