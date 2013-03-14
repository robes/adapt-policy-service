package edu.isi.policy.adapt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.policy.adapt.PolicyModule;
import edu.isi.policy.adapt.ResourceAllocationException;
import edu.isi.policy.adapt.ResourceAllocationLog;
import edu.isi.policy.adapt.SQLResourceAllocationLogImpl;
import edu.isi.policy.entity.ResourceExpressionPair;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.util.ResourceExpressionPairIntegerMap;
import edu.isi.policy.util.TransferStatisticsLog;
import gov.lbl.srm.client.main.IPassiveTransferMonitor;

public class DroolsPolicyManager_Bestman_Test {
    private static String hostname;
    private PolicyModule policyService;
    private IPassiveTransferMonitor passiveTransferMonitor;
    private ResourceAllocationLog resourceAllocationLog;

    private class DummyTransferMonitor implements IPassiveTransferMonitor {

        // keep this simple for testing - no accessors
        public long currentTxfRate = 0;
        public long totalTransferredData = 0;
        public long currentNumberOfStreams = 0;

        @Override
        public long[] getStatistics() {
            return new long[] { currentTxfRate, totalTransferredData,
                    currentNumberOfStreams };
        }

        @Override
        public void update(long arg0, long arg1, int arg2) {
            // TODO Auto-generated method stub
            currentTxfRate = arg0;
            totalTransferredData = arg1;
            currentNumberOfStreams = arg2;
        }

        @Override
        public boolean browseLogFile(String arg0) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    @Before
    public void preparePolicyServiceParameters()
            throws UnknownHostException,
            ResourceAllocationException, GlobalVariableNotFoundException {
        cleanupResourceAllocationDB();
        Properties props = new Properties();
        props.setProperty(PolicyModule.DEFAULT_MAX_RATE_KEY, "100");
        props.setProperty(PolicyModule.DEFAULT_MAX_STREAMS_KEY, "6");
        props.setProperty(PolicyModule.TRANSFER_STATISTICS_LOG_KEY,
                "edu.isi.policy.util.TransferStatisticsLogImpl");
        props.setProperty(PolicyModule.POLICY_FILE_KEY, "bestman.drl");
        props.setProperty(PolicyModule.LOGGER_TYPE_KEY,
                PolicyModule.LOG4J_LOGGER_TYPE);
        props.setProperty(PolicyModule.MAX_BANDWIDTH_KEY, "test_max_rate.txt");
        props.setProperty(PolicyModule.MAX_PARALLEL_STREAMS_KEY,
                "test_max_streams.txt");
        props.setProperty(PolicyModule.RESOURCE_ALLOCATION_LOG_KEY,
                "edu.isi.policy.adapt.SQLResourceAllocationLogImpl");
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_DRIVER_KEY,
                "org.hsqldb.jdbc.JDBCDriver");
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_URL_KEY,
                "jdbc:hsqldb:mem:ral" + new Random().nextInt());
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_USER_KEY, "SA");
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_PASSWORD_KEY,
                "");
        props.setProperty(PolicyModule.PTM_KEY,
                "gov.lbl.srm.client.main.SRMPassiveTransferMonitor");
        props.setProperty(PolicyModule.PTM_POLICY_FILE_KEY, "ptm.drl");

        InetAddress addr = InetAddress.getLocalHost();
        hostname = addr.getCanonicalHostName();
        passiveTransferMonitor = new DummyTransferMonitor();
        policyService = new PolicyModule(props);
        resourceAllocationLog = (ResourceAllocationLog) policyService
                .getGlobalVariable("resourceAllocationLog");
    }

    @Test
    public void testNewTransferDefaults() throws URISyntaxException {
        Transfer t = new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/");
        t.setProperty("data_volume", "1000000000");
        t = policyService.addTransfer(t);
        assertNotNull(t.getId());
        assertEquals(6, Integer.parseInt(t.getProperty("max_streams")));
        assertEquals(100, Float.parseFloat(t.getProperty("max_rate")), 0);

        Transfer t2 = new Transfer("gsiftp://server2.isi.edu/tmp/test2/",
                "file:///tmp/test2/");
        t2.setProperty("data_volume", "200000000");
        t2.setProperty("local_file_host", hostname);
        t2 = policyService.addTransfer(t2);
        assertNotNull(t2.getId());
        assertEquals(6, Integer.parseInt(t2.getProperty("max_streams")));
        assertEquals(100, Float.parseFloat(t2.getProperty("max_rate")), 0);
    }


    @Test
    public void testPassiveMonitorTransferDefaults() throws URISyntaxException,
    EntityNotFoundException {
        Transfer t = new Transfer("gsiftp://server1.isi.edu/tmp/test1/", "gsiftp://client1.isi.edu/tmp/test1/");
        t.setProperty("data_volume", "10000000");
        ((DummyTransferMonitor) passiveTransferMonitor).currentTxfRate = 80;
        ((DummyTransferMonitor) passiveTransferMonitor).totalTransferredData = 20000000;
        ((DummyTransferMonitor) passiveTransferMonitor).currentNumberOfStreams = 4;
        policyService.setGlobalVariable("passiveTransferMonitor", passiveTransferMonitor);
        t = policyService.addTransfer(t);

        // should be the default 6 and 100, respectively, since policy chooses
        // the higher between the PTM and default
        assertEquals(6, Integer.parseInt(t.getProperty("max_streams")));
        assertEquals(100, Float.parseFloat(t.getProperty("max_rate")), 0);

        ((DummyTransferMonitor) passiveTransferMonitor).currentTxfRate = 200;
        ((DummyTransferMonitor) passiveTransferMonitor).currentNumberOfStreams = 8;

        Transfer t2 = new Transfer("gsiftp://server2.isi.edu/tmp/test2/",
                "file:///tmp/test2/");
        t2.setProperty("data_volume", "200000");
        t2.setProperty("local_file_host", hostname);
        t2 = policyService.addTransfer(t2);

        // should be PTM 8 and 200, respectively, since it is higher than the
        // defaults
        assertEquals(8, Integer.parseInt(t2.getProperty("max_streams")));
        assertEquals(200, Float.parseFloat(t2.getProperty("max_rate")), 0);

        t.setProperty("STATUS", "COMPLETED");
        policyService.updateTransfer(t.getId(), t);
        ((DummyTransferMonitor) passiveTransferMonitor).currentTxfRate = 2048;
        ((DummyTransferMonitor) passiveTransferMonitor).totalTransferredData += 1000000;
        ((DummyTransferMonitor) passiveTransferMonitor).currentNumberOfStreams = 2;
        policyService.setGlobalVariable("passiveTransferMonitor", passiveTransferMonitor);
        t = new Transfer("gsiftp://server1.isi.edu/tmp/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/");
        t = policyService.addTransfer(t);
        assertEquals(6, Integer.parseInt(t.getProperty("max_streams")));
        assertEquals(2048, Float.parseFloat(t.getProperty("max_rate")), 0);
    }


    @Test
    public void testTransferStatisticsLog() throws URISyntaxException,
    EntityNotFoundException, GlobalVariableNotFoundException {
        Transfer t = new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/");
        t.setProperty("data_volume", "10000000");
        TransferStatisticsLog transferStatisticsLog = (TransferStatisticsLog) policyService
                .getGlobalVariable("transferStatisticsLog");
        assertEquals(0, transferStatisticsLog.getNumberOfNewTransfers());
        t = policyService.addTransfer(t);
        assertEquals(1, transferStatisticsLog.getNumberOfNewTransfers());
        assertEquals(0, transferStatisticsLog.getNumberOfNewCleanups());
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersCompleted());
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersFailed());

        Transfer t2 = new Transfer("gsiftp://server2.isi.edu/tmp/test2/",
                "file:///tmp/test2/");
        t2.setProperty("data_volume", "200000");
        t2.setProperty("local_file_host", hostname);
        t2 = policyService.addTransfer(t2);
        assertEquals(2, transferStatisticsLog.getNumberOfNewTransfers());
        assertEquals(0, transferStatisticsLog.getNumberOfNewCleanups());
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersCompleted());
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersFailed());

        t.setProperty("STATUS", "COMPLETED");
        policyService.updateTransfer(t.getId(), t);
        assertEquals(2, transferStatisticsLog.getNumberOfNewTransfers());
        assertEquals(0, transferStatisticsLog.getNumberOfNewCleanups());
        assertEquals(1, transferStatisticsLog.getNumberOfTransfersCompleted());
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersFailed());

        t2.setProperty("STATUS", "FAILED");
        t2 = policyService.updateTransfer(t2.getId(), t2);
        assertEquals(2, transferStatisticsLog.getNumberOfNewTransfers());
        assertEquals(0, transferStatisticsLog.getNumberOfNewCleanups());
        assertEquals(1, transferStatisticsLog.getNumberOfTransfersCompleted());
        assertEquals(1, transferStatisticsLog.getNumberOfTransfersFailed());

    }

    @Test
    public void testResourceAllocationLog() throws URISyntaxException,
    ResourceAllocationException, EntityNotFoundException {
        assertEquals(0, resourceAllocationLog.getNumberOfTransfers(
                "server1.isi.edu", "client1.isi.edu"));
        Transfer t = new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/");
        t.setProperty("data_volume", "10000000");
        t = policyService.addTransfer(t);
        t.setProperty("adjusted_streams", "2");
        t.setProperty("adjusted_rate", "50.0");
        t = policyService.updateTransfer(t.getId(), t);
        assertEquals(1, resourceAllocationLog.getNumberOfTransfers(
                "server1.isi.edu", "client1.isi.edu"));
        assertEquals(2, resourceAllocationLog.getAggregatedTransferStreams(
                "server1.isi.edu", "client1.isi.edu"));
        assertEquals(50.0f, resourceAllocationLog.getAggregatedRate(
                "server1.isi.edu", "client1.isi.edu"), 0);
        assertNotNull(resourceAllocationLog.getResourceAllocation(t.getId()));

        Transfer t2 = new Transfer("gsiftp://server1.isi.edu/tmp/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2 = policyService.addTransfer(t2);
        t2.setProperty("adjusted_streams", "4");
        t2.setProperty("adjusted_rate", "100.0");
        t2 = policyService.updateTransfer(t2.getId(), t2);
        assertEquals(2, resourceAllocationLog.getNumberOfTransfers(
                "server1.isi.edu", "client1.isi.edu"));
        assertEquals(6, resourceAllocationLog.getAggregatedTransferStreams(
                "server1.isi.edu", "client1.isi.edu"));
        assertEquals(150.0f, resourceAllocationLog.getAggregatedRate(
                "server1.isi.edu", "client1.isi.edu"), 0);
        assertNotNull(resourceAllocationLog.getResourceAllocation(t2.getId()));

        Transfer t3 = new Transfer("gsiftp://server2.isi.edu/tmp/test3/",
                "gsiftp://client2.isi.edu/tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3 = policyService.addTransfer(t3);
        t3.setProperty("adjusted_streams", "4");
        t3.setProperty("adjusted_rate", "75.0");
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(1, resourceAllocationLog.getNumberOfTransfers(
                "server2.isi.edu", "client2.isi.edu"));
        assertEquals(4, resourceAllocationLog.getAggregatedTransferStreams(
                "server2.isi.edu", "client2.isi.edu"));
        assertEquals(75.0f, resourceAllocationLog.getAggregatedRate(
                "server2.isi.edu", "client2.isi.edu"), 0);
        assertNotNull(resourceAllocationLog.getResourceAllocation(t3.getId()));

        Transfer t4 = new Transfer("gsiftp://server3.isi.edu/tmp/test4/",
                "file:///tmp/test4/");
        t4.setProperty("data_volume", "100000");
        t4.setProperty("local_file_host", "client3.isi.edu");
        t4 = policyService.addTransfer(t4);
        t4.setProperty("adjusted_streams", "10");
        t4.setProperty("adjusted_rate", "300.0");
        t4 = policyService.updateTransfer(t4.getId(), t4);
        assertEquals(1, resourceAllocationLog.getNumberOfTransfers(
                "server3.isi.edu", "client3.isi.edu"));
        assertEquals(10, resourceAllocationLog.getAggregatedTransferStreams(
                "server3.isi.edu", "client3.isi.edu"));
        assertEquals(300.0f, resourceAllocationLog.getAggregatedRate(
                "server3.isi.edu", "client3.isi.edu"), 0);
        assertNotNull(resourceAllocationLog.getResourceAllocation(t4.getId()));

    }

    /*
     * @Test public void testAggregatedRateAndStreamsThresholds() throws
     * URISyntaxException, EntityNotFoundException { Transfer t = new
     * Transfer("gsiftp://servera.isi.edu/tmp/test1/",
     * "gsiftp://clienta.isi.edu/tmp/test1/"); t.setProperty("data_volume",
     * "10000000"); t = policyService.addTransfer(t);
     * 
     * // single transfer violating threshold t.setProperty("adjusted_streams",
     * "20"); t.setProperty("adjusted_rate", "300"); t =
     * policyService.updateTransfer(t.getId(), t);
     * t.getProperties().remove("max_streams");
     * t.getProperties().remove("max_rate");
     * t.getProperties().remove("adjusted_streams");
     * t.getProperties().remove("adjusted_rate"); t =
     * policyService.updateTransfer(t.getId(), t); assertEquals(250,
     * Float.parseFloat(t.getProperty("max_rate")), 0); assertEquals(12,
     * Integer.parseInt(t.getProperty("max_streams")));
     * 
     * // two transfers Transfer t2 = new
     * Transfer("gsiftp://servera.isi.edu/tmp/test2/", "file:///tmp/test2/");
     * t2.setProperty("data_volume", "10000000");
     * t2.setProperty("local_file_host", "clienta.isi.edu"); t2 =
     * policyService.addTransfer(t2);
     * 
     * t2.getProperties().remove("max_streams");
     * t2.getProperties().remove("max_rate"); t2.setProperty("adjusted_rate",
     * "100"); t2.setProperty("adjusted_streams", "2"); t2 =
     * policyService.updateTransfer(t2.getId(), t2); assertEquals(125,
     * Float.parseFloat(t2.getProperty("max_rate")), 0); assertEquals(6,
     * Integer.parseInt(t2.getProperty("max_streams")));
     * t.getProperties().remove("max_streams");
     * t.getProperties().remove("max_rate");
     * t.getProperties().setProperty("adjusted_rate", "100");
     * t.getProperties().setProperty("adjusted_streams", "2"); t =
     * policyService.updateTransfer(t.getId(), t); assertEquals(100,
     * Float.parseFloat(t.getProperty("max_rate")), 0); assertEquals(6,
     * Integer.parseInt(t.getProperty("max_streams")));
     * 
     * // removing a transfer t.getProperties().remove("max_rate");
     * t.getProperties().remove("max_streams");
     * t2.getProperties().remove("max_streams");
     * t2.getProperties().remove("max_rate");
     * 
     * t.setProperty("adjusted_streams", "30"); t.setProperty("adjusted_rate",
     * "400"); t2.setProperty("adjusted_streams", "30");
     * t2.setProperty("adjusted_rate", "400");
     * 
     * t = policyService.updateTransfer(t.getId(), t); t2 =
     * policyService.updateTransfer(t2.getId(), t2);
     * 
     * assertEquals(6, Integer.parseInt(t.getProperty("max_streams")));
     * assertEquals(6, Integer.parseInt(t2.getProperty("max_streams")));
     * assertEquals(125, Float.parseFloat(t.getProperty("max_rate")), 0);
     * assertEquals(125, Float.parseFloat(t2.getProperty("max_rate")), 0);
     * 
     * t2.setProperty("STATUS", "COMPLETED");
     * t2.getProperties().remove("max_rate");
     * t2.getProperties().remove("max_streams"); t2 =
     * policyService.updateTransfer(t2.getId(), t2); assertEquals(1,
     * policyService.getTransfers().size());
     * 
     * t.getProperties().remove("max_rate");
     * t.getProperties().remove("max_streams"); t =
     * policyService.updateTransfer(t.getId(), t); assertEquals(12,
     * Integer.parseInt(t.getProperty("max_streams"))); assertEquals(250,
     * Float.parseFloat(t.getProperty("max_rate")), 0);
     * 
     * }
     */

    @Test
    public void testReserveInitialStreams_3rdParty() throws URISyntaxException,
    EntityNotFoundException {
        // total max streams: 12
        // default: 6 streams

        // 1st transfer - should receive the full default
        // resulting total max left: 6
        // advice: 6 streams
        Transfer t1 = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t1.setProperty("data_volume", "10000000");
        t1 = policyService.addTransfer(t1);

        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // 2nd transfer - should receive the full default
        // resulting total max left: 0
        // advice: 6 streams
        Transfer t2 = new Transfer("gsiftp://servera.isi.edu/tmp/test2/",
                "gsiftp://clienta.isi.edu/tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2 = policyService.addTransfer(t2);

        assertEquals(6, Integer.parseInt(t2.getProperty("max_streams")));

        // 1st transfer - update with adjusted 4 streams
        // resulting total max left: 2
        t1.getProperties().clear();
        t1.setProperty("adjusted_streams", "4");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        // 1st transfer - should receive the full default, since
        // this transfer is utilizing 4 streams itself
        // resulting total max left: 0
        // advice: 6 streams
        t1.getProperties().clear();
        t1 = policyService.updateTransfer(t1.getId(), t1);

        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // 2nd transfer - update with adjusted 2 streams
        // resulting total max left: 4
        t2.getProperties().clear();
        t2.setProperty("adjusted_streams", "2");
        t2 = policyService.updateTransfer(t2.getId(), t2);

        // 3rd transfer - should receive a reduced default
        // resulting total max left: 0
        // advice: 4 streams
        Transfer t3 = new Transfer("gsiftp://servera.isi.edu/tmp/test3/",
                "gsiftp://clienta.isi.edu/tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3 = policyService.addTransfer(t3);

        assertEquals(4, Integer.parseInt(t3.getProperty("max_streams")));

        // 1st transfer - complete the transfer
        // should free up its streams again
        // resulting total max left: 6
        t1.setProperty("STATUS", "COMPLETED");
        policyService.updateTransfer(t1.getId(), t1);

        // 3rd transfer - update with 4 streams
        // resulting total max left: 6
        t3.getProperties().clear();
        t3.setProperty("adjusted_streams", "4");
        t3 = policyService.updateTransfer(t3.getId(), t3);

        // 3rd transfer - request advice and should receive the full default
        // resulting total max left: 0
        // advice: 6 streams
        t3.getProperties().clear();
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(6, Integer.parseInt(t3.getProperty("max_streams")));
    }

    @Test
    public void testReserveInitialRate_3rdParty() throws URISyntaxException,
    EntityNotFoundException {
        // total max rate: 250
        // default: 100

        // 1st transfer - should receive the full default
        // resulting total max left: 150
        // advice: 100
        Transfer t1 = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t1.setProperty("data_volume", "10000000");
        t1 = policyService.addTransfer(t1);

        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // 2nd transfer - should receive the full default
        // resulting total max left: 50
        // advice: 100
        Transfer t2 = new Transfer("gsiftp://servera.isi.edu/tmp/test2/",
                "gsiftp://clienta.isi.edu/tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2 = policyService.addTransfer(t2);

        assertEquals(100, Float.parseFloat(t2.getProperty("max_rate")), 0);

        // 1st transfer - update with adjusted 75
        // resulting total max left: 75
        t1.getProperties().clear();
        t1.setProperty("adjusted_rate", "75");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        // 1st transfer - should receive the full default, since
        // this transfer is utilizing 75 streams itself
        // resulting total max left: 50
        // advice: 100
        t1.getProperties().clear();
        t1 = policyService.updateTransfer(t1.getId(), t1);

        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // 2nd transfer - update with adjusted 90
        // resulting total max left: 60
        t2.getProperties().clear();
        t2.setProperty("adjusted_rate", "90");
        t2 = policyService.updateTransfer(t2.getId(), t2);

        // 3rd transfer - should receive a reduced default
        // resulting total max left: 0
        // advice: 60
        Transfer t3 = new Transfer("gsiftp://servera.isi.edu/tmp/test3/",
                "gsiftp://clienta.isi.edu/tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3 = policyService.addTransfer(t3);

        assertEquals(60, Float.parseFloat(t3.getProperty("max_rate")), 0);

        // 1st transfer - complete the transfer
        // should free up its streams again
        // resulting total max left: 100
        t1.setProperty("STATUS", "COMPLETED");
        policyService.updateTransfer(t1.getId(), t1);

        // 3rd transfer - update with 50
        // resulting total max left: 110
        t3.getProperties().clear();
        t3.setProperty("adjusted_rate", "50");
        t3 = policyService.updateTransfer(t3.getId(), t3);

        // 3rd transfer - request advice and should receive the full default
        // resulting total max left: 60
        // advice: 100
        t3.getProperties().clear();
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(100, Float.parseFloat(t3.getProperty("max_rate")), 0);
    }

    @Test
    public void testReserveInitialStreams_2PartyDl() throws URISyntaxException,
    EntityNotFoundException {
        // total max streams: 12
        // default: 6 streams

        // 1st transfer - should receive the full default
        // resulting total max left: 6
        // advice: 6 streams
        Transfer t1 = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "file:///tmp/test1/");
        t1.setProperty("local_file_host", "clienta.isi.edu");
        t1.setProperty("data_volume", "10000000");
        t1 = policyService.addTransfer(t1);

        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // 2nd transfer - should receive the full default
        // resulting total max left: 0
        // advice: 6 streams
        Transfer t2 = new Transfer("gsiftp://servera.isi.edu/tmp/test2/",
                "file:///tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2.setProperty("local_file_host", "clienta.isi.edu");
        t2 = policyService.addTransfer(t2);

        assertEquals(6, Integer.parseInt(t2.getProperty("max_streams")));

        // 1st transfer - update with adjusted 4 streams
        // resulting total max left: 2
        t1.getProperties().clear();
        t1.setProperty("adjusted_streams", "4");
        t1.setProperty("local_file_host", "clienta.isi.edu");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        // 1st transfer - should receive the full default, since
        // this transfer is utilizing 4 streams itself
        // resulting total max left: 0
        // advice: 6 streams
        t1.getProperties().clear();
        t1 = policyService.updateTransfer(t1.getId(), t1);
        t1.setProperty("local_file_host", "clienta.isi.edu");
        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // 2nd transfer - update with adjusted 2 streams
        // resulting total max left: 4
        t2.getProperties().clear();
        t2.setProperty("adjusted_streams", "2");
        t2.setProperty("local_file_host", "clienta.isi.edu");
        t2 = policyService.updateTransfer(t2.getId(), t2);

        // 3rd transfer - should receive a reduced default
        // resulting total max left: 0
        // advice: 4 streams
        Transfer t3 = new Transfer("gsiftp://servera.isi.edu/tmp/test3/",
                "file:///tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3.setProperty("local_file_host", "clienta.isi.edu");
        t3 = policyService.addTransfer(t3);

        assertEquals(4, Integer.parseInt(t3.getProperty("max_streams")));

        // 1st transfer - complete the transfer
        // should free up its streams again
        // resulting total max left: 6
        t1.setProperty("STATUS", "COMPLETED");
        t1.setProperty("local_file_host", "clienta.isi.edu");
        policyService.updateTransfer(t1.getId(), t1);

        // 3rd transfer - update with 4 streams
        // resulting total max left: 6
        t3.getProperties().clear();
        t3.setProperty("adjusted_streams", "4");
        t3.setProperty("local_file_host", "clienta.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);

        // 3rd transfer - request advice and should receive the full default
        // resulting total max left: 0
        // advice: 6 streams
        t3.getProperties().clear();
        t1.setProperty("local_file_host", "clienta.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(6, Integer.parseInt(t3.getProperty("max_streams")));
    }

    @Test
    public void testReserveInitialRate_2PartyDl() throws URISyntaxException,
    EntityNotFoundException {
        // total max rate: 250
        // default: 100

        // 1st transfer - should receive the full default
        // resulting total max left: 150
        // advice: 100
        Transfer t1 = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "file:///tmp/test1/");
        t1.setProperty("data_volume", "10000000");
        t1.setProperty("local_file_host", "clienta.isi.edu");
        t1 = policyService.addTransfer(t1);

        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // 2nd transfer - should receive the full default
        // resulting total max left: 50
        // advice: 100
        Transfer t2 = new Transfer("gsiftp://servera.isi.edu/tmp/test2/",
                "file:///tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2.setProperty("local_file_host", "clienta.isi.edu");
        t2 = policyService.addTransfer(t2);

        assertEquals(100, Float.parseFloat(t2.getProperty("max_rate")), 0);

        // 1st transfer - update with adjusted 75
        // resulting total max left: 75
        t1.getProperties().clear();
        t1.setProperty("local_file_host", "clienta.isi.edu");
        t1.setProperty("adjusted_rate", "75");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        // 1st transfer - should receive the full default, since
        // this transfer is utilizing 75 streams itself
        // resulting total max left: 50
        // advice: 100
        t1.getProperties().clear();
        t1.setProperty("local_file_host", "clienta.isi.edu");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // 2nd transfer - update with adjusted 90
        // resulting total max left: 60
        t2.getProperties().clear();
        t2.setProperty("adjusted_rate", "90");
        t2.setProperty("local_file_host", "clienta.isi.edu");
        t2 = policyService.updateTransfer(t2.getId(), t2);

        // 3rd transfer - should receive a reduced default
        // resulting total max left: 0
        // advice: 60
        Transfer t3 = new Transfer("gsiftp://servera.isi.edu/tmp/test3/",
                "file:///tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3.setProperty("local_file_host", "clienta.isi.edu");
        t3 = policyService.addTransfer(t3);

        assertEquals(60, Float.parseFloat(t3.getProperty("max_rate")), 0);

        // 1st transfer - complete the transfer
        // should free up its streams again
        // resulting total max left: 100
        t1.setProperty("STATUS", "COMPLETED");
        t1.setProperty("local_file_host", "clienta.isi.edu");
        policyService.updateTransfer(t1.getId(), t1);

        // 3rd transfer - update with 50
        // resulting total max left: 110
        t3.getProperties().clear();
        t3.setProperty("adjusted_rate", "50");
        t3.setProperty("local_file_host", "clienta.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);

        // 3rd transfer - request advice and should receive the full default
        // resulting total max left: 60
        // advice: 100
        t3.getProperties().clear();
        t3.setProperty("local_file_host", "clienta.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(100, Float.parseFloat(t3.getProperty("max_rate")), 0);
    }

    @Test
    public void testReserveInitialStreams_2PartyUl() throws URISyntaxException,
    EntityNotFoundException {
        // total max streams: 12
        // default: 6 streams

        // 1st transfer - should receive the full default
        // resulting total max left: 6
        // advice: 6 streams
        Transfer t1 = new Transfer("file:///tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t1.setProperty("local_file_host", "servera.isi.edu");
        t1.setProperty("data_volume", "10000000");
        t1 = policyService.addTransfer(t1);

        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // 2nd transfer - should receive the full default
        // resulting total max left: 0
        // advice: 6 streams
        Transfer t2 = new Transfer("file:///tmp/test2/",
                "gsiftp://clienta.isi.edu/tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2.setProperty("local_file_host", "servera.isi.edu");
        t2 = policyService.addTransfer(t2);

        assertEquals(6, Integer.parseInt(t2.getProperty("max_streams")));

        // 1st transfer - update with adjusted 4 streams
        // resulting total max left: 2
        t1.getProperties().clear();
        t1.setProperty("adjusted_streams", "4");
        t1.setProperty("local_file_host", "servera.isi.edu");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        // 1st transfer - should receive the full default, since
        // this transfer is utilizing 4 streams itself
        // resulting total max left: 0
        // advice: 6 streams
        t1.getProperties().clear();
        t1 = policyService.updateTransfer(t1.getId(), t1);
        t1.setProperty("local_file_host", "servera.isi.edu");
        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // 2nd transfer - update with adjusted 2 streams
        // resulting total max left: 4
        t2.getProperties().clear();
        t2.setProperty("adjusted_streams", "2");
        t2.setProperty("local_file_host", "servera.isi.edu");
        t2 = policyService.updateTransfer(t2.getId(), t2);

        // 3rd transfer - should receive a reduced default
        // resulting total max left: 0
        // advice: 4 streams
        Transfer t3 = new Transfer("file:///tmp/test3/",
                "gsiftp://clienta.isi.edu/tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3.setProperty("local_file_host", "servera.isi.edu");
        t3 = policyService.addTransfer(t3);

        assertEquals(4, Integer.parseInt(t3.getProperty("max_streams")));

        // 1st transfer - complete the transfer
        // should free up its streams again
        // resulting total max left: 6
        t1.setProperty("STATUS", "COMPLETED");
        t1.setProperty("local_file_host", "servera.isi.edu");
        policyService.updateTransfer(t1.getId(), t1);

        // 3rd transfer - update with 4 streams
        // resulting total max left: 6
        t3.getProperties().clear();
        t3.setProperty("adjusted_streams", "4");
        t3.setProperty("local_file_host", "servera.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);

        // 3rd transfer - request advice and should receive the full default
        // resulting total max left: 0
        // advice: 6 streams
        t3.getProperties().clear();
        t1.setProperty("local_file_host", "servera.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(6, Integer.parseInt(t3.getProperty("max_streams")));
    }

    @Test
    public void testReserveInitialRate_2PartyUl() throws URISyntaxException,
    EntityNotFoundException {
        // total max rate: 250
        // default: 100

        // 1st transfer - should receive the full default
        // resulting total max left: 150
        // advice: 100
        Transfer t1 = new Transfer("file:///tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t1.setProperty("data_volume", "10000000");
        t1.setProperty("local_file_host", "servera.isi.edu");
        t1 = policyService.addTransfer(t1);

        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // 2nd transfer - should receive the full default
        // resulting total max left: 50
        // advice: 100
        Transfer t2 = new Transfer("file:///tmp/test2/",
                "gsiftp://clienta.isi.edu/tmp/test2/");
        t2.setProperty("data_volume", "10000000");
        t2.setProperty("local_file_host", "servera.isi.edu");
        t2 = policyService.addTransfer(t2);

        assertEquals(100, Float.parseFloat(t2.getProperty("max_rate")), 0);

        // 1st transfer - update with adjusted 75
        // resulting total max left: 75
        t1.getProperties().clear();
        t1.setProperty("adjusted_rate", "75");
        t1.setProperty("local_file_host", "servera.isi.edu");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        // 1st transfer - should receive the full default, since
        // this transfer is utilizing 75 streams itself
        // resulting total max left: 50
        // advice: 100
        t1.getProperties().clear();
        t1.setProperty("local_file_host", "servera.isi.edu");
        t1 = policyService.updateTransfer(t1.getId(), t1);

        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // 2nd transfer - update with adjusted 90
        // resulting total max left: 60
        t2.getProperties().clear();
        t2.setProperty("adjusted_rate", "90");
        t2.setProperty("local_file_host", "servera.isi.edu");
        t2 = policyService.updateTransfer(t2.getId(), t2);

        // 3rd transfer - should receive a reduced default
        // resulting total max left: 0
        // advice: 60
        Transfer t3 = new Transfer("file:///tmp/test3/",
                "gsiftp://clienta.isi.edu/tmp/test3/");
        t3.setProperty("data_volume", "10000000");
        t3.setProperty("local_file_host", "servera.isi.edu");
        t3 = policyService.addTransfer(t3);

        assertEquals(60, Float.parseFloat(t3.getProperty("max_rate")), 0);

        // 1st transfer - complete the transfer
        // should free up its streams again
        // resulting total max left: 100
        t1.setProperty("STATUS", "COMPLETED");
        t1.setProperty("local_file_host", "servera.isi.edu");
        policyService.updateTransfer(t1.getId(), t1);

        // 3rd transfer - update with 50
        // resulting total max left: 110
        t3.getProperties().clear();
        t3.setProperty("adjusted_rate", "50");
        t3.setProperty("local_file_host", "servera.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);

        // 3rd transfer - request advice and should receive the full default
        // resulting total max left: 60
        // advice: 100
        t3.getProperties().clear();
        t3.setProperty("local_file_host", "servera.isi.edu");
        t3 = policyService.updateTransfer(t3.getId(), t3);
        assertEquals(100, Float.parseFloat(t3.getProperty("max_rate")), 0);
    }

    @Test
    public void testStreamsBoundaries() throws URISyntaxException {
        // max: 12
        // default: 6

        // total: 6
        // free: 6
        Transfer t1 = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t1 = policyService.addTransfer(t1);
        assertEquals(6, Integer.parseInt(t1.getProperty("max_streams")));

        // total: 12
        // free: 0
        Transfer t2 = new Transfer("gsiftp://servera.isi.edu/tmp/test2/",
                "gsiftp://clienta.isi.edu/tmp/test2/");
        t2 = policyService.addTransfer(t2);
        assertEquals(6, Integer.parseInt(t2.getProperty("max_streams")));

        // total: 13
        // free: 0
        Transfer t3 = new Transfer("gsiftp://servera.isi.edu/tmp/test3/",
                "gsiftp://clienta.isi.edu/tmp/test3/");
        t3 = policyService.addTransfer(t3);
        assertEquals(1, Integer.parseInt(t3.getProperty("max_streams")));

        // total: 14
        // free: 0
        Transfer t4 = new Transfer("gsiftp://servera.isi.edu/tmp/test4/",
                "gsiftp://clienta.isi.edu/tmp/test4/");
        t4 = policyService.addTransfer(t4);
        assertEquals(1, Integer.parseInt(t4.getProperty("max_streams")));

        // total 15
        // free 0
        Transfer t5 = new Transfer("gsiftp://servera.isi.edu/tmp/test5/",
                "gsiftp://clienta.isi.edu/tmp/test5/");
        t5 = policyService.addTransfer(t5);
        assertEquals(1, Integer.parseInt(t5.getProperty("max_streams")));
    }

    @Test
    public void testRateBoundaries() throws URISyntaxException {
        // max: 250
        // default: 100

        // total: 100
        // free: 150
        Transfer t1 = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t1 = policyService.addTransfer(t1);
        assertEquals(100, Float.parseFloat(t1.getProperty("max_rate")), 0);

        // total: 200
        // free: 50
        Transfer t2 = new Transfer("gsiftp://servera.isi.edu/tmp/test2/",
                "gsiftp://clienta.isi.edu/tmp/test2/");
        t2 = policyService.addTransfer(t2);
        assertEquals(100, Float.parseFloat(t2.getProperty("max_rate")), 0);

        // total: 250
        // free: 0
        Transfer t3 = new Transfer("gsiftp://servera.isi.edu/tmp/test3/",
                "gsiftp://clienta.isi.edu/tmp/test3/");
        t3 = policyService.addTransfer(t3);
        assertEquals(50, Float.parseFloat(t3.getProperty("max_rate")), 0);

        // total: 251
        // free: 0
        Transfer t4 = new Transfer("gsiftp://servera.isi.edu/tmp/test4/",
                "gsiftp://clienta.isi.edu/tmp/test4/");
        t4 = policyService.addTransfer(t4);
        assertEquals(1, Float.parseFloat(t4.getProperty("max_rate")), 0);

        // total: 252
        // free: 0
        Transfer t5 = new Transfer("gsiftp://servera.isi.edu/tmp/test5/",
                "gsiftp://clienta.isi.edu/tmp/test5/");
        t5 = policyService.addTransfer(t5);
        assertEquals(1, Float.parseFloat(t5.getProperty("max_rate")), 0);
    }

    @Test
    public void testHigherDefaultThanMax() throws URISyntaxException {
        // max between hosts are 12 streams and 250 MB
        policyService.setGlobalVariable(PolicyModule.DEFAULT_MAX_STREAMS_KEY,
                20);
        policyService.setGlobalVariable(PolicyModule.DEFAULT_MAX_RATE_KEY,
                300.0f);

        Transfer t = new Transfer("gsiftp://servera.isi.edu/tmp/test1/",
                "gsiftp://clienta.isi.edu/tmp/test1/");
        t = policyService.addTransfer(t);
        assertEquals(12, Integer.parseInt(t.getProperty("max_streams")));
        assertEquals(250.0f, Float.parseFloat(t.getProperty("max_rate")), 0);
    }

/*
 *
    @Test
    public void testNersc() throws URISyntaxException {
        policyService.setGlobalVariable(PolicyModule.DEFAULT_MAX_STREAMS_KEY,
                680);
        ResourceExpressionPairIntegerMap maxStreams = new ResourceExpressionPairIntegerMap();
        maxStreams.put(new ResourceExpressionPair("dtn01.nersc.gov",
                "red-gridftp.unl.edu"), 600);
        policyService.setGlobalVariable(PolicyModule.MAX_PARALLEL_STREAMS_KEY,
                maxStreams);

        / *
         * Transfer t = new Transfer(
         * "gsiftp://dtn04.nersc.gov///scratch/sd/junmin/adt//26-amip.cam2.h2.1979-01-29-00000.nc"
         * ,
         * "gsiftp://red-gridftp.unl.edu:2811//mnt/hadoop/dropfiles/nersc/26-amip.cam2.h2.1979-01-29-00000.nc"
         * );
         * /
        Transfer t = new Transfer(
                "gsiftp://dtn01.nersc.gov///scratch/sd/junmin/adt//26-amip.cam2.h2.1979-01-29-00000.nc",
                "gsiftp://red-gridftp.unl.edu:2811//mnt/hadoop/dropfiles/nersc/26-amip.cam2.h2.1979-01-29-00000.nc");
        t.setProperty("data_volume", "1048576000");
        t = policyService.addTransfer(t);
        assertEquals(600, Integer.parseInt(t.getProperty("max_streams")));
    }
*
*/

    // cleans up resource allocation
    @After
    public void cleanupResourceAllocationDB() {
        if (resourceAllocationLog != null) {
            resourceAllocationLog.close();
        }
    }
}
