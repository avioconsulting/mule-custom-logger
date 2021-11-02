# What?
This project contains several utilities to supplement MuleSoft's out of the box logger and enforce logging standards on application logs. 
* The custom logger processor builds and logs Java HashMap with the given key-value pairs thus providing a way to generate 
consistent and structured log messages: 
* The timer scope records the amount of time it takes to execute the processors inside the scope and logs the time elapsed.
* The notification logger listens to the Mule runtime's engine notifications and logs when flows start and end.

# Why?
One of the reasons for developing this custom module is to feed JSON logs to log analyzers like Splunk, ELK etc. When these log analyzers receive structured logs(JSON), It is easy on the log analyzer side to create reports, dashboards etc. Below are the things this Logger can do when compared to out-of-the-box MuleSoft's logger component.

* Easy to use for a Mule developer to log necessary and sufficient data.
* Log metadata with each log message like application name, version, environment, location info etc.
* Log any number of data points within a log message thus enabling log analyzer to have sufficient data to create stunning reports.
* Log exceptions.
* Log Correlation ID.
* Optionally log location info with each log message.
* Tracepoints compatibility to measure request, response times and such.

# Using the Custom Logger Processor
Here is how mule-custom-logger looks like in action.

![image](https://user-images.githubusercontent.com/34731865/57938654-361bee00-788e-11e9-9ff6-6eadf9b77a1b.png)


This will log Java HashMap as shown below.
```
INFO  2019-07-01 19:10:59,412 [[MuleRuntime].cpuLight.24: [sample].sampleFlow.CPU_LITE @18c7b87e] [event: dccaf190-9c5d-11e9-9fcf-38f9d373e316] com.avio: {ext={additionalKey1=additionalValue1, additionalKey2=additionalValue2}, exception={detail=null, type=null, statusCode=null}, app_name=sample, app_version=1.0.0, log={trace_point=START, payload=null, correlation_id=a86212d5-d5c0-4b78-96cc-863f581a6ac0, message=This is nothing but a sample log message}, location={line_in_file=17, component=avio-core:custom-logger, file_name=sample.xml, root_container=sampleFlow, location=sampleFlow/processors/1}, env=dev, timestamp=2019-07-02T00:10:59.412Z}
```
With this custom module producing HashMap, 
* It would be handy in log4j2.xml to take this HashMap and convert to desired formats using log4j2 layouts. There are many out-of-the-box log4j layouts available here. See https://logging.apache.org/log4j/2.x/manual/layouts.html.
* Let appenders in log4j2 decide what layout to use to send to it specified destination.

Below is the out-of-the-box log4j2 JSON layout(wrapped in Console appender) to convert the HashMap produced by the custom module to JSON.
```$xml
<Console name="Console" target="SYSTEM_OUT">
    <JSONLayout objectMessageAsJsonObject="true"/>
</Console>
```
Dont forget to add this appender to your root or any of your loggers. See below
```xml
<AsyncRoot level="INFO">
    <AppenderRef ref="file"/>
    <AppenderRef ref="Console"/>
</AsyncRoot>
```
There are several attributes provided by this JSONLayout that you can play with. Above log4j layout definition produces the JSON like below.
```json
{
    "app_name": "sample",
    "app_version": "1.0.0",
    "env": "dev",
    "ext": {
        "additionalKey1": "additionalValue1",
        "additionalKey2": "additionalValue2"
    },
    "location": {
        "component": "avio-core:custom-logger",
        "file_name": "sample.xml",
        "line_in_file": "16",
        "location": "sampleFlow/processors/1",
        "root_container": "sampleFlow"
    },
    "log": {
        "category": "com.avio",
        "correlation_id": "33af6200-018b-42af-8ec1-ebdbf29c0505",
        "level": "INFO",
        "message": "This is nothing but a sample log message",
        "tracePoint": "START"
    },
    "thread": "[MuleRuntime].cpuLight.23: [sample].sampleFlow.CPU_LITE @27346628",
    "timestamp": "2019-05-17T15:06:34.592Z"
}
```
**Important**: If you are using this kind of approach(HashMap + JSONLayout in log4j2.xml) for any MuleSoft CloudHub projects, and if you want to see JSON logs in CloudHub console, The CloudHub appender provided by MuleSoft defaults to one pattern and it ignores any layout you specify. Most of the log4j2 appenders should accept layouts though. 

**Local Development**: The above logs use an ObjectMessage that can be parsed into JSON as a complete message.  To make local development easier, a flag can be enabled that will use a MapMessage.  This MapMessage can then be filtered in the Log4j setting.

From Studio: go to Run -> Run Configurations and add the following to VM arguments:
```
-Davio.custom.logger.env=local
```
In Log4j, add the following appender:
```xml
 <Console name="LOCAL" target="SYSTEM_OUT">
	<PatternLayout pattern="%5p [%d] %K{log}%n" />
</Console>
```


# Using the Timer Scope
The timer scope allows you to unobtrusively measure the time taken to execute the processors within the scope. For example, 
if you had a complex Transform Message processor that you wanted to time, you could place it inside the scope to get an INFO level 
log of how many milliseconds it took to execute.

To use the timer scope, pull it in from the AVIO Core section of the palette and fill in the two required values, Timer Name and Category. 
After that, check the App Level properties and validate they're what you desire. Now you're good to go!

# Using the Custom Logger Notification Listener
The custom logger notification listener implements an interface that allows it to be notified when flows start and end. It's also able to retrieve 
your ```avio-core:config``` global element and match its App Level properties to your custom loggers'. Note that (unless overriden) 
all flow start/stop messages will have a category suffix of ```.flow```.

To use the custom logger notification listener, you must instantiate the object inside your Mule runtime. You do this by using the ```<object>``` 
global element. An example has been included below:

```xml
<object doc:name="Object" doc:id="4df721fe-16b2-4ce3-819e-878fb4dd53c1" 
    name="CustomLoggerNotificationListener" 
    class="com.avio.customlogger.engine.CustomLoggerNotificationListener" />
```
You can also override the global configuration elements by specifying properties. The available properties are as follows:
 * appName
 * appVersion
 * env
 * category - Note, this completely overrides the category, i.e. it does not use the ```baseCategory``` from the global configuration 
 as a prefix.

# How?

## Using maven dependency
First, clone this repository and run ```mvn clean install``` to install this maven project in your local .m2 repository.


When you install this project into your machine's local .m2 repository, You can include this dependency(see below) in your mule projects. When you included this dependency in your project's pom.xml, AVIO's custom logger component automatically shows up in mule project's pallete and using this logger afterwards is just a drag away.

```xml
<dependency>
    <groupId>YOUR_GROUP_ID</groupId>
    <artifactId>mule-custom-logger</artifactId>
    <version>1.0.0</version>
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
