# What?
This is a Custom Mule logger to replace MuleSoft's out of the box logger to enforce logging standards on mule application logs. This logger builds and logs JSON objects with the given key-value pairs thus providing a way to generate consistent and structured log messages.

# Why?
One of the reasons for developing this custom module is to feed JSON logs to log analyzers like Splunk, ELK etc. When these log analyzers receive structured logs(JSON), It is easy on the log analyzer side to create reports, dashboards etc. Below are the things this Logger can do when compared to out-of-the-box MuleSoft's logger component.

* Easy to use for a Mule developer to log necessary and sufficient data.
* Log metadata with each log message like application name, version, environment, location info etc.
* Log any number of data points within a log message thus enabling log analyzer to have sufficient data to create stunning reports.
* Log exceptions.
* Log Correlation ID.
* Optionally log location info with each log message.
* Tracepoints compatibility to measure request, response times and such.

Here is the example of the JSON this custom logger can generate when configured with necessary data points.
```json
{
    "app_name": "some-api",
    "app_version": "1.0.0",
    "env": "dev",
    "exception": {
        "detail": "Resource not found",
        "status_code": "404",
        "type": "APIKIT:NOT_FOUND"
    },
    "ext": {
        "additionalKey1": "additionalValue1",
        "additionalKey2": "additionalValue2"
    },
    "location": {
        "component": "avio-core:custom-logger",
        "file_name": "some.xml",
        "line_in_file": "14",
        "location": "someFlow/processors/0",
        "root_container": "someFlow"
    },
    "log": {
        "category": "com.avio",
        "correlation_id": "77de8200-3cd2-4a1b-9505-69b4d9dc0e08",
        "level": "INFO",
        "message": "Request Received",
        "payload": "",
        "tracePoint": "START"
    },
    "thread": "[MuleRuntime].cpuLight.14: [some].someFlow.CPU_LITE @16d0beeb",
    "timestamp": "2019-01-18T21:38:40.842Z"
}
```

**Tip**: ext section in the above JSON is a great way to throw in any necessary/extraneous datapoints that are critical for business and for the debugging purposes.

# How?

## Using maven dependency
First, clone this repository and run ```mvn clean install``` to install this maven project in your local .m2 repository.


When you install this project into your machine's local .m2 repository, You can include this dependency(see below) in your mule projects. When you included this dependency in your project's pom.xml, AVIO's custom logger component automatically shows up in mule project's pallete and using this logger afterwards is just a drag away.

```xml
<dependency>
    <groupId>YOUR_GROUP_ID</groupId>
    <artifactId>mule-custom-logger</artifactId>
    <version>4.0.0</version>
    <classifier>mule-plugin</classifier>
</dependency>
```

## Push to anypoint private exchange
Alternatively, you can push this mule custom component to your anypoint organization's private exchange so that all developers inside that organization can use it. Here are the steps,

* First, Clone this GitHub repository into your local machine.
* Get your Anypoint's organization ID and
	* Place it in pom.xml group id tag. ```<groupId>YOUR_ORG_ID</groupId>```.
	* Place it in url tag under distribution management tag. See below

```xml
<distributionManagement>
	<repository>
		<id>anypoint-exchange</id>
		<name>Corporate Repository</name>
		<url>https://maven.anypoint.mulesoft.com/api/v1/organizations/YOUR_ORG_ID/maven</url>
		<layout>default</layout>
	</repository>
</distributionManagement>
```

* Include exchange credentials in your settings.xml under servers section and with the matching server id with the repository id in pom's distribution management tag.
* Run ```mvn clean deploy``` to deploy this custom component into your anypoint exchange.
* Now, click on "search on exchange" in your mule project pallete, login and install component in your project.