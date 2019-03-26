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
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.ContextAware;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.requireNonNull;

class AwsCWEventDump implements Runnable {
    private final RingBuffer<ILoggingEvent> queue = new RingBuffer<ILoggingEvent>(10);
    private final LoggingEventToString layout;
    private final AwsConfig awsConfig;
    private final boolean createLogGroup;
    private final String groupName;
    private final String streamName;
    private final DateFormat dateFormat;
    private final ContextAware logContext;
    private final Date dateHolder = new Date();
    private final PutLogEventsRequest.Builder logEventReq;

    private volatile boolean done = false;

    private CloudWatchLogsClient awsLogs;
    private String currentStreamName = null;
    private String nextToken = null;

    public AwsCWEventDump( final AwsLogAppender aAppender ) {
        logContext = requireNonNull(aAppender, "appender");
        awsConfig = aAppender.awsConfig==null ? new AwsConfig(): aAppender.awsConfig;
        createLogGroup = aAppender.createLogGroup;
        groupName = requireNonNull(aAppender.groupName, "appender.groupName");
        logEventReq = PutLogEventsRequest.builder().logGroupName(groupName);
        streamName = requireNonNull(aAppender.streamName, "appender.streamName");

        if(aAppender.layout==null) {
            layout = new LoggingEventToStringImpl();
        }
        else {
            final Layout<ILoggingEvent> delegate = aAppender.layout;
            layout = event -> delegate.doLayout(event);
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

    private void openStream(final String aNewStreamName) {

        if(awsLogs==null) {
            try {
                awsLogs = awsConfig.createAwsLogs();
            }
            catch(final Exception e) {
                logContext.addError("Exception while opening AWSLogs. Shutting down the cloud watch logger.", e);
                shutdown();
            }
        }

        if(createLogGroup) {
            if (findLogGroup(groupName)==null) {
                logContext.addInfo("creating log group '"+groupName+"'");
                try {
                    awsLogs.createLogGroup( r -> r.logGroupName(groupName));
                }
                catch(final OperationAbortedException e) {
                    logContext.addError("couldn't create log group '"+groupName+"': "+e.getLocalizedMessage());
                }
            }
        }

        final LogStream stream = findLogStream(groupName, aNewStreamName);
        if(stream==null) {
            try {
                logContext.addInfo("creating log stream '"+streamName+"'");
                awsLogs.createLogStream(r -> r.logGroupName(groupName).logStreamName(aNewStreamName));
            }
            catch(final Exception e) {
                logContext.addError("Exception while creating log stream ( "+groupName+" / "+aNewStreamName+" ). Shutting down the cloud watch logger.", e);
                shutdown();
            }
            nextToken = null;
        }
        else {
            nextToken = stream.uploadSequenceToken();
        }

        logEventReq.logStreamName(aNewStreamName);
        currentStreamName = aNewStreamName;

    }

    private LogGroup findLogGroup(final String aName) {
        final DescribeLogGroupsResponse result = awsLogs.describeLogGroups(r -> r.logGroupNamePrefix(groupName));
        for (final LogGroup group : result.logGroups()) {
            if (group.logGroupName().equals(aName)) {
                return group;
            }
        }
        return null;
    }

    private LogStream findLogStream(final String aGroupName, final String aStreamName) {
        try {
            final DescribeLogStreamsResponse result = awsLogs.describeLogStreams(r -> r.logGroupName(groupName).logStreamNamePrefix(aStreamName));

            for (final LogStream stream : result.logStreams()) {
                if (stream.logStreamName().equals(aStreamName)) {
                    return stream;
                }
            }
        }
        catch(final Exception e) {
            logContext.addError("Exception while trying to describe log stream ( "+aGroupName+"/"+aStreamName+" ).  Shutting down the cloud watch logger.", e);
            shutdown();
        }

        return null;
    }

    private void log(final Collection<ILoggingEvent> aEvents) {
        if(dateFormat!=null) {
            dateHolder.setTime(System.currentTimeMillis());
            final String newStreamName = streamName + "-" + dateFormat.format(dateHolder);

            if ( !newStreamName.equals(currentStreamName) ) {
                logContext.addInfo("stream name changed from '"+currentStreamName+"' to '"+newStreamName+"'");
                closeStream();
                openStream(newStreamName);
            }
        }
        else if (awsLogs==null) {
            closeStream();
            openStream(streamName);
        }

        final Collection<InputLogEvent> events =  new ArrayList<>(aEvents.size());

        for(final ILoggingEvent event : aEvents) {

            if(event.getLoggerContextVO()!=null) {
                events.add(InputLogEvent.builder()
                        .timestamp(event.getTimeStamp())
                        .message(layout.map(event))
                        .build());
            }
        }

        try {
            nextToken = awsLogs.putLogEvents(
                    logEventReq
                            .sequenceToken(nextToken)
                            .logEvents(events)
                            .build()
            ).nextSequenceToken();
        }
        catch(final Exception e) {
            logContext.addError("Exception while adding log events.", e);
        }
    }

    public void shutdown() {
        done = true;
    }

    public void queue(final ILoggingEvent event) {
        queue.put(event);
    }

    @Override
    public void run() {
        final List<ILoggingEvent> collections = new LinkedList<ILoggingEvent>();
        LoggerContextVO context = null;
        while(!done) {

            try {
                final int[] nbs = queue.drainTo(collections);
                if(context==null && !collections.isEmpty()) {
                    context = collections.get(0).getLoggerContextVO();
                }

                final int msgProcessed = nbs[0];
                final int msgSkipped = nbs[1];
                if(context!=null && msgSkipped>0) {
                    collections.add(new SkippedEvent(msgSkipped, context));
                }
                log(collections);
                collections.clear();
            }
            catch(final InterruptedException e) {
                // ignoring
            }
        }
    }
}

