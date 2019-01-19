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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

class LoggingEventToStringImpl implements LoggingEventToString {
    private final ObjectMapper m = new ObjectMapper();

    @Override
    public String map(ILoggingEvent event) {

        Map<String,Object> values = new HashMap<>();

        values.put("message", event.getFormattedMessage());
        values.put("level", event.getLevel().levelStr);
        values.put("logger-name", event.getLoggerName());
        values.put("thread-name", event.getThreadName());

        Marker marker = event.getMarker();
        if(marker!=null)
            values.put("marker", marker);

        IThrowableProxy exceptionProxy = event.getThrowableProxy();
        if(exceptionProxy!=null) {
            values.put("exception", ExceptionUtil.toString(exceptionProxy));
        }

        // In theory, event.getMDCPropertyMap() should not be null, in practice it can
        if (event.getMDCPropertyMap() != null && !event.getMDCPropertyMap().isEmpty()) {
            values.put("context", event.getMDCPropertyMap());
        }

        try {
            return m.writeValueAsString(values);
        }
        catch(Exception e) {
            return e.getLocalizedMessage();
        }
    }
}