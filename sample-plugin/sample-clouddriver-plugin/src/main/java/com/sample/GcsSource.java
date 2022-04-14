package com.sample;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.auth.oauth2.GoogleCredentials;
import com.netflix.spinnaker.kork.secrets.SecretException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

    @Component
    public class GcsSource {
        private static final Logger log = LoggerFactory.getLogger(GcsSource.class);
        private static final String IDENTIFIER = "gcs";
        private static final String APPLICATION_NAME = "Spinnaker";
        private final AtomicReference<Storage> googleStorage = new AtomicReference();

        public GcsSource() {
        }

        public InputStream downloadRemoteFile(String bucketName, String objectName) {
            String bucket = bucketName;
            String objName = objectName;
            log.info("Getting contents of object {} from bucket {}", objName, bucket);

            try {
                Storage storage = getStorage();
                return storage.objects().get(bucket, objName).executeMediaAsInputStream();
            } catch (IOException var5) {
                throw new SecretException(String.format("Error reading contents of GCS. Bucket: %s, Object: %s.\nError: %s", bucket, objName, var5.toString()));
            }
        }

        public Storage getStorage() throws IOException {
            Storage storage = (Storage)this.googleStorage.get();
            if (storage == null) {
                HttpTransport httpTransport = GoogleUtils.buildHttpTransport();
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                GoogleCredentials credentials = GoogleUtils.buildGoogleCredentials();
                HttpRequestInitializer requestInitializer = GoogleUtils.setTimeoutsAndRetryBehavior(credentials);
                storage = (new Storage.Builder(httpTransport, jsonFactory, requestInitializer)).setApplicationName("Spinnaker").build();
                this.googleStorage.compareAndSet((Storage) null, storage);
            }

            return storage;
        }
    }
