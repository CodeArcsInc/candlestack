# Overview#

Candlestack is an open source tool for monitoring dynamic infrastructure deployed via AWS. It is capable of automatically detecting infrastructure changes while utilizing a combination of open source tools to collect, visualize, monitor, and alert on various metrics. 

## How does it work?##

At the heart of the system sits the Candlestack server which runs the Candlestack Java application and [Nagios core](https://www.nagios.org/projects/nagios-core/). The Candlestack Java application performs two main roles:

1. Monitor the supported AWS services for any changes that requires us to either start or stop monitoring pieces of infrastructure. In the event something has changed it will update the Nagios configuration as applicable.
2. Collect certain metric data from the AWS CloudWatch service and/or AWS service API and feed it into Elasticsearch via Filebeat and Logstash so that it can be used for monitoring and visualization.  

Meanwhile the Nagios core provides the support for monitoring and alerting via a well known industry tool. To send alert emails it utilizes AWS SES but it could also easily utilize SMTP or any other means supported by Nagios core.

Looking beyond the Candlestack server we have an Elasticsearch server or cluster that aggregates and stores the various metrics being collected. These metrics can then be accessed via Kibana to visualize things such as CPU utilization over time. It is also accessed by Nagios as part of its monitoring routine.

Beyond that we have the applications themselves which in some cases will be running tools such as the munin-node agent to collect other metric data about the EC2 instance such as disk utilization and free memory.

## AWS Support##

AWS offers a large number of services, some of which do not make sense to monitor from a systems perspective. That being said Candlestack currently supports the monitoring of the following AWS services with more services to be supported down the road.

### EC2###

EC2 is the back bone for most dynamic infrastructure in the AWS ecosystem since it provides a variety of on demand hardware for running various applications. Currently Candlestack is able to monitor CPU utilization, network in, and network out for any EC2 instance out of the box thanks to CloudWatch. It is also possible to monitor disk utilization and memory utilization if other services are installed on the EC2 instance such as the munin-node agent.

### Elastic Beanstalk###

Elastic Beanstalk provides an easy way to deploy applications to AWS EC2 instances along with load balancing and system scaling. Currently Candlestack is able to perform monitoring of the Elastic Beanstalk environment health along with the EC2 instance monitoring mentioned above.

### SQS###

SQS provides a message queueing service that is often used in place of other JMS providers such as ActiveMQ when deploying infrastructure to AWS. Currently Candlestack is able to monitor on a per queue basis the approximate number of messages, approximate age of oldest message, number of messages received, number of messages sent, and last modified.

### RDS###

RDS provides a variety of relational database technologies that can be easily spun up with out you needing to know the intricacies of that databases hardware setup. Since each of the database technologies in RDS have different CloudWatch metrics Candlestack currently only supports MariaDB and AuroraDB databases. For a MariaDB database it is able to monitor CPU utilization, number of database connections, and free storage space. As for an AuroraDB database it is able to monitor CPU utilization, number of database connections, volume bytes used, replica lag, and number of active transactions. 

### S3###

S3 provides virtually unlimited storage potential for any files that need to be persisted or accessed via different systems. Since a common use of S3 is to store backups of databases or servers Candlestack currently supports monitoring specific S3 files last modified for staleness.

# Installation#

To help with the installation of the Candlestack server we have provided an example Docker image file and Elastic Beanstalk template. The Docker image file example located [here](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/base-image-dockerfile) includes all of the dependencies needed by the Candlestack server to properly function, which includes:

- [Nagios Core](https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/quickstart.html) & [Plugins](http://nagios-plugins.org/downloads/)
  - Nagios handles the monitoring and alerting logic
- [Apache2 Web Server](https://httpd.apache.org/)
  - Web server for serving the Nagios web pages
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  - The Candlestack java application was built using Java 8 and thus requires a Java 8 JVM to run
- [Filebeat](https://www.elastic.co/guide/en/beats/filebeat/1.3/filebeat-installation.html)
  - Sends the Candlestack logs and metric data to Elasticsearch
- [AWS Command Line Interface (CLI)](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)
  - Used by Nagios to send emails via AWS SES, see the [notify-host-by-email.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/notify-host-by-email.sh) and [notify-service-by-email.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/notify-service-by-email.sh) scripts for how this is done
- [Supervisor](http://supervisord.org/installing.html)
  - Used to keep the Filebeat and Candlestack Java application running

The Elastic Beanstalk template example located [here](https://github.com/CodeArcsInc/candlestack/tree/master/deploy/elasticbeanstalk-template) demonstrates the various files that need to be provided and how to start the various components. Throughout the files you will come across variables that are prefixed with **$TODO_**, which represent places in the script you should replace the variable with a value applicable for your system. You can find a complete list of these variables below along with an explanation of each one.

| Variable                      | File(s)                                  | Description                              |
| ----------------------------- | ---------------------------------------- | ---------------------------------------- |
| $*TODO*_DOCKER_IMAGE_LOCATION | [Dockerfile](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/Dockerfile) | The URL to retrieve the base Docker image created by the Docker image file mentioned above |
| $TODO_NAGIOS_ADMIN            | [Dockerfile](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/Dockerfile) | The desired Nagios admin username        |
| $TODO_NAGIOS_PASSWORD         | [Dockerfile](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/Dockerfile) | The desired Nagios admin password        |
| $*TODO*_LOGSTASH_HOST         | [filebeat.yml](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/dev-filebeat.yml) and [00-munin.config](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/.ebextensions/00-munin.config) | The URL of the Logstash server to be used by Candlestack for sending logs or metric data (Note: you may need to alter the ports depending on your Logstash configuration) |
| $*TODO*_FROM_EMAIL_ADDRESS    | [notify-host-by-email.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/notify-host-by-email.sh) and [notify-service-by-email.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/notify-service-by-email.sh) | The from email address Nagios should use when sending notification emails |
| $*TODO*_REPOSITORY            | [pom.xml](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/pom.xml) | The maven repository URL to be used when building the deployment |
| $*TODO*_SSL_CERTIFICATE       | [elb.config](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/.ebextensions/elb.config) | The SSL certificate ARN to be used by Candlestack server |

# Configuration#

Candlestack has been built to be highly configurable since everyone's infrastructure is different and thus so are their monitoring requirements. 

## Candlestack Java Application##

Outlined below you will find all of the configuration properties for the Java application explained and a sample configuration file has been provided [here](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/candlestack/candlestack-sample.ini) to get you started. 

| Property                                 | Required | Default | Description |
| ---------------------------------------- | -------- | ------- | ----------- |
| metrics.writer.dir                       |          |         |             |
| scripts.dir                              |          |         |             |
| nagios.updater.sleep.interval.min        |          |         |             |
| nagios.updater.restart.cmd               |          |         |             |
| nagios.object.definition.dir             |          |         |             |
| nagios.command.notify-host-by-email      |          |         |             |
| nagios.command.notify-service-by-email   |          |         |             |
| nagios.contact.default.host.notifications.enabled |          |         |             |
| nagios.contact.default.host.notification.options |          |         |             |
| nagios.contact.default.host.notification.commands |          |         |             |
| nagios.contact.default.host.notification.period |          |         |             |
| nagios.contact.default.service.notifications.enabled |          |         |             |
| nagios.contact.default.service.notification.options |          |         |             |
| nagios.contact.default.service.notification.commands |          |         |             |
| nagios.contact.default.service.notification.period |          |         |             |
| nagios.contact.***contactname***.alias   |          |         |             |
| nagios.contact.***contactname***.email   |          |         |             |
| nagios.contact.***contactname***.host.notifications.enabled |          |         |             |
| nagios.contact.***contactname***.host.notification.options |          |         |             |
| nagios.contact.***contactname***.host.notification.commands |          |         |             |
| nagios.contact.***contactname***.host.notification.period |          |         |             |
| nagios.contact.***contactname***.service.notifications.enabled |          |         |             |
| nagios.contact.***contactname***.service.notification.options |          |         |             |
| nagios.contact.***contactname***.service.notification.commands |          |         |             |
| nagios.contact.***contactname***.service.notification.period |          |         |             |
| nagios.contactgroup.***contactgroupname***.alias |          |         |             |
| nagios.contactgroup.***contactgroupname***.members |          |         |             |
| aws.region                               |          |         |             |
| aws.logs.host                            |          |         |             |
| aws.logs.authtoken                       |          |         |             |
| aws.cloudwatch.detailed.monitoring.enabled |          |         |             |
| aws.ec2.enabled                          |          |         |             |
| aws.ec2.name.prefix                      |          |         |             |
| aws.ec2.name.regex                       |          |         |             |
| aws.ec2.metrics.fetcher.sleep.min        |          |         |             |
| aws.ec2.graphite.metrics.monitor         |          |         |             |
| aws.ec2.graphite.metric.warning.default.DiskUtilization |          |         |             |
| aws.ec2.graphite.metric.critical.default.DiskUtilization |          |         |             |
| aws.ec2.graphite.metric.warning.default.FreeMemory |          |         |             |
| aws.ec2.graphite.metric.critical.default.FreeMemory |          |         |             |
| aws.ec2.graphite.metric.warning.***instanceid***.DiskUtilization |          |         |             |
| aws.ec2.graphite.metric.critical.***instanceid***.DiskUtilization |          |         |             |
| aws.ec2.graphite.metric.warning.***instanceid***.FreeMemory |          |         |             |
| aws.ec2.graphite.metric.critical.***instanceid***.FreeMemory |          |         |             |
| aws.ec2.cloudwatch.metrics.fetch         |          |         |             |
| aws.ec2.cloudwatch.metrics.monitor       |          |         |             |
| aws.ec2.cloudwatch.metric.warning.default.CPUUtilization |          |         |             |
| aws.ec2.cloudwatch.metric.critical.default.CPUUtilization |          |         |             |
| aws.ec2.cloudwatch.metric.warning.default.NetworkIn |          |         |             |
| aws.ec2.cloudwatch.metric.critical.default.NetworkIn |          |         |             |
| aws.ec2.cloudwatch.metric.warning.default.NetworkOut |          |         |             |
| aws.ec2.cloudwatch.metric.critical.default.NetworkOut |          |         |             |
| aws.ec2.cloudwatch.metric.warning.***instanceid***.CPUUtilization |          |         |             |
| aws.ec2.cloudwatch.metric.critical.***instanceid***.CPUUtilization |          |         |             |
| aws.ec2.cloudwatch.metric.warning.***instanceid***.NetworkIn |          |         |             |
| aws.ec2.cloudwatch.metric.critical.***instanceid***.NetworkIn |          |         |             |
| aws.ec2.cloudwatch.metric.warning.***instanceid***.NetworkOut |          |         |             |
| aws.ec2.cloudwatch.metric.critical.***instanceid***.NetworkOut |          |         |             |
| aws.eb.enabled                           |          |         |             |
| aws.eb.environment.name.prefix           |          |         |             |
| aws.eb.environment.name.regex            |          |         |             |
| aws.eb.metrics.fetcher.sleep.min         |          |         |             |
| aws.eb.cloudwatch.metrics.fetch          |          |         |             |
| aws.eb.cloudwatch.metrics.monitor        |          |         |             |
| aws.eb.cloudwatch.metric.warning.default.EnvironmentHealth |          |         |             |
| aws.eb.cloudwatch.metric.critical.default.EnvironmentHealth |          |         |             |
| aws.eb.cloudwatch.metric.warning.***environmentname***.EnvironmentHealth |          |         |             |
| aws.eb.cloudwatch.metric.critical.***environmentname***.EnvironmentHealth |          |         |             |
| aws.sqs.enabled                          |          |         |             |
| aws.sqs.queue.name.prefix                |          |         |             |
| aws.sqs.queue.name.regex                 |          |         |             |
| aws.sqs.monitor.deadletter               |          |         |             |
| aws.sqs.metrics.fetcher.sleep.min        |          |         |             |
| aws.sqs.queue.attributes.fetch           |          |         |             |
| aws.sqs.queue.attributes.monitor         |          |         |             |
| aws.sqs.queue.attribute.warning.default.ApproximateNumberOfMessage |          |         |             |
| aws.sqs.queue.attribute.critical.default.ApproximateNumberOfMessage |          |         |             |
| aws.sqs.queue.attribute.warning.default.LastModifiedTimestamp |          |         |             |
| aws.sqs.queue.attribute.critical.default.LastModifiedTimestamp |          |         |             |
| aws.sqs.queue.attribute.warning.***queuename***.ApproximateNumberOfMessage |          |         |             |
| aws.sqs.queue.attribute.critical.***queuename***.ApproximateNumberOfMessage |          |         |             |
| aws.sqs.queue.attribute.warning.***queuename***.LastModifiedTimestamp |          |         |             |
| aws.sqs.queue.attribute.critical.***queuename***.LastModifiedTimestamp |          |         |             |
| aws.sqs.cloudwatch.metrics.fetch         |          |         |             |
| aws.sqs.cloudwatch.metrics.monitor       |          |         |             |
| aws.sqs.cloudwatch.metric.warning.default.ApproximateAgeOfOldestMessage |          |         |             |
| aws.sqs.cloudwatch.metric.critical.default.ApproximateAgeOfOldestMessage |          |         |             |
| aws.sqs.cloudwatch.metric.warning.default.NumberOfMessagesReceived |          |         |             |
| aws.sqs.cloudwatch.metric.critical.default.NumberOfMessagesReceived |          |         |             |
| aws.sqs.cloudwatch.metric.warning.default.NumberOfMessagesSent |          |         |             |
| aws.sqs.cloudwatch.metric.critical.default.NumberOfMessagesSent |          |         |             |
| aws.sqs.cloudwatch.metric.warning.***queuename***.ApproximateAgeOfOldestMessage |          |         |             |
| aws.sqs.cloudwatch.metric.critical.***queuename***.ApproximateAgeOfOldestMessage |          |         |             |
| aws.sqs.cloudwatch.metric.warning.***queuename***.NumberOfMessagesReceived |          |         |             |
| aws.sqs.cloudwatch.metric.critical.***queuename***.NumberOfMessagesReceived |          |         |             |
| aws.sqs.cloudwatch.metric.warning.***queuename***.NumberOfMessagesSent |          |         |             |
| aws.sqs.cloudwatch.metric.critical.***queuename***.NumberOfMessagesSent |          |         |             |
| aws.rds.enabled                          |          |         |             |
| aws.rds.dbinstance.prefix                |          |         |             |
| aws.rds.dbinstance.regex                 |          |         |             |
| aws.rds.metrics.fetcher.sleep.min        |          |         |             |
| aws.rds.cloudwatch.metrics.fetch         |          |         |             |
| aws.rds.cloudwatch.metrics.monitor       |          |         |             |
| aws.rds.cloudwatch.metric.warning.default.CPUUtilization |          |         |             |
| aws.rds.cloudwatch.metric.critical.default.CPUUtilization |          |         |             |
| aws.rds.cloudwatch.metric.warning.default.DatabaseConnections |          |         |             |
| aws.rds.cloudwatch.metric.critical.default.DatabaseConnections |          |         |             |
| aws.rds.cloudwatch.metric.warning.default.FreeStorageSpace |          |         |             |
| aws.rds.cloudwatch.metric.critical.default.FreeStorageSpace |          |         |             |
| aws.rds.cloudwatch.metric.warning.default.VolumeBytesUsed |          |         |             |
| aws.rds.cloudwatch.metric.critical.default.VolumeBytesUsed |          |         |             |
| aws.rds.cloudwatch.metric.warning.default.AuroraReplicaLag |          |         |             |
| aws.rds.cloudwatch.metric.critical.default.AuroraReplicaLag |          |         |             |
| aws.rds.cloudwatch.metric.warning.default.ActiveTransactions |          |         |             |
| aws.rds.cloudwatch.metric.critical.default.ActiveTransactions |          |         |             |
| aws.rds.cloudwatch.metric.warning.***dbinstance***.CPUUtilization |          |         |             |
| aws.rds.cloudwatch.metric.critical.***dbinstance***.CPUUtilization |          |         |             |
| aws.rds.cloudwatch.metric.warning.***dbinstance***.DatabaseConnections |          |         |             |
| aws.rds.cloudwatch.metric.critical.***dbinstance***.DatabaseConnections |          |         |             |
| aws.rds.cloudwatch.metric.warning.***dbinstance***.FreeStorageSpace |          |         |             |
| aws.rds.cloudwatch.metric.critical.***dbinstance***.FreeStorageSpace |          |         |             |
| aws.rds.cloudwatch.metric.warning.***dbinstance***.VolumeBytesUsed |          |         |             |
| aws.rds.cloudwatch.metric.critical.***dbinstance***.VolumeBytesUsed |          |         |             |
| aws.rds.cloudwatch.metric.warning.***dbinstance***.AuroraReplicaLag |          |         |             |
| aws.rds.cloudwatch.metric.critical.***dbinstance***.AuroraReplicaLag |          |         |             |
| aws.rds.cloudwatch.metric.warning.***dbinstance***.ActiveTransactions |          |         |             |
| aws.rds.cloudwatch.metric.critical.***dbinstance***.ActiveTransactions |          |         |             |
| aws.s3.enabled                           |          |         |             |
| aws.s3.metrics.fetcher.sleep.min         |          |         |             |
| aws.s3.locations                         |          |         |             |
| aws.s3.metadata.metrics.fetch            |          |         |             |
| aws.s3.metadata.metrics.monitor          |          |         |             |
| aws.s3.metadata.metric.warning.default.LastModified |          |         |             |
| aws.s3.metadata.metric.critical.default.LastModified |          |         |             |
| aws.s3.metadata.metric.warning.***locationid***.LastModified |          |         |             |
| aws.s3.metadata.metric.critical.***locationid***.LastModified |          |         |             |

## Nagios Check Scripts##

Depending on the metrics you have enabled for monitoring via the Candlestack Java application configuration you will need to provide a corresponding Nagios check script. Example check scripts can be found [here](https://github.com/CodeArcsInc/candlestack/tree/master/deploy/elasticbeanstalk-template/scripts) and in most cases can be used by your application with very little to no modifications. Below you will find a table that outlines the parameters a check script will always receive and another table that maps the monitor metric to script file name.

### Check Script Properties###

| Order Number | Property Name | Description                              |
| ------------ | ------------- | ---------------------------------------- |
| 1            | host          | The Elasticsearch host containing the metric data to be checked |
| 2            | authtoken     | The authtoken to use when accessing Elasticsearch |
| 3            | instanceid    | The particular "id" that identifies the specific instance of a resource that is being checked (an example would be for EC2 instance check this would be the EC2 instance id) |
| 4            | warning       | The value that has been provided as the warning level for metric checks |
| 5            | critical      | The value that has been provided as the critical level for metric checks |

### Monitor Metric to Check Script Mapping###

| Monitor Metric                       | Related Java Application Configuration(s) | Check Script File Name                   |
| ------------------------------------ | ---------------------------------------- | ---------------------------------------- |
| Elastic Beanstalk Environment Health |                                          | [check-aws-eb-environment-health-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-eb-environment-health-via-es.sh) |
| EC2 CPU Utilization                  |                                          | [check-aws-ec2-cpu-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-ec2-cpu-via-es.sh) |
| EC2 Disk Utilization                 |                                          | [check-aws-ec2-disk-utilization-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-ec2-disk-utilization-via-es.sh) |
| EC2 Free Memory                      |                                          | [check-aws-ec2-free-memory-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-ec2-free-memory-via-es.sh) |
| EC2 Network In                       |                                          | [check-aws-ec2-network-in-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-ec2-network-in-via-es.sh) |
| EC2 Network Out                      |                                          | [check-aws-ec2-network-out-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-ec2-network-out-via-es.sh) |
| RDS Active Transactions              |                                          | [check-aws-rds-active-transactions-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-rds-active-transactions-via-es.sh) |
| RDS CPU Utilization                  |                                          | [check-aws-rds-cpu-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-rds-cpu-via-es.sh) |
| RDS Database Connections             |                                          | [check-aws-rds-db-connections-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-rds-db-connections-via-es.sh) |
| RDS Free Storage                     |                                          | [check-aws-rds-free-storage-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-rds-free-storage-via-es.sh) |
| RDS Aurora Replica Lag               |                                          | [check-aws-rds-replica-lag-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-rds-replica-lag-via-es.sh) |
| RDS Storage Used                     |                                          | [check-aws-rds-storage-used-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-rds-storage-used-via-es.sh) |
| S3 Last Modified                     |                                          | [check-aws-s3-last-modified-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-s3-last-modified-via-es.sh) |
| SQS Last Modified                    |                                          | [check-aws-sqs-queue-last-modified-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-sqs-queue-last-modified-via-es.sh) |
| SQS Message Age                      |                                          | [check-aws-sqs-queue-message-age-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-sqs-queue-message-age-via-es.sh) |
| SQS Messages Received                |                                          | [check-aws-sqs-queue-messages-received-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-sqs-queue-messages-received-via-es.sh) |
| SQS Messages Sent                    |                                          | [check-aws-sqs-queue-messages-sent-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-sqs-queue-messages-sent-via-es.sh) |
| SQS Queue Size                       |                                          | [check-aws-sqs-queue-size-via-es.sh](https://github.com/CodeArcsInc/candlestack/blob/master/deploy/elasticbeanstalk-template/scripts/check-aws-sqs-queue-size-via-es.sh) |

