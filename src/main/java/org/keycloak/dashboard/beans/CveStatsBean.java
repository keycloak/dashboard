package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.rep.CveStat;
import org.keycloak.dashboard.util.GHQuery;

public class CveStatsBean {

    private final CveStat cveStat;

    public CveStatsBean(CveStat cveStat) {
        this.cveStat = cveStat;
    }

    public String getRef() {
        return cveStat.getRef();
    }

    public String getRefLink() {
        return getLink(null);
    }

    private String getLink(String severity) {
        String q = "is:open tool:Trivy ref:" + getRef();
        if (severity != null) {
            q += " severity:" + severity;
        }
        return "https://github.com/keycloak/keycloak/security/code-scanning?query=" + GHQuery.encode(q);
    }

    public int getCriticial() {
        return cveStat.getCriticalCount();
    }

    public String getCriticalClass() {
        return getClass(getCriticial(), 1, 1);
    }

    public String getCriticialLink() {
        return getLink("critical");
    }

    public int getHigh() {
        return cveStat.getHighCount();
    }

    public String getHighClass() {
        return getClass(getHigh(), 5, 1);
    }

    public String getHighLink() {
        return getLink("high");
    }

    public int getMedium() {
        return cveStat.getMediumCount();
    }

    public String getMediumClass() {
        return getClass(getMedium(), 10, 5);
    }

    public String getMediumLink() {
        return getLink("medium");
    }

    public int getLow() {
        return cveStat.getLowCount();
    }

    public String getLowClass() {
        return getClass(getLow(), 20, 10);
    }

    public String getLowLink() {
        return getLink("low");
    }

    private String getClass(int count, int errorCount, int warnCount) {
        if (count >= errorCount) {
            return "error";
        } else if (count >= warnCount) {
            return "warn";
        } else {
            return "";
        }
    }

}
