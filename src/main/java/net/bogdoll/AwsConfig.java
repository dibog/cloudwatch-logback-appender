package net.bogdoll;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;

public class AwsConfig {
    private ClientConfiguration clientConfig;
    private AwsCredentials credentials;
    private String region;

    public void setCredentials(AwsCredentials credentials) {
        this.credentials = credentials;
    }

    public void setClientConfig(ClientConfiguration clientConfig) {
        this.clientConfig = clientConfig;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public AWSLogs createAWSLogs() {
        AWSLogsClientBuilder builder = AWSLogsClientBuilder
                .standard()
                .withRegion(region);

        if(clientConfig!=null) {
                builder.withClientConfiguration(new ClientConfiguration());
        }

        if(credentials!=null) {
            builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        }

        return builder.build();
    }
}
