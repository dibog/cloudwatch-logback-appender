package net.bogdoll;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.spi.ContextAware;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Marker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

class AwsCWEventDump implements Runnable {

    private final RingBuffer<ILoggingEvent> queue = new RingBuffer<ILoggingEvent>(10);
    private final Function<ILoggingEvent,String> layout;
    private final AwsConfig awsConfig;
    private final boolean createLogGroup;
    private final String groupName;
    private final String streamName;
    private final DateFormat dateFormat;
    private final ContextAware logContext;
    private final Date dateHolder = new Date();
    private final PutLogEventsRequest logEventReq;

    private volatile boolean done = false;

    private AWSLogs awsLogs;
    private String currentStreamName = null;
    private String nextToken = null;

    public AwsCWEventDump( AwsLogAppender aAppender ) {
        logContext = requireNonNull(aAppender, "appender");
        awsConfig = requireNonNull(aAppender.awsConfig, "appender.awsConfig");
        createLogGroup = aAppender.createLogGroup;
        groupName = requireNonNull(aAppender.groupName, "appender.groupName");
        logEventReq = new PutLogEventsRequest().withLogGroupName(groupName);
        streamName = requireNonNull(aAppender.streamName, "appender.streamName");

        if(aAppender.layout==null) {
            layout = new LoggingEventToString();
        }
        else {
            layout = (ILoggingEvent event) -> aAppender.layout.doLayout(event);
        }

        if(aAppender.dateFormat==null || aAppender.dateFormat.trim().isEmpty()) {
            dateFormat = null;
        }
        else {
            dateFormat = new SimpleDateFormat(aAppender.dateFormat);
        }
    }

    private void closeStream() {
        currentStreamName = null;
    }

    private void openStream(String aNewStreamName) {

        if(awsLogs==null) {
            try {
                awsLogs = awsConfig.createAWSLogs();
            }
            catch(Exception e) {
                logContext.addError("Exception while opening AWSLogs. Shutting down the cloud watch logger.", e);
                shutdown();
            }
        }

        if(createLogGroup) {
            if (findLogGroup(groupName)==null) {
                awsLogs.createLogGroup(new CreateLogGroupRequest(groupName));
            }
        }

        LogStream stream = findLogStream(groupName, aNewStreamName);
        if(stream==null) {
            try {
                awsLogs.createLogStream(new CreateLogStreamRequest(groupName, aNewStreamName));
            }
            catch(Exception e) {
                logContext.addError("Exception while creating log stream ( "+groupName+" / "+aNewStreamName+" ). Shutting down the cloud watch logger.", e);
                shutdown();
            }
            nextToken = null;
        }
        else {
            nextToken = stream.getUploadSequenceToken();
        }

        logEventReq.withLogStreamName(aNewStreamName);
        currentStreamName = aNewStreamName;

    }

    private LogGroup findLogGroup(String aName) {
        DescribeLogGroupsResult result = awsLogs.describeLogGroups(
                new DescribeLogGroupsRequest()
                        .withLogGroupNamePrefix(groupName)
        );
        for (LogGroup group : result.getLogGroups()) {
            if (group.getLogGroupName().equals(aName)) {
                return group;
            }
        }
        return null;
    }

    private LogStream findLogStream(String aGroupName, String aStreamName) {
        try {
            DescribeLogStreamsResult result = awsLogs.describeLogStreams(
                    new DescribeLogStreamsRequest(groupName)
                            .withLogStreamNamePrefix(aStreamName)
            );
            for (LogStream stream : result.getLogStreams()) {
                if (stream.getLogStreamName().equals(aStreamName)) {
                    return stream;
                }
            }
        }
        catch(Exception e) {
            logContext.addError("Exception while trying to describe log stream ( "+aGroupName+"/"+aStreamName+" ).  Shutting down the cloud watch logger.", e);
            shutdown();
        }

        return null;
    }

    private void log(Collection<ILoggingEvent> aEvents) {
        if(dateFormat!=null) {
            dateHolder.setTime(System.currentTimeMillis());
            String newStreamName = streamName + "-" + dateFormat.format(dateHolder);

            if ( !newStreamName.equals(currentStreamName) ) {
                closeStream();
                openStream(newStreamName);
            }
        }
        else if (awsLogs==null) {
            closeStream();
            openStream(streamName);
        }

        Collection<InputLogEvent> events =  new ArrayList<>(aEvents.size());
        for(ILoggingEvent event : aEvents) {

            events.add( new InputLogEvent()
                    .withTimestamp(event.getTimeStamp())
                    .withMessage(layout.apply(event)) );
        }

        try {
            nextToken = awsLogs.putLogEvents(
                    logEventReq
                            .withSequenceToken(nextToken)
                            .withLogEvents(events)
            ).getNextSequenceToken();
        }
        catch(Exception e) {
            logContext.addError("Exception while adding log events.", e);
        }
    }

    public void shutdown() {
        done = true;
    }

    public void queue(ILoggingEvent event) {
        queue.put(event);
    }

    public void run() {
        List<ILoggingEvent> collections = new LinkedList<ILoggingEvent>();
        while(!done) {

            try {
                int[] nbs = queue.drainTo(collections);
                int msgProcessed = nbs[0];
                int msgSkipped = nbs[1];
                if(msgSkipped>0) {
                    collections.add(new SkippedEvent(msgSkipped));
                }
                log(collections);
                collections.clear();
            }
            catch(InterruptedException e) {
                // ignoring
            }
        }
    }
}

