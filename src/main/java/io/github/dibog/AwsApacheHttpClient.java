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
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

public class AwsApacheHttpClient extends ContextAwareBase {
    private ApacheHttpClient.Builder builder = ApacheHttpClient.builder();
    private StringHelper toString = new StringHelper("HttpClient");

    public void setConnectionAcquisitionTimeout(long aTimeOut) {
        toString.add("connectionAcquisitionTimeout", aTimeOut);
        builder.connectionAcquisitionTimeout(Duration.ofMillis(aTimeOut));
    }

    public void setConnectionMaxIdleTime(long aTimeOut) {
        toString.add("connectionMaxIdleTime", aTimeOut);
        builder.connectionMaxIdleTime(Duration.ofMillis(aTimeOut));
    }

    public void setConnectionTimeout(long aTimeOut) {
        toString.add("connectionTimeout", aTimeOut);
        builder.connectionTimeout(Duration.ofMillis(aTimeOut));
    }

    public void setConnectionTimeToLive(long aTimeOut) {
        toString.add("connectionTimeToLive", aTimeOut);
        builder.connectionTimeToLive(Duration.ofMillis(aTimeOut));
    }

    public void setExpectContinueEnabled(boolean aEnabled) {
        toString.add("expectContinueEnabled", aEnabled);
        builder.expectContinueEnabled(aEnabled);
    }

    public void setLocalAddress(String aLocalAddress) {
        try {
            builder.localAddress(InetAddress.getByName(aLocalAddress));
            toString.add("localAddress", aLocalAddress);
        }
        catch(UnknownHostException e) {
            addError("'"+aLocalAddress+"' can not be converted to an URI", e);
        }
    }

    public void setMaxConnections(int aMaxConnections) {
        toString.add("maxConnections", aMaxConnections);
        builder.maxConnections(aMaxConnections);
    }

    public void setSocketTimeout(long aTimeOut) {
        toString.add("socketTimeout", aTimeOut);
        builder.socketTimeout(Duration.ofMillis(aTimeOut));
    }

    public void setUseIdleConnectionReaper(boolean aUseConnectionReaper) {
        toString.add("useIdleConnectionReaper", aUseConnectionReaper);
        builder.useIdleConnectionReaper(aUseConnectionReaper);
    }

    public void setProxyConfig(final AwsProxyConfig proxyConfig) {
        toString.add("proxyConfig", proxyConfig.toString());
        builder.proxyConfiguration(proxyConfig.build());
    }

    @Override
    public String toString() {
        return toString.toString();
    }

    SdkHttpClient.Builder builder() {
        return builder;
    }
}
