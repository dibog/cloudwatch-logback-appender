/*
 * Copyright 2018  Dieter Bogdoll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.dibog;

import ch.qos.logback.core.spi.ContextAwareBase;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;

public class AwsConfig extends ContextAwareBase {

    private final CloudWatchLogsClientBuilder builder = CloudWatchLogsClient.builder();

    public void setRegion(final String region) {
        addInfo("region = "+region);
        builder.region(Region.of(region));
    }

    public void setProfileName(final String profileName) {
        addInfo("profileName = "+profileName);
        builder.credentialsProvider(ProfileCredentialsProvider.builder().profileName(profileName).build());
    }

    public void setCredentials(final AwsCredentials credentials) {
        builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
    }

    public void setHttpClient(final AwsApacheHttpClient httpClient) {
        builder.httpClientBuilder(httpClient.builder());
    }

    public CloudWatchLogsClient createAwsLogs() {
        return builder.build();
    }
}
