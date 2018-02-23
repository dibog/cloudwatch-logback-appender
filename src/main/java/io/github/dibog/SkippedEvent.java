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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;

import java.util.Map;

class SkippedEvent implements ILoggingEvent {
    private final int no;
    private final LoggerContextVO loggerContextVO;

    public SkippedEvent(int aMessages, LoggerContextVO aLoggerContextV0) {
        no = aMessages;
        loggerContextVO = aLoggerContextV0;
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
        return loggerContextVO;
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