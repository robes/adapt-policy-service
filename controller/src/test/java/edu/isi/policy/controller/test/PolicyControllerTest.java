package edu.isi.policy.controller.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.isi.policy.controller.PolicyController;
import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.util.TransferList;

public class PolicyControllerTest {

    private PolicyController policyController;

    @Before
    public void createPolicyController() {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "/WEB-INF/policy-context.xml");
        policyController = (PolicyController) context
                .getBean("policyController");
    }

    @Test
    public void testRequestNewTransfers() throws URISyntaxException {
        final Transfer t1 = new Transfer(new URI(
                "gsiftp://jacoby.isi.edu/tmp/test.txt"), new URI(
                        "file:///tmp/test2.txt"));
        final Transfer t2 = new Transfer(new URI("file:///tmp/test4.txt"),
                new URI("gsiftp://jacoby.isi.edu/home/policy/test4.txt"));
        final TransferList tl = new TransferList();
        tl.add(t1);
        tl.add(t2);

        final TransferList result = policyController.addTransfers(tl);
        assertNotNull(result);
        assertEquals(result.size(), 2);

        boolean found1 = false;
        boolean found2 = false;
        for (Transfer t : result) {
            if (t.getSource().equals(t1.getSource())
                    && t.getDestination().equals(t1.getDestination())) {
                if (found1) {
                    fail("Unexpected transfer result " + t);
                } else {
                    found1 = true;
                }
            } else if (t.getSource().equals(t2.getSource())
                    && t.getDestination().equals(t2.getDestination())) {
                if (found2) {
                    fail("Unexpected transfer result " + t);
                } else {
                    found2 = true;
                }
            } else {
                fail("Unexpected transfer result " + t);
            }
        }
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    public void testGetTransfer_missingTransfer() {
        boolean missing = false;
        try {
            policyController.getTransfer("34242432-4234234-234324");
        } catch (HTTPException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                missing = true;
            }
        }
        assertEquals(true, missing);
    }

    @Test
    public void testUpdateTransfer_missingTransfer() {
        boolean missing = false;
        try {
            policyController.updateTransfer("34242432-4234234-234324",
                    new Transfer());
        } catch (HTTPException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                missing = true;
            }
        }
        assertEquals(true, missing);
    }

    @Test
    public void testGetCleanup_missingCleanup() {
        boolean missing = false;
        try {
            policyController.getCleanup("34242432-4234234-234324");
        } catch (HTTPException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                missing = true;
            }
        }
        assertEquals(true, missing);
    }

    @Test
    public void testUpdateCleanup_missingCleanup() {
        boolean missing = false;
        try {
            policyController.updateCleanup("34242432-4234234-234324",
                    new Cleanup());
        } catch (HTTPException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                missing = true;
            }
        }
        assertEquals(true, missing);
    }

    @Test
    public void testRemoveTransfer_missingTransfer() {
        boolean missing = false;
        try {
            policyController.removeTransfer("4234-234234-34234");
        } catch (HTTPException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                missing = true;
            }
        }
        assertEquals(true, missing);
    }

    @Test
    public void testGetGlobalVariable_missingVariable() {
        boolean missing = false;
        try {
            policyController.getGlobal("notavariable");
        } catch (HTTPException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                missing = true;
            }
        }
        assertEquals(true, missing);
    }
}
