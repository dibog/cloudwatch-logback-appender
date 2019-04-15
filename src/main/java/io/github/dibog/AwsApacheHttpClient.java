package io.github.dibog;

import ch.qos.logback.core.spi.ContextAwareBase;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

public class AwsApacheHttpClient extends ContextAwareBase {
    private ApacheHttpClient.Builder builder = ApacheHttpClient.builder();

    public void setConnectionAcquisitionTimeout(long aTimeOut) {
        addInfo("connectionAcquisitionTimeout = "+aTimeOut);
        builder.connectionAcquisitionTimeout(Duration.ofMillis(aTimeOut));
    }

    public void setConnectionMaxIdleTime(long aTimeOut) {
        addInfo("connectionMaxIdleTime = "+aTimeOut);
        builder.connectionMaxIdleTime(Duration.ofMillis(aTimeOut));
    }

    public void setConnectionTimeout(long aTimeOut) {
        addInfo("connectionTimeout = "+aTimeOut);
        builder.connectionTimeout(Duration.ofMillis(aTimeOut));
    }

    public void setConnectionTimeToLive(long aTimeOut) {
        addInfo("connectionTimeToLive = "+aTimeOut);
        builder.connectionTimeToLive(Duration.ofMillis(aTimeOut));
    }

    public void setExpectContinueEnabled(boolean aEnabled) {
        addInfo("expectContinueEnabled = "+aEnabled);
        builder.expectContinueEnabled(aEnabled);
    }

    public void setLocalAddress(String aLocalAddress) {
        try {
            builder.localAddress(InetAddress.getByName(aLocalAddress));
            addInfo("localAddress = "+aLocalAddress);
        }
        catch(UnknownHostException e) {
            addError("'"+aLocalAddress+"' can not be converted to an URI", e);
        }
    }

    public void setMaxConnections(int aMaxConnections) {
        addInfo("maxConnections = "+aMaxConnections);
        builder.maxConnections(aMaxConnections);
    }

    public void setSocketTimeout(long aTimeOut) {
        addInfo("socketTimeout = "+aTimeOut);
        builder.socketTimeout(Duration.ofMillis(aTimeOut));
    }

    public void setUseIdleConnectionReaper(boolean aUseConnectionReaper) {
        addInfo("useIdleConnectionReaper = "+aUseConnectionReaper);
        builder.useIdleConnectionReaper(aUseConnectionReaper);
    }

    public void setProxyConfig(final AwsProxyConfig proxyConfig) {
        builder.proxyConfiguration(proxyConfig.build());
    }

    SdkHttpClient.Builder builder() {
        return builder;
    }
}
