package net.bogdoll;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;

import java.util.Map;

class SkippedEvent implements ILoggingEvent {
    private final int no;

    public SkippedEvent(int aMessages) {
        no = aMessages;
    }

    @Override
    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    @Override
    public Level getLevel() {
        return Level.WARN;
    }

    @Override
    public String getMessage() {
        return "Skipped "+no+" messages in the last log cycle.";
    }

    @Override
    public Object[] getArgumentArray() {
        return null;
    }

    @Override
    public String getFormattedMessage() {
        return getMessage();
    }

    @Override
    public String getLoggerName() {
        return "AWS Cloud Watch Logger";
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return null;
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return null;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return null;
    }

    @Override
    public boolean hasCallerData() {
        return false;
    }

    @Override
    public Marker getMarker() {
        return null;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return null;
    }

    @Override
    public Map<String, String> getMdc() {
        return null;
    }

    @Override
    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

    @Override
    public void prepareForDeferredProcessing() { }
}