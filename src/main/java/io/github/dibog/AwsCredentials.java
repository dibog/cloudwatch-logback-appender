package io.github.dibog;

import com.amazonaws.auth.AWSCredentials;

public class AwsCredentials implements AWSCredentials{
    private String accessKeyId;
    private String secretAccessKey;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    @Override
    public String getAWSAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
        return secretAccessKey;
    }
}

