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

# Changes
## 2.1.0
* Added
	- Implementation to optionally compress or encrypt the payload within the logger operations
* Updated
	- Custom Logger class log method to optionally log the encrypted, compressed or raw payload string
## 1.2.1
* Added system property '_avio.logger.mapmessage = true_' to utilize a MapMessage instead of ObjectMessage for logging, this allows for filtered logs.  See Simplified Logging section below.
* Supports 4.1 - 4.4 releases of the Mule Runtime

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

# Using Compression and Encryption (v 2.1.x and higher)
The compression and encyrption functionality allows you to compress, encrypt, or apply both to the payload string that is being logged. The mule modules for Crypto and Compression are being utilized seemlessly incorporate this functionality into your Mule project. 

To use this functionality, be sure you are using at or above v2.1.0, navigate to your AVIO Logger configuration. Once in the configuration you can access the new tabs `Compression` and `Encryption`. In the Compression tab, select from the list of support compressing strategies. For the Encryption tab, select the algorithm you want to utilize, and finally supply the password that will be used to encrypt.

Example of logger configuration that utilizes compression with the `compressor` attribute: 
```
	<avio-logger:config name="ts-logger-test-config-compression-applied" doc:name="AVIO Logger Config" doc:id="5b6cabea-3a64-48ae-81a7-1c28c45e70cb" applicationVersion="#[p('api.version')]" defaultCategory="com.avioconsulting.mule" compressor="GZIP"/>
```

Example of logger configuration that is utilizing `encryptionAlgorithm` and `encryptionPassword` to implement encryption of the payload
```
	<avio-logger:config name="ts-logger-test-config-encryption" doc:name="AVIO Logger Config" doc:id="e4f60146-7ee7-404a-a177-ac187c874bdd" applicationVersion="#[p('api.version')]" defaultCategory="com.avioconsulting.mule" encryptionAlgorithm="PBEWithHmacSHA512AndAES_128" encryptionPassword="${secure::encryption.password}"/>
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
        class="com.avioconsulting.mule.logger.internal.listeners.CustomLoggerNotificationListener"/>
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

# Simplified Logging
When developing and debugging code locally, the complete log message is rarely necessary, only a small subset. 
To be able to filter messages, the logger code must use a MapMessage component, instead of an ObjectMessage.  This MapMessage can then be filtered in a Log4j pattern.

* Create a ```src/main/resources/local/log4j2.xml``` file - This file will only be used when running locally
```xml
<?xml version="1.0" encoding="utf-8"?>
<Configuration>
	<Appenders>
		<Console name="CONSOLE" target="SYSTEM_OUT">
			<!-- the pattern can be modified to your need -->
			<PatternLayout pattern="%5p [%d] %K{log}%n" />
		</Console>
	</Appenders>
	<Loggers>
		<AsyncRoot level="INFO">
			<!-- don't forget to enable the appender -->
			<AppenderRef ref="CONSOLE"/>
		</AsyncRoot>
	</Loggers>
</Configuration>
```
* Update pom.xml to include the following profiles - Using the ```local``` profile will copy the src/main/resources/local directory into the project output directory, overriding the original log4j2.xml 
```xml
. . .
<profiles>
	<profile>
		<!-- using maven pass in -Plocal to use this profile -->
		<id>local</id>
		<build>
			<plugins>
				<plugin>
					<artifactId>maven-resources-plugin</artifactId>
					<version>3.1.0</version>
					<executions>
						<execution>
							<id>local-log4j2</id>
							<phase>process-resources</phase>
							<goals>
								<goal>copy-resources</goal>
							</goals>
							<configuration>
								<resources>
									<resource>
										<!-- copies the 'local' folder to outputDirectory -->
										<directory>src/main/resources/local/</directory>
										<filtering>true</filtering>
									</resource>
								</resources>
								<outputDirectory>${project.build.outputDirectory}</outputDirectory>
								<overwrite>true</overwrite>
							</configuration>
						</execution>
					</executions>
				</plugin>
			</plugins>
		</build>
	</profile>
	<profile>
		<!-- Standard profile for normal builds -->
		<id>other</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
		<build>
			<resources>
				<resource>
					<directory>src/main/resources</directory>
					<excludes>
						<exclude>local/**</exclude>
					</excludes>
				</resource>
				<resource>
					<!-- Helps IntelliJ understand src/main/mule is a source directory too -->
					<directory>src/main/mule</directory>
				</resource>
			</resources>
		</build>
	</profile>
</profiles>
. . .
```
* From Studio, Run -> Run Configurations
  * Add VM Argument: ```-Davio.logger.mapmessage=true```
![VMArgument](https://user-images.githubusercontent.com/36522886/140087312-290dd3d0-5158-4a6f-b07d-ec18d9ff743e.png)
  * Add Maven command line arguments: ```-Plocal```
![MavenCommand](https://user-images.githubusercontent.com/36522886/140087337-5bc6421c-640e-4c57-b836-d80cb8fa0c21.png)

Sample Log
```
 INFO [2021-11-03 10:14:29,744] {trace_point=START, payload=OrderId : 548102842, correlation_id=b25b770a.ee7a.4820.8abc.9346ff693840, message=Start of flow}
 INFO [2021-11-03 10:14:29,773] {trace_point=FLOW, payload={
  "orderId": 548102842,
  "customerId": "ARG-12934",
  "items": [
    "CP-123",
    "CP-452"
  ]
}, correlation_id=correlationId, message=Here is my message}
 INFO [2021-11-03 10:14:29,775] {trace_point=END, payload=OrderId : 548102842, correlation_id=b25b770a.ee7a.4820.8abc.9346ff693840, message=End of flow}
```
