Cloudwatch Logback Appender
===========================

## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Dieter Bogdoll.

## Overview
This project provides a [logback appender](https://logback.qos.ch/) whichs target is [AWS Cloudwatch Logs](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/WhatIsCloudWatchLogs.html).

## Maven

    <dependency>
        <groupId>io.github.dibog</groupId>
        <artifactId>cloudwatch-logback-appender</artifactId>
        <version>1.0.6</version>
    </dependency>

## Configuration
With the following XML fragment you can configure your Cloudtwatch logback appender:

    <appender name="cloud-watch" class="io.github.dibog.AwsLogAppender">

        <awsConfig>
            <credentials>
                <accessKeyId></accessKeyId>
                <secretAccessKey></secretAccessKey>
            </credentials>
            
            <profileName>awsProfile</profileName>

            <region></region>

            <clientConfig class="com.amazonaws.ClientConfiguration">
                <proxyHost></proxyHost>
                <proxyPort></proxyPort>
            </clientConfig>
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

The section ``<awsConfig>`` is optional and on an EC2 instance. It usually is not required as long
as you have attached an IAM profile to your instance with the right permissions and/or have
set the environment variables required to provide the AWS credentials.

But if that section is available in the configuration it will be used instead of the data
from the environment.

To authenticate you can have currently three mechanism:
* Use the tag ``<profileName>`` to specify the name of profile.
* The use of tag ``<credentials>`` is self explanatory if you know your AWS. Just determine your values for this 
section and enter them here.
* Don't specify anything and you should get the IAM settings of your EC2 instance.


The sub section ``<region>`` should contain the AWS region into which the log information
should be streamed, please find here the [actual list of regions](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html#available_regions).
You have to use the format you can find in the left column, e.g. like ``eu-central-1``.

The ``<clientConfig>`` is again used mainly when your logging process is not run on an EC2 instance,
but somewhere outside of AWS. Please lookup the [ClientConfiguration](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/ClientConfiguration.html) within the AWS documentation.

And here now the remaining configuration elements:

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

### [](#unique-log-stream-names) Unique log stream names

To make the log stream name unqiue across the same application and multiple ec2 instances, 
we can use the variable substitution mechanism of logback:
 
    <appender name="cloud-watch" class="io.github.dibog.AwsLogAppender">

        <!-- just referencing the important settings -->
        <streamName>stream-name-${instance.id}</streamName>
        
    </appender>

And set the variable (in our case) `instance.id` via either `-D` from the command line, or via calling 
`System.setProperty("instance.id", uniqueId)` as one of the first methods in your main. 

Setting via `-D` is the recommended way.
    
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
