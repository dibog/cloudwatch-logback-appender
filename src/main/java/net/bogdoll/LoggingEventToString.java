package net.bogdoll;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class LoggingEventToString implements Function<ILoggingEvent, String> {
    private final ObjectMapper m = new ObjectMapper();

    @Override
    public String apply(ILoggingEvent event) {

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

        try {
            return m.writeValueAsString(values);
        }
        catch(Exception e) {
            return e.getLocalizedMessage();
        }
    }
}