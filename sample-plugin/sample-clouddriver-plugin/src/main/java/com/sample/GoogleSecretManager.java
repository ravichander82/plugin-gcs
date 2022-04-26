package com.sample;

import com.google.cloud.secretmanager.v1beta1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1beta1.SecretVersionName;
import com.google.cloud.secretmanager.v1beta1.SecretManagerServiceClient;
import com.netflix.spinnaker.kork.secrets.EncryptedSecret;
import com.netflix.spinnaker.kork.secrets.SecretDecryptionException;
import com.netflix.spinnaker.kork.secrets.SecretException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class GoogleSecretManager {

    public String decrypt(EncryptedSecret encryptedSecret) {

        String projectNumber = encryptedSecret.getParams().get("p");
        String secretId = encryptedSecret.getParams().get("s");

        SecretManagerServiceClient client = null;
        try {
            client = SecretManagerServiceClient.create();
            SecretVersionName secretVersionName =
                    SecretVersionName.of(projectNumber, secretId, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            String value = response.getPayload().getData().toStringUtf8();
            return value;
        } catch (IOException e) {
            throw new SecretException(
                    String.format(
                            "Failed to parse secret when using Google Secrets Manager to fetch: [projectNumber: %s, secretId: %s]",
                            projectNumber, secretId),
                    e);
        }
        finally {
            client.close();
        }
    }


    public Path createTempFile(String prefix, byte[] decryptedContents) {
        try {
        File tempFile = File.createTempFile(prefix, ".secret");
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        bufferedOutputStream.write(decryptedContents);

        tempFile.deleteOnExit();
        bufferedOutputStream.close();
        fileOutputStream.close();

        return tempFile.toPath();
    } catch (IOException e) {
        throw new SecretDecryptionException(e.getMessage());
    }
}

}
