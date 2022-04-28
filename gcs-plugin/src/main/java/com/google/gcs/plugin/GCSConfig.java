package com.google.gcs.plugin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("gcsbucketproperties")
public class GCSConfig {

    String gcsBucketName;
    String fileName;

}