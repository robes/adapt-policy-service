package edu.isi.policy.adapt.test;

import java.io.IOException;

import org.junit.Test;

import edu.isi.policy.adapt.PolicyModule;

public class PolicyModuleTest {

    private final String propertiesFilename = PolicyModule.class.getResource(
            "/policymodule.properties").getPath();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArg0() throws IOException {
        new PolicyModule((String) null);
    }

    @Test
    public void testConstructor1() throws IOException {
        new PolicyModule(propertiesFilename);
    }

    @Test
    public void testConstructor2() throws IOException {
        new PolicyModule();
    }
}
