package io.github.dibog;

import ch.qos.logback.core.spi.ContextAwareBase;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

public class AwsProxyConfig extends ContextAwareBase {
    private ProxyConfiguration.Builder builder = ProxyConfiguration.builder();

    public void setEndpoint(String aEndpoint) {
        addInfo("endpoint = "+aEndpoint);
        builder.endpoint(URI.create(aEndpoint));
    }

    public void setNonProxies(String aNonProxies) {
        addInfo("nonProxies = "+aNonProxies);
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
        addInfo("ntlmDomain = "+aProxyDomain);
        builder.ntlmDomain(aProxyDomain);
    }

    public void setNtlmWorkstation(String aProxyWorkstation) {
        addInfo("ntlmWorkstation = "+aProxyWorkstation);
        builder.ntlmWorkstation(aProxyWorkstation);
    }

    public void setPassword(String aPassword) {
        addInfo("password = *********");
        builder.password(aPassword);
    }

    public void setPreemptiveBasicAuthenticationEnabled(boolean aPreemptiveBasicAuthenticationEnabled) {
        addInfo("preemptiveBasicAuthenticationEnabled = "+aPreemptiveBasicAuthenticationEnabled);
        builder.preemptiveBasicAuthenticationEnabled(aPreemptiveBasicAuthenticationEnabled);
    }

    public void setUsername(String aUsername) {
        addInfo("username = "+aUsername);
        builder.username(aUsername);
    }

    public void setUseSystemPropertyValues(boolean aUseSystemPropertyValues) {
        addInfo("useSystemPropertyValues = "+aUseSystemPropertyValues);
        builder.useSystemPropertyValues(aUseSystemPropertyValues);
    }

    public ProxyConfiguration build() {
        return builder.build();
    }
}
