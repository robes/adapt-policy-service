package edu.isi.policy.adapt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.lbl.srm.client.main.IPassiveTransferMonitor;
import gov.lbl.srm.client.main.SRMClientN;

import org.junit.Before;
import org.junit.Test;

public class PassiveTransferMonitorTest {

    private static final String PTM_FILE_NAME = "/ptm.log";

    private String ptmLog;

    @Before
    public void findLogFile() {
        ptmLog = this.getClass().getResource(PTM_FILE_NAME).getFile();
    }

    @Test
    public void testBrowseLogFile() {
        IPassiveTransferMonitor ptm = SRMClientN.getPTM();
        long[] statistics = ptm.getStatistics();
        assertEquals(0, statistics[0]);
        assertEquals(0, statistics[1]);
        assertEquals(0, statistics[2]);

        boolean loaded = ptm.browseLogFile(ptmLog);
        assertTrue(loaded);

        statistics = ptm.getStatistics();
        assertEquals(15, statistics[0]);
        assertEquals(47, statistics[1]);
        assertEquals(1, statistics[2]);
    }
}
