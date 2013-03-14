package edu.isi.policy.service.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.service.PolicyService;
import edu.isi.policy.service.PolicyServiceImpl;
import edu.isi.policy.service.drools.DroolsStatefulPolicySession;
import edu.isi.policy.util.CleanupList;
import edu.isi.policy.util.TransferList;

public class PolicyServiceImplTest {

    private PolicyService policyService = null;

    @Before
    public void createPolicyService() {
        policyService = new PolicyServiceImpl();
        Collection<String> rulesFile = new ArrayList<String>(1);
        rulesFile.add("default.drl");
        policyService.setPolicySession(new DroolsStatefulPolicySession(
                new HashMap<String, Object>(), rulesFile));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTransfers_nullArg() {
        policyService.addTransfers(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTransfers_emptyArg() {
        policyService.addTransfers(new TransferList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTransfer_nullArg() throws EntityNotFoundException {
        policyService.removeTransfer(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testRemoveTransfer_nonTransfer() throws EntityNotFoundException {
        policyService.removeTransfer("54353495453459--5345-35345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTransfer_emptyArg() throws EntityNotFoundException {
        policyService.removeTransfer("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTransfer_nullArg() throws EntityNotFoundException {
        policyService.getTransfer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTransfer_emptyArg() throws EntityNotFoundException {
        policyService.getTransfer("");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetTransfer_missingTransfer()
            throws EntityNotFoundException {
        policyService.getTransfer("5435-04534534-534534-5345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCleanup_nullArg() throws EntityNotFoundException {
        policyService.getCleanup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCleanup_emptyArg() throws EntityNotFoundException {
        policyService.getCleanup("");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetCleanup_missingCleanup() throws EntityNotFoundException {
        policyService.getCleanup("54543-543534-5435345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTransfer_null1stArg() throws EntityNotFoundException {
        policyService.updateTransfer(null, new Transfer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTransfer_empty1stArg() throws EntityNotFoundException {
        policyService.updateTransfer("", new Transfer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTransfer_null2ndArg() throws EntityNotFoundException {
        policyService.updateTransfer("423423-434323-42343", null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateTransfer_missingTransfer()
            throws EntityNotFoundException {
        policyService.updateTransfer("4324234-234234-342344",
                new Transfer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPolicySession_nullArg() {
        policyService.setPolicySession(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddCleanup_nullArg() {
        policyService.addCleanup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddCleanups_nullArg() {
        policyService.addCleanups(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddCleanups_emptyArg() {
        policyService.addCleanups(new CleanupList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveCleanup_nullArg() throws EntityNotFoundException {
        policyService.removeCleanup(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testRemoveCleanup_missingCleanup()
            throws EntityNotFoundException {
        policyService.removeCleanup("432423-432423-4324");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCleanup_null1stArg() throws EntityNotFoundException {
        policyService.updateCleanup(null, new Cleanup());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCleanup_empty1stArg() throws EntityNotFoundException {
        policyService.updateCleanup("", new Cleanup());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCleanup_null2ndArg() throws EntityNotFoundException {
        policyService.updateCleanup("4324-34234-24234324-3434", null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateCleanup_missingCleanup()
            throws EntityNotFoundException {
        policyService.updateCleanup("54254-45345-3453453",
                new Cleanup());
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddTransfer_nullArg() {
        policyService.addTransfer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTransfers_nullArg() {
        policyService.updateTransfers(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTransfers_emptyArg() {
        policyService.updateTransfers(new TransferList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCleanups_nullArg() {
        policyService.updateCleanups(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCleanups_emptyArg() {
        policyService.updateCleanups(new CleanupList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetGlobalVariable_nullArg()
            throws GlobalVariableNotFoundException {
        policyService.getGlobalVariable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetGlobalVariable_emptyArg()
            throws GlobalVariableNotFoundException {
        policyService.getGlobalVariable("");
    }

    @Test(expected = GlobalVariableNotFoundException.class)
    public void testGetGlobalVariable_missingGlobal()
            throws GlobalVariableNotFoundException {
        policyService.getGlobalVariable("notavar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGlobalVariable_null1stArg() {
        policyService.setGlobalVariable(null, new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGlobalVariable_empty1stArg() {
        policyService.setGlobalVariable("", new Object());
    }
}
