Cloudwatch Logback Appender
===========================

## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Dieter Bogdoll.

## Overview
This project provides a [logback appender](https://logback.qos.ch/) whichs target is [AWS Cloudwatch Logs](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/WhatIsCloudWatchLogs.html).

## Configuration
With the following XML fragemnt you can configure your Cloudtwatch logback appender:

    <appender name="cloud-watch" class="io.github.dibog.AwsLogAppender">

        <awsConfig>
            <credentials>
                <accessKeyId></accessKeyId>
                <secretAccessKey></secretAccessKey>
            </credentials>

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

    </appender>
    
To be able to use the appender the IAM profile under which the logging is running requires    
at least the following AWS permissions:
* ``logs:DescribeLogStreams``
* ``logs:PutLogEvents``

The section ``<awsConfig>`` is optional and on an EC2 instance usually not required as long
as you have attached an IAM profile to your instance with the right permissions and/or have
set the environment variables required to provide the AWS credentials.

But if that section is available in the configuration it will be used instead of the data
from the environment.

The sub section ``<credentials>`` is self explainoary, just determine your values for this 
section and enter them.

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
to desynchronize the calling of the logger appender and the writing of the logging events into AWS.
The argument is optional and has an default value.

* ``<groupName>``: The name of the log group. If ``<createLogGroup>`` was configured to ``true`` the log group
will be created it it does not yet exist. 

* ``<streamName>``: The name of the log stream. If the argument ``<dateFormat>`` is set, then the
content of ``<streamName>`` is used as prefix otherwise it is taken as specified. The log stream will
be created on the fly. 

* ``<dataFormat>``: If a valid (SimpleDateFormat string)[https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html] is
provided it will be used with the ``<streamName>`` to create a combined log stream. This feature
can be used to create multiple log streams of the live time of the logging process. Everytime the
SimpleDateFormat will yield a new stream name the current log stream will be closed and a new
one created.





 
