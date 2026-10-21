package org.keycloak.dashboard.ci;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An error that failed a job on an earlier attempt of a run, where a later attempt of the same run was green.
 */
public class FlakyError {

    private static final int MAX_SAMPLES = 6;

    private final String error;
    private final Map<String, Integer> jobs = new TreeMap<>();
    private final List<FailedJob> samples = new LinkedList<>();
    private int count;
    private Date lastSeen;

    public FlakyError(String error) {
        this.error = error;
    }

    void add(FailedJob job) {
        count++;
        jobs.merge(error.startsWith(FailedJob.NO_ERROR_PREFIX) ? job.getName() : job.getJobName(), 1, Integer::sum);
        Date date = job.getFailedRun().getDate();
        if (lastSeen == null || date.after(lastSeen)) {
            lastSeen = date;
        }
        samples.add(job);
        samples.sort((a, b) -> b.getFailedRun().getDate().compareTo(a.getFailedRun().getDate()));
        while (samples.size() > MAX_SAMPLES) {
            samples.remove(samples.size() - 1);
        }
    }

    public String getError() {
        return error;
    }

    // Matrix variants are collapsed into the base job name with their number of occurrences
    public List<String> getJobs() {
        return jobs.entrySet().stream().map(e -> e.getKey() + (jobs.size() > 1 && e.getValue() > 1 ? " (" + e.getValue() + "x)" : "")).toList();
    }

    public int getCount() {
        return count;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public List<FailedJob> getSamples() {
        return samples;
    }
}
