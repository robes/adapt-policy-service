package edu.isi.policy.entity;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Defines a resource that is staged or in the process of being staged.
 * 
 * @author David Smith
 * 
 */
public class Resource extends AbstractEntity {
    private String host = null;
    private String file = null;

    private int numberOfJobs = 0;

    private Collection<String> jobs;

    private Resource() {
        super();
        jobs = new HashSet<String>();
    }

    /**
     * 
     * @param host
     *            host that contains the file
     * @param file
     *            path to the file on the host
     * @throws URISyntaxException
     */
    public Resource(String host, String file) throws URISyntaxException {
        this();
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host must be specified.");
        }
        if (file == null || file.length() == 0) {
            throw new IllegalArgumentException("File must be specified.");
        }
        this.host = host;
        this.file = file;
    }

    public void setHost(String host) {
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host must be specified.");
        }
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setFile(String file) {
        if (file == null || file.length() == 0) {
            throw new IllegalArgumentException("File must be specified.");
        }
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    /**
     * 
     * 
     * @return number of jobs attached to this resource
     */
    public int getNumberOfJobs() {
        return numberOfJobs;
    }

    /**
     * sets the number of jobs attached to this resource
     * 
     * @param numberOfJobs
     */
    public void setNumberOfJobs(int numberOfJobs) {
        if (numberOfJobs < 0) {
            throw new IllegalArgumentException("Number of jobs must be >= 0");
        }
        this.numberOfJobs = numberOfJobs;
    }

    /**
     * Increments the number of jobs (not needed when using addJob())
     */
    public void incrementNumberOfJobs() {
        this.numberOfJobs++;
    }

    /**
     * Decrements the number of jobs (not needed when using removeJob())
     */
    public void decrementNumberOfJobs() {
        this.numberOfJobs--;
    }

    /**
     * 
     * @return the jobs assigned to this resource
     */
    public Collection<String> getJobs() {
        return jobs;
    }

    /**
     * sets the jobs assigned to this resource, automatically adjusting the
     * number of jobs as well
     * 
     * @param jobs
     */
    public void setJobs(Collection<String> jobs) {
        if (jobs == null) {
            throw new IllegalArgumentException("Jobs must be specified.");
        }
        this.jobs = jobs;
        this.numberOfJobs = jobs.size();
    }

    /**
     * Add job to the resource
     * 
     * @param job
     */
    public void addJob(String job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must be specified.");
        }
        this.jobs.add(job);
        incrementNumberOfJobs();
    }

    /**
     * Removes the job assigned to this resource
     * 
     * @param job
     */
    public void removeJob(String job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must be specified.");
        }
        this.jobs.remove(job);
        decrementNumberOfJobs();
    }

    @Override
    public String toString() {
        return new StringBuffer(host).append(":").append(file).append(", ")
                .append(getProperties().toString()).toString();
    }
}
