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

import software.amazon.awssdk.http.apache.ProxyConfiguration;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

public class AwsProxyConfig {
    private ProxyConfiguration.Builder builder = ProxyConfiguration.builder();
    private StringHelper toString = new StringHelper("ProxyConfiguration");

    public void setEndpoint(String aEndpoint) {
        toString.add("endpoint", aEndpoint);
        builder.endpoint(URI.create(aEndpoint));
    }

    public void setNonProxies(String aNonProxies) {
        toString.add("nonProxies", aNonProxies);
        String[] nonProxies = aNonProxies.split("[,;|]");
        for(int i=0, size=nonProxies.length;i<size; ++i) {
            String nonProxy = nonProxies[i];
            if(nonProxy.charAt(0)=='*') {
                nonProxies[i] = nonProxy.substring(1);
            }
        }

        builder.nonProxyHosts(new HashSet<>(Arrays.asList(nonProxies)));
    }

    public void setNtlmDomain(String aProxyDomain) {
        toString.add("htlmDomain", aProxyDomain);
        builder.ntlmDomain(aProxyDomain);
    }

    public void setNtlmWorkstation(String aProxyWorkstation) {
        toString.add("htlmWorkstation", aProxyWorkstation);
        builder.ntlmWorkstation(aProxyWorkstation);
    }

    public void setPassword(String aPassword) {
        toString.add("password","*********");
        builder.password(aPassword);
    }

    public void setPreemptiveBasicAuthenticationEnabled(boolean aPreemptiveBasicAuthenticationEnabled) {
        toString.add("preemptiveBasicAuthenticationEnabled", aPreemptiveBasicAuthenticationEnabled);
        builder.preemptiveBasicAuthenticationEnabled(aPreemptiveBasicAuthenticationEnabled);
    }

    public void setUsername(String aUsername) {
        toString.add("username", aUsername);
        builder.username(aUsername);
    }

    public void setUseSystemPropertyValues(boolean aUseSystemPropertyValues) {
        toString.add("useSystemPropertyValues", aUseSystemPropertyValues);
        builder.useSystemPropertyValues(aUseSystemPropertyValues);
    }

    public String toString() {
        return toString.toString();
    }

    public ProxyConfiguration build() {
        return builder.build();
    }
}
