package edu.isi.policy.service;

import java.util.Collection;
import java.util.Map;

import edu.isi.policy.service.drools.DroolsStatefulPolicySession;

/**
 * Factory for creating a policy service
 * 
 * @author David Smith
 * 
 */
public class PolicyServiceFactory {

    /**
     * Don't allow factory to be instantiated
     */
    private PolicyServiceFactory() {
    }

    /**
     * Creates a policy service using a drools stateful policy session
     * 
     * @param globalVariables
     *            the global variables that are defined for the session
     * @param policyRuleFiles
     *            the policy rule files that should be used for the session
     * @return a new policy service instance
     */
    public static PolicyService createPolicyService(
            final Map<String, Object> globalVariables,
            final Collection<String> policyRuleFiles) {
        final PolicyService policyService = new PolicyServiceImpl();
        final PolicySession policySession = new DroolsStatefulPolicySession(
                globalVariables, policyRuleFiles);
        policyService.setPolicySession(policySession);
        return policyService;
    }
}
