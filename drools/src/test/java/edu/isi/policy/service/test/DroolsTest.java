package edu.isi.policy.service.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.isi.policy.service.PolicyService;
import edu.isi.policy.service.PolicyServiceFactory;

abstract public class DroolsTest {

    protected PolicyService createPolicyService(String rulesFile) {
        Logger logger = Logger.getLogger(PolicyService.class);
        Map<String, Object> globalVariables = new HashMap<String, Object>();
        globalVariables.put("logger", logger);
        globalVariables.put("third_party_transfer_parallel_default", "4");
        Collection<String> policyRuleFiles = new ArrayList<String>();
        if (rulesFile != null) {
            policyRuleFiles.add(rulesFile);
        } else {
            policyRuleFiles.add("default.drl");
        }

        final PolicyService policyService = PolicyServiceFactory
                .createPolicyService(globalVariables, policyRuleFiles);
        return policyService;
    }
}
