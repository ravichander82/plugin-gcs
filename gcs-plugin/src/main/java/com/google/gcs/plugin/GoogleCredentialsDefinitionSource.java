package com.google.gcs.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.netflix.spinnaker.clouddriver.google.config.GoogleConfigurationProperties;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import com.netflix.spinnaker.kork.secrets.EncryptedSecret;
import com.netflix.spinnaker.kork.secrets.SecretManager;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoogleCredentialsDefinitionSource implements CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount> {

    private static final Logger log = LoggerFactory.getLogger(GoogleCredentialsDefinitionSource.class);
    private static final GcsSource gcsSource = new GcsSource();
    private String PROVIDER = "google";
    private String ACCOUNTS = "accounts";

    @Autowired
    private GCSConfig config;

    @Autowired
    private SecretManager secretManager;

    @Override
    public List<GoogleConfigurationProperties.ManagedAccount> getCredentialsDefinitions() {

        List<GoogleConfigurationProperties.ManagedAccount> googleCredentialsDefinitions =
                new ArrayList<>();

        InputStream gcsData = gcsSource.downloadRemoteFile(config.getGcsBucketName(), config.getFileName());

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(gcsData);
            HashMap map = (HashMap) data.get(PROVIDER);
            ArrayList accountsList = (ArrayList) map.get(ACCOUNTS);

            ObjectMapper mapper = new ObjectMapper();

            GoogleConfigurationProperties.ManagedAccount managedAccount = null;
            for (int i = 0; i < accountsList.size(); i++) {
                managedAccount = mapper.convertValue(accountsList.get(i), GoogleConfigurationProperties.ManagedAccount.class);
                String jsonPath = managedAccount.getJsonPath();
                if (EncryptedSecret.isEncryptedSecret(jsonPath)) {
                    System.out.println(" JsonPath is encrypted secret ");
                    managedAccount.setJsonPath(secretManager.decryptAsFile(jsonPath).toString());
                }
                googleCredentialsDefinitions.add(managedAccount);
            }
        }
        catch (Exception e){
            log.debug(" Exception encountered " + e.getMessage());
        }
        return ImmutableList.copyOf(googleCredentialsDefinitions);
    }
}
