package org.keycloak.dashboard.rep;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GitHubData {

    public Date updatedDate;

    @JsonProperty
    public List<String> areas;
    @JsonProperty
    public List<GitHubIssue> issues;
    @JsonProperty
    public List<GitHubIssue> prs;
    @JsonProperty
    public List<PullRequestWait> pullRequestWaits;
    @JsonProperty
    public List<String> branches;

    @JsonIgnore
    public List<String> keycloakDevelopers;

    public int issuesWithPr;

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public List<String> getAreas() {
        return areas;
    }

    public void setAreas(List<String> areas) {
        this.areas = areas;
    }

    public List<GitHubIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<GitHubIssue> issues) {
        issues.sort(Comparator.naturalOrder());
        this.issues = issues;
    }

    public List<GitHubIssue> getPrs() {
        return prs;
    }

    public void setPrs(List<GitHubIssue> prs) {
        issues.sort(Comparator.naturalOrder());
        this.prs = prs;
    }

    public int getIssuesWithPr() {
        return issuesWithPr;
    }

    public void setIssuesWithPr(int issuesWithPr) {
        this.issuesWithPr = issuesWithPr;
    }

    public List<PullRequestWait> getPullRequestWaits() {
        return pullRequestWaits;
    }

    public void setPullRequestWaits(List<PullRequestWait> pullRequestWaits) {
        this.pullRequestWaits = pullRequestWaits;
    }

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }
}
