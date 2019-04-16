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
    private final StringHelper toString = new StringHelper(this);

    public void setRegion(final String region) {
        toString.add("region", region);
        builder.region(Region.of(region));
    }

    public void setProfileName(final String profileName) {
        toString.add("profileName", profileName);
        builder.credentialsProvider(ProfileCredentialsProvider.builder().profileName(profileName).build());
    }

    public void setCredentials(final AwsCredentials credentials) {
        toString.add("credentials", credentials.toString());
        builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
    }

    public void setHttpClient(final AwsApacheHttpClient httpClient) {
        toString.add("httpClient", httpClient.toString());
        builder.httpClientBuilder(httpClient.builder());
    }

    public String toString() {
        return toString.toString();
    }

    public CloudWatchLogsClient createAwsLogs() {
        return builder.build();
    }
}
