package net.bogdoll;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.read.CyclicBufferAppender;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

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
    protected void append(ILoggingEvent event) {

        AwsCWEventDump queue = dump;
        if (dump != null) {
            dump.queue(event);
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
        // flush it
        dump.shutdown();
        dump = null;
    }
}
