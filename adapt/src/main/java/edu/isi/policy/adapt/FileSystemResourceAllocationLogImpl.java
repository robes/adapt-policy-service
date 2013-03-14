package edu.isi.policy.adapt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * File System-based database implementation of the Resource Allocation Log.
 * 
 * Assumptions: - multiple clients with separate JVM processes on the same
 * machine will share resource logs - only one client will update a resource
 * allocation entry - multiple clients will be interested in aggregating
 * parameters from other clients' records
 * 
 * @author David Smith
 * 
 */
public class FileSystemResourceAllocationLogImpl implements ResourceAllocationLog {

    private static final Logger LOG = Logger
            .getLogger(ResourceAllocationLog.class);
    public static final String BASE_DIRECTORY_KEY = "resourceAllocationBaseDirectory";
    private static final String RESOURCE_EXT = "rae";
    private final String baseDirectory;
    private final Map<String, ResourceAllocation> allocations;

    /**
     * Constructs a Resource allocation log
     * 
     * @param properties
     *            properties for the resource allocation entries
     */
    public FileSystemResourceAllocationLogImpl(Properties properties) {
        if(properties == null) {
            throw new IllegalArgumentException("Properties must be specified.");
        }
        if(properties.containsKey(BASE_DIRECTORY_KEY)) {
            baseDirectory = properties.getProperty(BASE_DIRECTORY_KEY);
            if(baseDirectory == null || baseDirectory.length() == 0) {
                throw new IllegalArgumentException(
                        BASE_DIRECTORY_KEY + " must be specified.");
            }
        } else {
            throw new IllegalArgumentException(BASE_DIRECTORY_KEY
                    + " must be specified.");
        }
        allocations = new HashMap<String, ResourceAllocation>();
    }

    @Override
    public ResourceAllocation getResourceAllocation(String transferId) {
        return allocations.get(transferId);
    }

    @Override
    public void addResourceAllocation(ResourceAllocation allocation)
            throws ResourceAllocationException {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation must be specified.");
        }
        final File allocationFile = getResourceAllocationFile(allocation);
        if (allocations.containsKey(allocation.getId())
                || allocationFile.exists()) {
            throw new ResourceAllocationException(
                    "Cannot create resource allocation for " + allocationFile
                    + ": record exists.");
        }
        final File parentDir = allocationFile.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new ResourceAllocationException(
                        "Could not create resource allocation for "
                                + allocationFile + ": permission denied.");
            }
        }
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(
                    allocationFile));
            writer.write(allocation.toString());
            writer.close();
        } catch (IOException e) {
            throw new ResourceAllocationException(e);
        }
        allocations.put(allocation.getId(), allocation);
    }

    private File getResourceAllocationFile(ResourceAllocation allocation) {
        final StringBuffer buf = new StringBuffer(baseDirectory);
        buf.append(File.separator);
        buf.append(allocation.getSource().getHost());
        buf.append(File.separator);
        buf.append(allocation.getDestination().getHost());
        buf.append(File.separator);
        buf.append(allocation.getId());
        buf.append("." + RESOURCE_EXT);
        return new File(buf.toString());
    }

    @Override
    public void updateResourceAllocation(ResourceAllocation allocation)
            throws ResourceAllocationException {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation must be specified.");
        }
        final File allocationFile = getResourceAllocationFile(allocation);
        if (!allocations.containsKey(allocation.getId())
                || !allocationFile.exists()) {
            throw new ResourceAllocationException("Could not find record for "
                    + allocationFile);
        }
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(
                    allocationFile));
            writer.write(allocation.toString());
            writer.close();
        } catch (IOException e) {
            throw new ResourceAllocationException(e);
        }
        allocations.put(allocation.getId(), allocation);
    }

    @Override
    public ResourceAllocation removeResourceAllocation(String transferId)
            throws ResourceAllocationException {
        if (transferId == null || transferId.length() == 0) {
            throw new IllegalArgumentException(
                    "Allocation ID must be specified.");
        }
        if (!allocations.containsKey(transferId)) {
            throw new ResourceAllocationException("No record found for "
                    + transferId);
        }
        final ResourceAllocation allocation = allocations.get(transferId);
        final File resourceAllocationFile = getResourceAllocationFile(allocation);
        if (resourceAllocationFile.exists()) {
            if (!resourceAllocationFile.delete()) {
                throw new ResourceAllocationException("Could not remove "
                        + resourceAllocationFile);
            }
        }
        return allocations.remove(transferId);
    }

    @Override
    public int getAggregatedTransferStreams(String sourceHost,
            String destinationHost) {
        if (sourceHost == null || sourceHost.length() == 0) {
            throw new IllegalArgumentException("Source host must be specified.");
        }
        if (destinationHost == null || destinationHost.length() == 0) {
            throw new IllegalArgumentException(
                    "Destination host must be specified.");
        }
        int aggregatedStreams = 0;
        final File hostPairDir = getHostPairDirectory(sourceHost,
                destinationHost);
        if (hostPairDir.exists()) {
            final File[] records = hostPairDir
                    .listFiles(new ResourceAllocationFilter());
            ResourceAllocation allocation = null;
            BufferedReader reader = null;
            for (int i = 0; i < records.length; i++) {
                try {
                    reader = new BufferedReader(new FileReader(records[i]));
                    allocation = ResourceAllocationImpl.fromString(reader
                            .readLine());
                    aggregatedStreams += allocation.getTransferStreams();
                    reader.close();
                } catch (Exception e) {
                    LOG.error("Error reading transfer streams.", e);
                }
            }
        }
        return aggregatedStreams;
    }

    @Override
    public float getAggregatedRate(String sourceHost, String destinationHost) {
        if (sourceHost == null || sourceHost.length() == 0) {
            throw new IllegalArgumentException("Source host must be specified.");
        }
        if (destinationHost == null || destinationHost.length() == 0) {
            throw new IllegalArgumentException(
                    "Destination host must be specified.");
        }

        float aggregatedRate = 0.0f;
        final File hostPairDir = getHostPairDirectory(sourceHost,
                destinationHost);
        if (hostPairDir.exists()) {
            final File[] records = hostPairDir
                    .listFiles(new ResourceAllocationFilter());
            ResourceAllocation allocation = null;
            BufferedReader reader = null;
            for (int i = 0; i < records.length; i++) {
                try {
                    reader = new BufferedReader(new FileReader(records[i]));
                    allocation = ResourceAllocationImpl.fromString(reader
                            .readLine());
                    aggregatedRate += allocation.getRate();
                    reader.close();
                } catch (Exception e) {
                    LOG.error("Error reading transfer rate.", e);
                }
            }
        }
        return aggregatedRate;
    }

    private File getHostPairDirectory(String sourceHost, String destinationHost) {
        StringBuffer buf = new StringBuffer(baseDirectory);
        buf.append(File.separator);
        buf.append(sourceHost);
        buf.append(File.separator);
        buf.append(destinationHost);
        return new File(buf.toString());
    }

    @Override
    public void finalize() {
        close();
    }

    private class ResourceAllocationFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith("." + RESOURCE_EXT);
        }

    }

    @Override
    public int getNumberOfTransfers(String sourceHost, String destinationHost) {
        if (sourceHost == null || sourceHost.length() == 0) {
            throw new IllegalArgumentException("Source host must be specified.");
        }
        if (destinationHost == null || destinationHost.length() == 0) {
            throw new IllegalArgumentException(
                    "Destination host must be specified.");
        }
        int num = 0;
        final File hostPairDir = getHostPairDirectory(sourceHost,
                destinationHost);
        if (hostPairDir.exists()) {
            final String[] records = hostPairDir
                    .list(new ResourceAllocationFilter());
            num = records.length;
        }
        return num;
    }

    @Override
    public void open() throws ResourceAllocationException {
        File dir = new File(baseDirectory);
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                throw new ResourceAllocationException(
                        "Could not create base directory" + baseDirectory);
            }
        }

    }

    @Override
    public void close() {
        // database shouldn't persist
        ResourceAllocation allocation = null;
        File allocationFile = null;
        for (String id : allocations.keySet()) {
            allocation = allocations.get(id);
            allocationFile = getResourceAllocationFile(allocation);
            if (!allocationFile.delete()) {
                LOG.warn("Could not delete file " + allocationFile);
            }
        }
    }
}
