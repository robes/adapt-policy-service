package edu.isi.policy.adapt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.isi.policy.service.PolicyServiceImpl;
import edu.isi.policy.service.PolicySession;
import edu.isi.policy.service.drools.DroolsStatefulPolicySession;
import edu.isi.policy.util.ResourceExpressionPairFloatMap;
import edu.isi.policy.util.ResourceExpressionPairIntegerMap;

/**
 * Convenience constructor for the policy module used in the ADAPT project
 * 
 * @author David Smith
 * 
 */
public class PolicyModule extends PolicyServiceImpl {

    private static final String DEFAULT_PROPERTIES_FILE = "/policymodule.properties";

    private final Map<String, Object> globalVariables;
    private static final Logger LOG = Logger.getLogger(PolicyModule.class);

    public static final String PTM_KEY = "passiveTransferMonitor";
    public static final String TRANSFER_STATISTICS_LOG_KEY = "transferStatisticsLog";
    public static final String DEFAULT_MAX_STREAMS_KEY = "default_max_streams";
    public static final String DEFAULT_MAX_RATE_KEY = "default_max_rate";
    public static final String MAX_BANDWIDTH_KEY = "maxBandwidth";
    public static final String MAX_PARALLEL_STREAMS_KEY = "maxParallelStreams";
    public static final String RESOURCE_ALLOCATION_LOG_KEY = "resourceAllocationLog";
    public static final String POLICY_FILE_KEY = "policyFile";
    public static final String LOGGER_TYPE_KEY = "logger";
    public static final String LOG4J_LOGGER_TYPE = "log4j";
    public static final String PTM_LOG_KEY = "passiveTransferMonitorLogFile";
    public static final String PTM_POLICY_FILE_KEY = "passiveTransferMonitorPolicyFile";

    /**
     * Default constructor. Loads with the default properties.
     */
    public PolicyModule() throws IOException {
        this(PolicyModule.class.getResource(DEFAULT_PROPERTIES_FILE).getFile());
    }

    /**
     * Constructs a policy module with a custom properties file.
     * 
     * @param filename
     *            properties filename that contains the properties for the
     *            policy module
     * @param ptm
     *            the passive transfer monitor that the policy module will
     *            interface with
     * @throws IOException
     *             if the properties file cannot be read
     */
    public PolicyModule(String filename)
            throws IOException {
        if (filename == null || filename.length() == 0) {
            throw new IllegalArgumentException("Filename must be specified.");
        }

        final BufferedReader reader = new BufferedReader(new FileReader(
                new File(filename)));
        final Properties properties = new Properties();
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!(line.length() == 0 || line.startsWith("#"))) {
                String[] pieces = line.split("=");
                if (pieces.length == 2) {
                    properties.setProperty(pieces[0], pieces[1]);
                } else if (pieces.length == 1) {
                    properties.setProperty(pieces[0], "");
                }
            }
        }
        reader.close();
        globalVariables = new HashMap<String, Object>();
        createPolicySession(properties);
    }

    public PolicyModule(Properties properties) {
        globalVariables = new HashMap<String, Object>();
        createPolicySession(properties);
    }

    private void createPolicySession(Properties properties) {
        List<String> policyRuleFiles = new ArrayList<String>();
        if (properties.containsKey(POLICY_FILE_KEY)) {
            policyRuleFiles.add(properties.getProperty(POLICY_FILE_KEY));
        }
        if (properties.containsKey(PTM_KEY)) {
            try {
                final Object ptm = getClass().getClassLoader()
                        .loadClass(properties.getProperty(PTM_KEY))
                        .getConstructor((Class[]) null).newInstance();
                globalVariables.put(PTM_KEY, ptm);
                if (properties.containsKey(PTM_LOG_KEY)) {
                    if (((gov.lbl.srm.client.main.IPassiveTransferMonitor) ptm)
                            .browseLogFile(properties.getProperty(PTM_LOG_KEY))) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Initialized passive transfer monitor statistics from file "
                                    + properties.getProperty(PTM_LOG_KEY));
                        }
                    }
                }
                if (properties.containsKey(PTM_POLICY_FILE_KEY)) {
                    policyRuleFiles.add(properties
                            .getProperty(PTM_POLICY_FILE_KEY));
                }
            } catch (Exception e) {
                LOG.warn("Could not construct passive transfer monitor from class "
                        + properties.getProperty(PTM_KEY));
            }
        }


        if (properties.containsKey(TRANSFER_STATISTICS_LOG_KEY)) {
            try {
                globalVariables
                .put(TRANSFER_STATISTICS_LOG_KEY,
                        Class.forName(
                                properties
                                .getProperty(TRANSFER_STATISTICS_LOG_KEY))
                                .newInstance());
            } catch (Exception e) {
                LOG.warn("Could not instantiate transfer statistics log from class "
                        + properties.getProperty(TRANSFER_STATISTICS_LOG_KEY));
            }
        }
        if (properties.containsKey(DEFAULT_MAX_STREAMS_KEY)) {
            globalVariables.put(DEFAULT_MAX_STREAMS_KEY, Integer
                    .parseInt(properties.getProperty(DEFAULT_MAX_STREAMS_KEY)));
        }
        if (properties.containsKey(DEFAULT_MAX_RATE_KEY)) {
            globalVariables.put(DEFAULT_MAX_RATE_KEY, Float
                    .parseFloat(properties.getProperty(DEFAULT_MAX_RATE_KEY)));
        }
        if (properties.containsKey(MAX_BANDWIDTH_KEY)) {
            try {
                final InputStream inputStream = getFileInputStream(
                        properties.getProperty(MAX_BANDWIDTH_KEY));
                ResourceExpressionPairFloatMap maxBandwidth = new ResourceExpressionPairFloatMap(
                        inputStream);
                inputStream.close();
                globalVariables.put(MAX_BANDWIDTH_KEY, maxBandwidth);
            } catch (Exception e) {
                LOG.warn("Could not populate max bandwidth map.");
            }
        }
        if (properties.containsKey(MAX_PARALLEL_STREAMS_KEY)) {
            try {
                final InputStream inputStream = getFileInputStream(
                        properties.getProperty(MAX_PARALLEL_STREAMS_KEY));
                ResourceExpressionPairIntegerMap maxStreams = new ResourceExpressionPairIntegerMap(
                        inputStream);
                inputStream.close();
                globalVariables.put(MAX_PARALLEL_STREAMS_KEY, maxStreams);
            } catch (Exception e) {
                LOG.warn("Could not populate max bandwidth map.");
            }
        }
        if (properties.containsKey(RESOURCE_ALLOCATION_LOG_KEY)) {
            try {
                ResourceAllocationLog resourceAllocationLog = (ResourceAllocationLog) Class
                        .forName(
                                properties
                                .getProperty(RESOURCE_ALLOCATION_LOG_KEY))
                                .getConstructor(Properties.class)
                                .newInstance(properties);
                resourceAllocationLog.open();
                globalVariables.put(RESOURCE_ALLOCATION_LOG_KEY,
                        resourceAllocationLog);
            } catch (Exception e) {
                LOG.warn("Could not construct the resource allocation log.", e);
            }
        }
        if (properties.containsKey(LOGGER_TYPE_KEY)) {
            if (LOG4J_LOGGER_TYPE.equals(properties
                    .getProperty(LOGGER_TYPE_KEY))) {
                Logger logger = Logger.getLogger(getClass());
                globalVariables.put(LOGGER_TYPE_KEY, logger);
            }
        }

        final PolicySession policySession = new DroolsStatefulPolicySession(
                globalVariables, policyRuleFiles);
        super.setPolicySession(policySession);
    }

    private InputStream getFileInputStream(String filename)
            throws FileNotFoundException {
        InputStream stream = null;
        if (new File(filename).exists()) {
            stream = new FileInputStream(filename);
        } else {
            stream = this.getClass().getClassLoader()
                    .getResourceAsStream(filename);
        }
        return stream;
    }

    @Override
    public void finalize() {
        ResourceAllocationLog resourceAllocationLog = (ResourceAllocationLog) globalVariables
                .get(RESOURCE_ALLOCATION_LOG_KEY);
        if (resourceAllocationLog != null) {
            resourceAllocationLog.close();
        }
    }
}
