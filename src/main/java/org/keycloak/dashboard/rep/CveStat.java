package org.keycloak.dashboard.rep;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class CveStat {
    private String ref;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;

    public CveStat() {
    }

    public CveStat(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public int getHighCount() {
        return highCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public int getLowCount() {
        return lowCount;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setCriticalCount(int criticalCount) {
        this.criticalCount = criticalCount;
    }

    @JsonIgnore
    public void increase(String severity) {
        switch (severity) {
            case "critical": criticalCount++; break;
            case "high": highCount++; break;
            case "medium": mediumCount++; break;
            case "low": lowCount++; break;
        }
    }

    public void setHighCount(int highCount) {
        this.highCount = highCount;
    }

    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
    }

    public void setLowCount(int lowCount) {
        this.lowCount = lowCount;
    }


}
