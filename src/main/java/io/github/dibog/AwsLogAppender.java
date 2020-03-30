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
        addInfo("AwsConfig was set to "+config);
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setGroupName(String groupName) {
        addInfo("groupName was set to "+groupName);
        this.groupName = groupName;
    }

    public void setStreamName(String streamName) {
        addInfo("streamName was set to "+streamName);
        this.streamName = streamName;
    }

    public void setDateFormat(String dateFormat) {
        addInfo("dateFormat was set to "+dateFormat);
        this.dateFormat = dateFormat;
    }
    
    public void setQueueLength(int aLength) {
        addInfo("queueLength was set to "+aLength);
        queueLength = aLength;
    }

    public void setCreateLogGroup(boolean createLogGroup) {
        addInfo("createLogGroup was set to "+createLogGroup);
        this.createLogGroup = createLogGroup;
    }

    @Override
    protected synchronized void append(ILoggingEvent event) {

        event.prepareForDeferredProcessing();
        dump.queue(event);
    }

    @Override
    public synchronized void start() {
        if (isStarted()) {
            return;
        }
        
        dump = new AwsCWEventDump(this);

        Thread t = new Thread(dump);
        t.setDaemon(true);
        t.start();

        super.start();
    }

    @Override
    public synchronized void stop() {
        if (!isStarted()) {
            return;
        }
        
        super.stop();
        dump.shutdown();
        dump = null;
    }
}
