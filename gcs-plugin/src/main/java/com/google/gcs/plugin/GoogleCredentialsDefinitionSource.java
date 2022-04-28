package com.google.gcs.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.netflix.spinnaker.clouddriver.google.config.GoogleConfigurationProperties;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import com.netflix.spinnaker.kork.secrets.EncryptedSecret;
import com.netflix.spinnaker.kork.secrets.SecretManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoogleCredentialsDefinitionSource implements CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount> {

    private static final GcsSource gcsSource = new GcsSource();

    @Autowired
    private GCSConfig config;

    @Autowired
    private SecretManager secretManager;

    @Override
    public List<GoogleConfigurationProperties.ManagedAccount> getCredentialsDefinitions() {

        List<GoogleConfigurationProperties.ManagedAccount> googleCredentialsDefinitions =
                new ArrayList<>();

        InputStream bucket = gcsSource.downloadRemoteFile(config.getGcsBucketName(), config.getFileName());

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(bucket);

        HashMap map = (HashMap) data.get("google");
        ArrayList accountsList = (ArrayList) map.get("accounts");

        ObjectMapper mapper = new ObjectMapper();

        for( int i =0 ;i<accountsList.size();i++) {
            GoogleConfigurationProperties.ManagedAccount managedAccount = mapper.convertValue(accountsList.get(i),GoogleConfigurationProperties.ManagedAccount.class);
            String jsonPath = managedAccount.getJsonPath();
            if(EncryptedSecret.isEncryptedSecret(jsonPath)){
                System.out.println(" JsonPath is encrypted secret ");
                managedAccount.setJsonPath(secretManager.decryptAsFile(managedAccount.getJsonPath()).toString());
            }
            googleCredentialsDefinitions.add(managedAccount);
        }

        return ImmutableList.copyOf(googleCredentialsDefinitions);
    }
}
