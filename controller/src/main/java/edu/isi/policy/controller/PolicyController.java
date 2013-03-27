package edu.isi.policy.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Resource;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.service.PolicyService;
import edu.isi.policy.util.CleanupList;
import edu.isi.policy.util.TransferList;

/**
 * Controller used to communicate with the Policy Service.
 * 
 * @author David Smith
 * 
 */
@Controller
public class PolicyController {

    private static final Logger LOG = Logger.getLogger(PolicyController.class);
    private final Object policyLock = new Object();

    @Autowired
    private PolicyService policyService;
    
    /**
     * 
     * @return the policy service
     */
    public PolicyService getPolicyService() {
        return policyService;
    }

    /**
     * Sets the policy service
     * 
     * @param policyService
     *            the policy service
     */
    public void setPolicyService(PolicyService policyService) {
        if (policyService == null) {
            throw new IllegalArgumentException(
                    "Policy service must be specified");
        }
        this.policyService = policyService;
    }

    /**
     * Retrieves the list of current transfers that are known by the policy
     * service
     * 
     * @return
     */
    @RequestMapping(value = "/transfer/list", method = RequestMethod.GET)
    public @ResponseBody
    TransferList getTransfers() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: GET /transfer/list");
        }
        final TransferList transfers;
        synchronized (this.policyLock) {
            transfers = policyService.getTransfers();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: GET /transfer/list; transfers=" + transfers);
        }
        return transfers;
    }

    /**
     * Adds a list of transfers to the policy service
     * 
     * @param transfers
     * @return a list of transfers that are sent back from the policy service
     */
    @RequestMapping(value = "/transfer/list", method = RequestMethod.POST)
    public @ResponseBody
    TransferList addTransfers(@RequestBody TransferList transfers) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: POST /transfer/list; transfers=" + transfers);
        }
        final TransferList result;
        synchronized (this.policyLock) {
            result = policyService.addTransfers(transfers);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: POST /transfer/list; transfers=" + result);
        }
        return result;
    }

    /**
     * Updates a list of transfers that exist in the policy service
     * 
     * @param transfers
     *            the transfers to update
     * @return the updated transfers
     */
    @RequestMapping(value = "/transfer/list", method = RequestMethod.PUT)
    public @ResponseBody
    TransferList updateTransfers(@RequestBody TransferList transfers) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: PUT /transfer/list; transfers=" + transfers);
        }
        final TransferList result;
        synchronized (this.policyLock) {
            result = policyService.updateTransfers(transfers);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: PUT /transfer/list; transfers=" + result);
        }
        return result;
    }

    /**
     * Adds a transfer to the policy service
     * 
     * @param transfer
     * @return the transfer that was sent back from the policy service
     */
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public @ResponseBody
    Transfer addTransfer(@RequestBody Transfer transfer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: POST /transfer; transfer=" + transfer);
        }
        final Transfer result;
        synchronized (this.policyLock) {
            result = policyService.addTransfer(transfer);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: POST /transfer; transfer=" + result);
        }
        return result;
    }

    /**
     * Removes a transfer from the policy service
     * 
     * @param transferId
     *            unique identifier of the transfer
     */
    @RequestMapping(value = "/transfer/{transferId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void removeTransfer(@PathVariable String transferId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: DELETE /transfer/" + transferId);
        }
        try {
            synchronized (this.policyLock) {
                policyService.removeTransfer(transferId);
            }
        } catch (EntityNotFoundException e) {
            LOG.warn(e);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: DELETE /transfer/" + transferId);
        }
    }

    /**
     * Retrieves a single transfer
     * 
     * @param transferId
     *            unique identifier of the transfer
     * @return the transfer
     */
    @RequestMapping(value = "/transfer/{transferId}", method = RequestMethod.GET)
    public @ResponseBody
    Transfer getTransfer(@PathVariable String transferId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: GET /transfer/" + transferId);
        }
        Transfer result = null;
        try {
            synchronized (this.policyLock) {
                result = policyService.getTransfer(transferId);
            }
        } catch (EntityNotFoundException e) {
            LOG.warn(e);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: GET /transfer/" + transferId + "; transfer="
                    + result);
        }
        return result;
    }

    /**
     * Updates an existing transfer
     * 
     * @param transferId
     *            the ID of the transfer
     * @param newTransfer
     *            the updated transfer object
     * @return the updated transfer object after being evaluated by policy
     */
    @RequestMapping(value = "/transfer/{transferId}", method = RequestMethod.PUT)
    public @ResponseBody
    Transfer updateTransfer(@PathVariable String transferId,
            @RequestBody Transfer newTransfer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: PUT /transfer/" + transferId + "; transfer="
                    + newTransfer);
        }
        Transfer result = null;
        try {
            synchronized (this.policyLock) {
                policyService.updateTransfer(transferId,newTransfer);
                result = policyService.getTransfer(transferId);
            }
        } catch (EntityNotFoundException e) {
            LOG.warn(e);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: PUT /transfer/" + transferId + "; transfer="
                    + result);
        }
        return result;
    }

    /**
     * 
     * @return a list of all resources that exist in the policy service
     */
    @RequestMapping(value = "/resource/list", method = RequestMethod.GET)
    public @ResponseBody
    List<Resource> getResources() {
        synchronized (this.policyLock) {
            return policyService.getResources();
        }
    }

    /**
     * 
     * @return a list of all cleanups that exist in the policy service
     */
    @RequestMapping(value = "/cleanup/list", method = RequestMethod.GET)
    public @ResponseBody
    CleanupList getCleanups() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: GET /cleanup/list");
        }
        final CleanupList cleanups;
        synchronized (this.policyLock) {
            cleanups = policyService.getCleanups();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: GET /cleanup/list; cleanups=" + cleanups);
        }
        return cleanups;
    }

    @RequestMapping(value = "/cleanup/{cleanupId}", method = RequestMethod.GET)
    public @ResponseBody
    Cleanup getCleanup(@PathVariable String cleanupId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: GET /cleanup/" + cleanupId);
        }
        Cleanup cleanup = null;
        try {
            synchronized (this.policyLock) {
                cleanup = policyService.getCleanup(cleanupId);
            }
        } catch (EntityNotFoundException e) {
            LOG.warn(e);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: GET /cleanup/" + cleanupId + "; cleanup=" + cleanup);
        }
        return cleanup;
    }

    /**
     * Adds a cleanup request to the policy service for a resource
     * 
     * @param cleanup
     *            the cleanup
     * @return the cleanup after processed from the policy service
     */
    @RequestMapping(value = "/cleanup", method = RequestMethod.POST)
    public @ResponseBody
    Cleanup addCleanup(@RequestBody Cleanup cleanup) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: POST /cleanup; cleanup=" + cleanup);
        }
        final Cleanup result;
        synchronized (this.policyLock) {
            result = policyService.addCleanup(cleanup);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: POST /cleanup; cleanup=" + result);
        }
        return result;
    }

    /**
     * Adds a list of cleanups to the policy service
     * 
     * @param resourceCleanups
     *            the list of cleanups
     * @return the updated list of transfers that is returned after being
     *         processed from the policy service
     */
    @RequestMapping(value = "/cleanup/list", method = RequestMethod.POST)
    public @ResponseBody
    CleanupList addCleanups(
            @RequestBody CleanupList resourceCleanups) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: POST /cleanup/list; cleanups=" + resourceCleanups);
        }
        final CleanupList result;
        synchronized (this.policyLock) {
            result = policyService.addCleanups(resourceCleanups);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: POST /cleanup/list; cleanups=" + result);
        }
        return result;
    }

    /**
     * Updates a list of transfers in the policy service
     * 
     * @param cleanups
     *            the cleanups to update
     * @return the updated cleanups
     */
    @RequestMapping(value = "/cleanup/list", method = RequestMethod.PUT)
    public @ResponseBody
    CleanupList updateCleanups(@RequestBody CleanupList cleanups) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: PUT /cleanup/list; cleanups=" + cleanups);
        }
        final CleanupList result;
        synchronized (this.policyLock) {
            result = policyService.updateCleanups(cleanups);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: PUT /cleanup/list; cleanups=" + result);
        }
        return result;
    }

    /**
     * Updates an existing cleanup
     * 
     * @param cleanupId
     *            the ID of the cleanup
     * @param cleanup
     *            the new cleanup object
     * @return the updated cleanup after being processed from the policy service
     */
    @RequestMapping(value = "/cleanup/{cleanupId}", method = RequestMethod.PUT)
    public @ResponseBody
    Cleanup updateCleanup(@PathVariable String cleanupId,
            @RequestBody Cleanup cleanup) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: /cleanup/" + cleanupId + "; cleanup=" + cleanup);
        }
        Cleanup result = null;
        try {
            synchronized (this.policyLock) {
                policyService.updateCleanup(cleanupId, cleanup);
            }
        } catch (EntityNotFoundException e) {
            LOG.warn(e);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: /cleanup/" + cleanupId + "; cleanup=" + result);
        }
        return result;
    }

    @RequestMapping(value = "/global/{variableName}", method = RequestMethod.GET)
    public @ResponseBody
    Object getGlobal(@PathVariable String variableName) {
        if (variableName == null || variableName.length() == 0) {
            throw new IllegalArgumentException(
                    "Variable name must be specified.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: GET /global/" + variableName);
        }
        Object result = null;
        try {
            synchronized (this.policyLock) {
                result = policyService.getGlobalVariable(variableName);
            }
        } catch (GlobalVariableNotFoundException e) {
            LOG.warn(e);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: GET /global/" + variableName + "; global=" + result);
        }
        return result;
    }

    @RequestMapping(value = "/global/{variableName}", method = RequestMethod.PUT)
    public void setGlobal(@PathVariable String variableName,
            @RequestBody Object value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In: PUT /global/" + variableName + "; value=" + value);
        }
        synchronized (this.policyLock) {
            policyService.setGlobalVariable(variableName, value);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Out: PUT /global/" + variableName + "; value=" + value);
        }
    }
}
