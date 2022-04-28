package com.google.gcs.plugin;


import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GoogleCredentialsRepositoryTest {


    @Test
    public void constructorTest() {
        assertNotNull(new GoogleCredentialsRepository(mock(CredentialsLifecycleHandler.class)));
    }
}
