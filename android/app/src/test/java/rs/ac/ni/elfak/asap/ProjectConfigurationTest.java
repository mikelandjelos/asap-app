package rs.ac.ni.elfak.asap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ProjectConfigurationTest {

    @Test
    public void applicationNamespace_isStable() {
        assertEquals("rs.ac.ni.elfak.asap", BuildConfig.APPLICATION_ID);
    }
}
