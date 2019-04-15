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
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class AwsLogAppender extends AppenderBase<ILoggingEvent> {

    private AwsCWEventDump dump;

    AwsConfig awsConfig;
    String groupName;
    boolean createLogGroup = true;
    String streamName;
    String dateFormat;
    int queueLength = 500;
    Layout<ILoggingEvent> layout;

    public void setAwsConfig(AwsConfig config) {
        this.awsConfig = config;
        addInfo("awsConfig = "+config);
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setGroupName(String groupName) {
        addInfo("groupName = "+groupName);
        this.groupName = groupName;
    }

    public void setStreamName(String streamName) {
        addInfo("streamName = "+streamName);
        this.streamName = streamName;
    }

    public void setDateFormat(String dateFormat) {
        addInfo("dateFormat = "+dateFormat);
        this.dateFormat = dateFormat;
    }
    
    public void setQueueLength(int aLength) {
        addInfo("queueLength = "+aLength);
        queueLength = aLength;
    }

    public void setCreateLogGroup(boolean createLogGroup) {
        addInfo("createLogGroup = "+createLogGroup);
        this.createLogGroup = createLogGroup;
    }

    @Override
    protected void append(ILoggingEvent event) {

        AwsCWEventDump queue = dump;
        if (queue != null) {
            event.prepareForDeferredProcessing();
            queue.queue(event);
        }

    }

    @Override
    public void start() {
        dump = new AwsCWEventDump(this );

        Thread t = new Thread(dump);
        t.setDaemon(true);
        t.start();

        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if(dump!=null) {
            // flush it
            dump.shutdown();
        }
        dump = null;
    }
}
