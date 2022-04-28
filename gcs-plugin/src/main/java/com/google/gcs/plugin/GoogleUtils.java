package com.google.gcs.plugin;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class GoogleUtils {
    private static final int CONNECT_TIMEOUT;
    private static final int READ_TIMEOUT;

    GoogleUtils() {
    }

    static GoogleCredentials buildGoogleCredentials() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        if (credentials.createScopedRequired()) {
            credentials = credentials.createScoped(Collections.singleton("https://www.googleapis.com/auth/devstorage.read_only"));
        }

        return credentials;
    }

    static HttpTransport buildHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException | GeneralSecurityException var1) {
            throw new RuntimeException("Failed to build trusted transport", var1);
        }
    }

    static HttpRequestInitializer setTimeoutsAndRetryBehavior(GoogleCredentials credentials) {
        return new HttpCredentialsAdapter(credentials) {
            public void initialize(HttpRequest request) throws IOException {
                super.initialize(request);
                request.setConnectTimeout(GoogleUtils.CONNECT_TIMEOUT);
                request.setReadTimeout(GoogleUtils.READ_TIMEOUT);
                HttpBackOffUnsuccessfulResponseHandler unsuccessfulResponseHandler = new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff());
                unsuccessfulResponseHandler.setBackOffRequired(HttpBackOffUnsuccessfulResponseHandler.BackOffRequired.ON_SERVER_ERROR);
                request.setUnsuccessfulResponseHandler(unsuccessfulResponseHandler);
            }
        };
    }

    static {
        CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20L);
        READ_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(20L);
    }
}
