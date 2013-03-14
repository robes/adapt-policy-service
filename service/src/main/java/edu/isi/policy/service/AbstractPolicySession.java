package edu.isi.policy.service;

import java.util.Collection;
import java.util.Map;

/**
 * Common abstract parent for policy session implementations. This class can
 * manage the policy rule files and global variable definitions.
 * 
 * @author David Smith
 * 
 */
public abstract class AbstractPolicySession implements PolicySession {
    private final Map<String, Object> globalVariables;
    private final Collection<String> policyRuleFiles;

    /**
     * Constructs a policy session with global variables and policy rules
     * 
     * @param globalVariables
     * @param policyRuleFiles
     */
    public AbstractPolicySession(Map<String, Object> globalVariables,
            Collection<String> policyRuleFiles) {
        this.globalVariables = globalVariables;
        this.policyRuleFiles = policyRuleFiles;
    }

    /**
     * 
     * @return the map of global variables defined
     */
    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    /**
     * 
     * @return the collection of policy rules defined
     */
    public Collection<String> getPolicyRuleFiles() {
        return policyRuleFiles;
    }
}
