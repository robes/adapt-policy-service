package edu.isi.policy.adapt;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

/**
 * SQL implementation of the Resource Allocation Log
 * 
 * @author David Smith
 * 
 */
public class SQLResourceAllocationLogImpl implements ResourceAllocationLog {

    private static final Logger LOG = Logger
            .getLogger(SQLResourceAllocationLogImpl.class);

    public static final String DATABASE_DRIVER_KEY = "resourceAllocationDatabaseDriver";
    public static final String DATABASE_URL_KEY = "resourceAllocationDatabaseUrl";
    public static final String DATABASE_USER_KEY = "resourceAllocationDatabaseUser";
    public static final String DATABASE_PASSWORD_KEY = "resourceAllocationDatabasePassword";

    private final String databaseDriver;

    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;

    private Connection conn = null;
    private Statement stmt = null;
    private PreparedStatement insertHostname_pstmt;
    private PreparedStatement selectHostname_pstmt;
    private PreparedStatement insertResourceAllocation_pstmt;
    private PreparedStatement selectResourceAllocation_pstmt;
    private PreparedStatement updateResourceAllocation_pstmt;
    private PreparedStatement removeResourceAllocation_pstmt;
    private PreparedStatement selectAggregatedTransferStreams_pstmt;
    private PreparedStatement selectAggregatedRate_pstmt;
    private PreparedStatement selectNumberOfTransfers_pstmt;

    private final Collection<String> resourceAllocations;
    private boolean isOpen = false;

    /**
     * Default constructor.
     * 
     * @param properties
     *            properties used to create the SQL connection
     */
    public SQLResourceAllocationLogImpl(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties must be specified.");
        }
        synchronized (this) {
            if (properties.containsKey(DATABASE_DRIVER_KEY)) {
                databaseDriver = properties.getProperty(DATABASE_DRIVER_KEY);
                if (databaseDriver == null || databaseDriver.length() == 0) {
                    throw new IllegalArgumentException(DATABASE_DRIVER_KEY
                            + " must be specified.");
                }
            } else {
                throw new IllegalArgumentException("Properties must contain "
                        + DATABASE_DRIVER_KEY);
            }
            if (properties.containsKey(DATABASE_URL_KEY)) {
                databaseUrl = properties.getProperty(DATABASE_URL_KEY);
                if (databaseUrl == null || databaseUrl.length() == 0) {
                    throw new IllegalArgumentException(DATABASE_URL_KEY
                            + " must be specified.");
                }
            } else {
                throw new IllegalArgumentException("Properties must contain "
                        + DATABASE_URL_KEY);
            }
            if (properties.containsKey(DATABASE_USER_KEY)) {
                databaseUser = properties.getProperty(DATABASE_USER_KEY);
            } else {
                throw new IllegalArgumentException("Properties must contain "
                        + DATABASE_USER_KEY);
            }
            if (properties.containsKey(DATABASE_PASSWORD_KEY)) {
                databasePassword = properties.getProperty(DATABASE_PASSWORD_KEY);
            } else {
                throw new IllegalArgumentException("Properties must contain "
                        + DATABASE_PASSWORD_KEY);
            }
            resourceAllocations = new ConcurrentLinkedQueue<String>();
        }
    }

    @Override
    public void open() throws ResourceAllocationException {
        synchronized (this) {
            if (!isOpen) {
                try {
                    connect();
                    createTables();
                    prepareStatements();
                } catch (Exception e) {
                    LOG.error("Error opening resource allocation log.",
                            e);
                    throw new ResourceAllocationException(e);
                }
                isOpen = true;
            }
        }
    }

    private void connect() throws SQLException, ClassNotFoundException {
        Class.forName(databaseDriver);
        conn = DriverManager.getConnection(databaseUrl, databaseUser,
                databasePassword);
        stmt = conn.createStatement();
    }

    private void prepareStatements() throws SQLException {
        selectHostname_pstmt = conn
                .prepareStatement("SELECT id FROM public.host WHERE hostname=?");
        insertHostname_pstmt = conn.prepareStatement(
                "INSERT INTO host(hostname) VALUES(?)");
        insertResourceAllocation_pstmt = conn
                .prepareStatement("INSERT INTO resource_allocation "
                        + "(resource_id, source_uri, source_host_id, destination_uri, "
                        + "destination_host_id, transfer_streams, rate) VALUES (?, ?, ?, ?, ?, ?, ?)");
        selectResourceAllocation_pstmt = conn
                .prepareStatement("SELECT resource_id, source_uri, destination_uri, transfer_streams, rate "
                        + "FROM resource_allocation WHERE resource_id=?");
        updateResourceAllocation_pstmt = conn
                .prepareStatement("UPDATE resource_allocation "
                        + "SET transfer_streams=?, rate=? WHERE resource_id=?");
        removeResourceAllocation_pstmt = conn
                .prepareStatement("DELETE FROM resource_allocation WHERE resource_id=?");
        selectAggregatedTransferStreams_pstmt = conn
                .prepareStatement("SELECT IFNULL(SUM(r.transfer_streams),0) aggregated_streams FROM resource_allocation r "
                        + "INNER JOIN host h1 ON (r.source_host_id=h1.id) INNER JOIN host h2 ON (r.destination_host_id=h2.id) "
                        + "WHERE h1.hostname=? AND h2.hostname=?");
        selectAggregatedRate_pstmt = conn
                .prepareStatement("SELECT IFNULL(SUM(r.rate),0) aggregated_rate FROM resource_allocation r "
                        + "INNER JOIN host h1 ON (r.source_host_id=h1.id) INNER JOIN host h2 ON (r.destination_host_id=h2.id) "
                        + "WHERE h1.hostname=? AND h2.hostname=?");
        selectNumberOfTransfers_pstmt = conn
                .prepareStatement("SELECT COUNT(*) number_transfers FROM resource_allocation r "
                        + "INNER JOIN host h1 ON (r.source_host_id=h1.id) INNER JOIN host h2 ON (r.destination_host_id=h2.id) "
                        + "WHERE h1.hostname=? AND h2.hostname=?");
    }

    private void createTables() throws SQLException {
        stmt.executeUpdate("CREATE MEMORY TABLE IF NOT EXISTS host ("
                + "id INT NOT NULL PRIMARY KEY IDENTITY,"
                + "hostname VARCHAR(255) NOT NULL UNIQUE" + ")");
        stmt.executeUpdate("CREATE MEMORY TABLE IF NOT EXISTS resource_allocation ("
                + "id INTEGER NOT NULL PRIMARY KEY IDENTITY,"
                + "resource_id VARCHAR(255) NOT NULL UNIQUE,"
                + "source_uri VARCHAR(255) NOT NULL,"
                + "source_host_id INTEGER NOT NULL,"
                + "destination_uri VARCHAR(255) NOT NULL,"
                + "destination_host_id INTEGER NOT NULL,"
                + "transfer_streams INTEGER," + "rate DOUBLE" + ")");
    }

    @Override
    public ResourceAllocation getResourceAllocation(String transferId)
            throws ResourceAllocationException {
        if (transferId == null || transferId.length() == 0) {
            throw new IllegalArgumentException(
                    "Allocation ID must be specified.");
        }
        ResourceAllocation allocation = null;
        try {
            ResultSet rs = null;
            synchronized (selectResourceAllocation_pstmt) {
                selectResourceAllocation_pstmt.clearParameters();
                selectResourceAllocation_pstmt.setString(1, transferId);
                rs = selectResourceAllocation_pstmt.executeQuery();
            }
            if (rs.next()) {
                allocation = new ResourceAllocationImpl(rs.getString(1),
                        new URI(rs.getString(2)), new URI(rs.getString(3)),
                        rs.getInt(4), rs.getFloat(5));
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Error retrieving resource allocation " + transferId
                    + ".", e);
            throw new ResourceAllocationException(e);
        } catch (URISyntaxException e) {
            LOG.error("Error retrieving resource allocation: bad URI format.",
                    e);
        }
        return allocation;
    }

    @Override
    public void addResourceAllocation(ResourceAllocation allocation)
            throws ResourceAllocationException {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation must be specified.");
        }
        int sourceHostId = getOrCreateHostname(allocation.getSource().getHost());
        int destinationHostId = getOrCreateHostname(allocation.getDestination()
                .getHost());
        try {
            synchronized (insertResourceAllocation_pstmt) {
                insertResourceAllocation_pstmt.clearParameters();
                insertResourceAllocation_pstmt.setString(1, allocation.getId());
                insertResourceAllocation_pstmt.setString(2, allocation
                        .getSource().toString());
                insertResourceAllocation_pstmt.setInt(3, sourceHostId);
                insertResourceAllocation_pstmt.setString(4, allocation
                        .getDestination().toString());
                insertResourceAllocation_pstmt.setInt(5, destinationHostId);
                insertResourceAllocation_pstmt.setInt(6,
                        allocation.getTransferStreams());
                insertResourceAllocation_pstmt
                .setFloat(7, allocation.getRate());
                insertResourceAllocation_pstmt.executeUpdate();
            }
            resourceAllocations.add(allocation.getId());
        } catch (SQLException e) {
            LOG.error("Error adding resource allocation.", e);
            throw new ResourceAllocationException(e);
        }
    }

    private int getOrCreateHostname(String hostname)
            throws ResourceAllocationException {
        if (hostname == null || hostname.length() == 0) {
            throw new IllegalArgumentException("Hostname must be specified.");
        }
        int hostId = -1;
        try {
            ResultSet rs = null;
            synchronized (selectHostname_pstmt) {
                selectHostname_pstmt.clearParameters();
                selectHostname_pstmt.setString(1, hostname);
                rs = selectHostname_pstmt.executeQuery();
            }
            if (rs.next()) {
                hostId = rs.getInt(1);
                LOG.debug("Retrieved sourceHostId=" + hostId
                        + " persisted in memory for host " + hostname);
            } else {
                synchronized (insertHostname_pstmt) {
                    insertHostname_pstmt.clearParameters();
                    insertHostname_pstmt.setString(1, hostname);
                    insertHostname_pstmt.executeUpdate();
                }
                rs = stmt.executeQuery("CALL IDENTITY()");
                if (rs.next()) {
                    hostId = rs.getInt(1);
                }
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Error adding resource allocation entry.", e);
            throw new ResourceAllocationException(e);
        }
        return hostId;
    }

    @Override
    public void updateResourceAllocation(ResourceAllocation allocation)
            throws ResourceAllocationException {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation must be specified.");
        }
        try {
            synchronized (updateResourceAllocation_pstmt) {
                updateResourceAllocation_pstmt.clearParameters();
                updateResourceAllocation_pstmt.setInt(1,
                        allocation.getTransferStreams());
                updateResourceAllocation_pstmt
                .setFloat(2, allocation.getRate());
                updateResourceAllocation_pstmt.setString(3, allocation.getId());
                updateResourceAllocation_pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOG.error(
                    "Error updating resource allocation " + allocation.getId(),
                    e);
            throw new ResourceAllocationException(e);
        }
    }

    @Override
    public ResourceAllocation removeResourceAllocation(String transferId)
            throws ResourceAllocationException {
        if (transferId == null || transferId.length() == 0) {
            throw new IllegalArgumentException(
                    "Allocation ID must be specified.");
        }
        final ResourceAllocation allocation = getResourceAllocation(transferId);
        if (allocation != null) {
            try {
                synchronized (removeResourceAllocation_pstmt) {
                    removeResourceAllocation_pstmt.clearParameters();
                    removeResourceAllocation_pstmt.setString(1, transferId);
                    removeResourceAllocation_pstmt.executeUpdate();
                }
                resourceAllocations.remove(transferId);
            } catch (SQLException e) {
                LOG.error("Error removing resource allocation " + transferId, e);
            }
        }
        return null;
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
        try {
            ResultSet rs = null;
            synchronized (selectAggregatedTransferStreams_pstmt) {
                selectAggregatedTransferStreams_pstmt.clearParameters();
                selectAggregatedTransferStreams_pstmt.setString(1, sourceHost);
                selectAggregatedTransferStreams_pstmt.setString(2,
                        destinationHost);
                rs = selectAggregatedTransferStreams_pstmt.executeQuery();
            }
            if (rs.next()) {
                aggregatedStreams = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Error retrieving aggregated transfer streams.", e);
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
        try {
            ResultSet rs = null;
            synchronized (selectAggregatedRate_pstmt) {
                selectAggregatedRate_pstmt.clearParameters();
                selectAggregatedRate_pstmt.setString(1, sourceHost);
                selectAggregatedRate_pstmt.setString(2, destinationHost);
                rs = selectAggregatedRate_pstmt.executeQuery();
            }
            if (rs.next()) {
                aggregatedRate = rs.getFloat(1);
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Error retrieving aggregated rate.", e);
        }
        return aggregatedRate;
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
        int numberOfTransfers = 0;
        try {
            ResultSet rs = null;
            synchronized (selectNumberOfTransfers_pstmt) {
                selectNumberOfTransfers_pstmt.clearParameters();
                selectNumberOfTransfers_pstmt.setString(1, sourceHost);
                selectNumberOfTransfers_pstmt.setString(2, destinationHost);
                rs = selectNumberOfTransfers_pstmt.executeQuery();
            }
            if (rs.next()) {
                numberOfTransfers = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Error retrieving number of transfers.", e);
        }
        return numberOfTransfers;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (isOpen) {
                for (String resourceId : resourceAllocations) {
                    try {
                        removeResourceAllocation(resourceId);
                    } catch (ResourceAllocationException e) {
                        LOG.warn("Couldn't remove resource allocation "
                                + resourceId);
                    }
                }
                closePreparedStatement(insertHostname_pstmt);
                closePreparedStatement(insertResourceAllocation_pstmt);
                closePreparedStatement(removeResourceAllocation_pstmt);
                closePreparedStatement(selectAggregatedRate_pstmt);
                closePreparedStatement(selectAggregatedTransferStreams_pstmt);
                closePreparedStatement(selectNumberOfTransfers_pstmt);
                closePreparedStatement(selectHostname_pstmt);
                closePreparedStatement(updateResourceAllocation_pstmt);

                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
                try {
                    conn.close();
                } catch (SQLException e) {
                }
                conn = null;
                isOpen = false;
            }
        }
    }

    private void closePreparedStatement(PreparedStatement p) {
        if (p != null) {
            try {
                p.close();
            } catch (SQLException e) {
            }
            p = null;
        }
    }

    @Override
    public void finalize() {
        close();
    }
}
