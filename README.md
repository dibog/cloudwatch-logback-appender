Cloudwatch Logback Appender
===========================

## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Dieter Bogdoll.

## Overview
This project provides a [logback appender](https://logback.qos.ch/) whichs target is [AWS Cloudwatch Logs](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/WhatIsCloudWatchLogs.html)
and is using the AWS V2 API. 

## Maven

    <dependency>
        <groupId>io.github.dibog</groupId>
        <artifactId>cloudwatch-logback-appender</artifactId>
        <version>2.0.0</version>
    </dependency>

## Configuration
With the following XML fragment you can configure your Cloudtwatch logback appender:

    <appender name="cloud-watch" class="io.github.dibog.AwsLogAppender">

        <awsConfig>

            <region>eu-central-1</region>

            <profileName>awsProfile</profileName>

            <credentials>
                <accessKeyId></accessKeyId>
                <secretAccessKey></secretAccessKey>
            </credentials>

            <httpClient> 
                <connectionAcquisitionTimeout>LONG</conconnectionAcquisitionTimeout>
                <connectionMaxIdleTime>LONG</connectionMaxIdleTime>
                <connectionTimeout>LONG</connectionTimeout>
                <connectionTimeToLive>LONG</connectionTimeToLive>
                <expectContinueEnabled>BOOLEAN</expectContinueEnabled>
                <localAddress>STRING</localAddress>
                <maxConnections>LONG</maxConnections>
                <socketTimeout>LONG</socketTimeout>
                <useIdleConnectionReaper>BOOLEAN</useIdleConnectionReaper>
                <proxyConfig>
                    <endpoint>STRING</endpoint>
                    <nonProxies>STRING</nonProxies>
                    <ntlmDomain>STRING</ntlmDomain>
                    <ntlmWorkstation>STRING</ntlmWorkstation>
                    <username>STRING</username>
                    <password>STRING</password>
                    <preemptiveBasicAuthenticationEnabled>BOOLEAN</preemptiveBasicAuthenticationEnabled>
                    <useSystemPropertyValues>BOOLEAN</useSystemPropertyValues>
                </proxyConfig>
            </httpClient>            

        </awsConfig>

        <createLogGroup>false</createLogGroup>
        <queueLength>100</queueLength>
        <groupName>group-name</groupName>
        <streamName>stream-name</streamName>
        <dateFormat>yyyyMMdd_HHmm</dateFormat>
        
         <layout>
            <pattern>[%X{a} %X{b}] %-4relative [%thread] %-5level %logger{35} - %msg %n</pattern>
         </layout>

    </appender>
    
To be able to use the appender the IAM profile under which the logging is running requires    
at least the following AWS permissions:
* ``logs:DescribeLogStreams``
* ``logs:PutLogEvents``

The section ``<awsConfig>`` is optional on an EC2 instance. It usually is not required as long
as you have attached an IAM profile to your instance with the right permissions and/or have
set the environment variables required to provide the AWS credentials.

But if that section is available in the configuration it will be used instead of the data
from the environment.

Please look here for more details to the properties: 
* [CloudWatchLogsClientBuilder](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/cloudwatchlogs/CloudWatchLogsClientBuilder.html)
* [AwsCredentials](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/AwsCredentials.html)
* [ApacheHttpClient.Builder](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/http/apache/ApacheHttpClient.Builder.html)
* [ProxyConfiguration.Builder](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/http/apache/ProxyConfiguration.Builder.html)


In the following a re the configuration elements of the `cloudwatch-logback-appender`:

* ``<createLogGroup>``: Valid arguments: ``true`` or ``false``, where ``true`` requires the IAM User to have 
the permissions ``logs:CreateLogGroup`` and ``logs:DescribeLogGroups``. Setting ``true`` allows the logger appender
to create the required logGroup in AWS. Setting it to ``false`` means that the logger appender expect
that the logGroup already exists. The default value is ``false``.

* ``<queueLength>``: Valid Arguments: an positive integer indicating the queue length which is used 
to decouple the calling of the logger appender and the writing of the logging events into AWS.
The argument is optional and has an default value. If the queue is not long enough you will get a message
like ``Skipped <n> messages in the last log cycle.`` within the log. Enlarging the queue length
might resolve this issue when there are some bursts of log message from time to time.

* ``<groupName>``: The name of the log group. If ``<createLogGroup>`` was configured to ``true`` the log group
will be created it it does not yet exist. 

* ``<streamName>``: The name of the log stream. If the argument ``<dateFormat>`` is set, then the
content of ``<streamName>`` is used as prefix otherwise it is taken as specified. The log stream will
be created on the fly. 

* ``<dataFormat>``: If a valid [SimpleDateFormat string](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) is
provided it will be used with the ``<streamName>`` to create a combined log stream. This feature
can be used to create multiple log streams of the live time of the logging process. Everytime the
SimpleDateFormat will yield a new stream name the current log stream will be closed and a new
one created.

* ``<layout>``: If exist it will be used to transform the logging event to a string which is stored in cloudwatch logs.
( See https://logback.qos.ch/manual/layouts.html#PatternLayout. ) 
If the tag is missing, the logging event will be transformed into a json object.

## Tips and Tricks

### Unique log stream names

To make the log stream name unqiue across the same application and multiple ec2 instances, 
we can use the variable substitution mechanism of logback:
 
    <appender name="cloud-watch" class="io.github.dibog.AwsLogAppender">

        <!-- just referencing the important settings -->
        <streamName>stream-name-${instance.id}</streamName>
        
    </appender>

And set the variable (in our case) `instance.id` via either `-D` from the command line, or via calling 
`System.setProperty("instance.id", uniqueId)` as one of the first methods in your main. 

Setting via `-D` is the recommended way.
    
### Remove the dependency to apache-client

If you don't need the `<httpClient>` tag in your logback settings you could even exclude the dependency to
the ApacheHttpClient in your pom.xml:
    
     <dependency>
            <groupId>io.github.dibog</groupId>
            <artifactId>cloudwatch-logback-appender</artifactId>
            <version>VERSION_OF_CLOUDWATCH_LOGBACK_APPENDER</version>
            <exclusion>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>apache-client</artifactId>
            </exclusion>
    </dependency>    
    
## Caveats

The [pom.xml](pom.xml) used for this project binds to a specific version of the AWS SDK.
In case you are using in your project also the AWS SDK the changes are high that the
version is different to that ot the cloudwatch-logback-appender, and this could lead to problems.
If the version of your AWS SDK is smaller then the one of cloudwatch-logback-appender I advise 
you to upgrade to the latest [AWS SDK version](https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk).
If your AWS SDK is version is later then the one used by cloudwatch-logback-appender you could
replace the dependecy to cloudwatch-logback-appender like this:

    <dependency>
        <groupId>com.amazonaws</groupId>
        <artifactId>aws-java-sdk-logs</artifactId>
        <version>VERSION_OF_OUR_AWS_SDK</version>
    </dependency>
    
    <dependency>
        <groupId>io.github.dibog</groupId>
        <artifactId>cloudwatch-logback-appender</artifactId>
        <version>VERSION_OF_CLOUDWATCH_LOGBACK_APPENDER</version>
        <exclusions>
            <exclusion>
                <groupId>com.amazonaws</groupId>
                <artifactId>aws-java-sdk-logs</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

n>