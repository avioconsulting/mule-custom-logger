package com.avioconsulting.mule.logger.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.MuleException;

public class CustomLoggerTest {

  private String examplePayload;
  private String correlationId;
  private String applicationName;
  private String applicationVersion;
  private String environment;
  private String defaultCategory;
  private Boolean enableV1Compatibility;

  @Before
  public void init() {
    examplePayload = "Example Payload Text";
    correlationId = "ab9195f8-0ff6-4611-ab78-63c60c824c95";
    applicationName = "example-app-name";
    applicationVersion = "1.0.0";
    environment = "dev";
    defaultCategory = "com.avioconsulting.mule";
    enableV1Compatibility = false;
  }

  @Test
  public void log_verifyLogPropertiesNoPayloadNoAttributes_test() {
    // Initialize, create mocks and spys
    CustomLogger customLogger = new CustomLogger();
    LogProperties logProperties = new LogProperties();
    LogProperties spyLogProperties = spy(logProperties);
    MessageAttributes messageAttributes = new MessageAttributes();
    MessageAttributes spyMessageAttributes = spy(messageAttributes);
    when(spyLogProperties.getPayload()).thenReturn(null);

    // Call method
    customLogger.log(spyLogProperties,
        spyMessageAttributes,
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        mock(ComponentLocation.class),
        correlationId,
        applicationName,
        applicationVersion,
        environment,
        defaultCategory,
        enableV1Compatibility, false);

    // Assertions
    // Assert log properties
    Assert.assertEquals(null, spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertEquals(null, spyMessageAttributes.getOTelContext());
    ArrayList emptyList = new ArrayList();
    Assert.assertEquals(emptyList, spyMessageAttributes.getAttributeList());

  }

  @Test
  public void log_verifyJsonLogWriting() {
    // Initialize, create mocks and spys
    CustomLogger customLogger = new CustomLogger();
    LogProperties logProperties = new LogProperties();
    LogProperties spyLogProperties = spy(logProperties);
    MessageAttributes messageAttributes = new MessageAttributes();
    MessageAttributes spyMessageAttributes = spy(messageAttributes);
    when(spyLogProperties.getPayload()).thenReturn(null);

    // Call method
    customLogger.log(spyLogProperties,
        spyMessageAttributes,
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        mock(ComponentLocation.class),
        correlationId,
        applicationName,
        applicationVersion,
        environment,
        defaultCategory,
        enableV1Compatibility, true);

    // Assertions
    // Assert log properties
    Assert.assertEquals(null, spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertEquals(null, spyMessageAttributes.getOTelContext());
    ArrayList emptyList = new ArrayList();
    Assert.assertEquals(emptyList, spyMessageAttributes.getAttributeList());

  }

  @Test
  public void log_traceAndSpanDataIsInMessageAttributes_test() throws MuleException, IOException {
    CustomLogger customLogger = new CustomLogger();
    LogProperties logProperties = new LogProperties();
    LogProperties spyLogProperties = spy(logProperties);
    MessageAttributes messageAttributes = new MessageAttributes();
    MessageAttributes spyMessageAttributes = spy(messageAttributes);
    String jsonString = "{" +
        "\"traceId\": \"76d5bcae3d49ff2e1b5ace9f0dcbee42\"," +
        "\"spanId\": \"fa6fbe46daf007b9\"," +
        "\"spanIdLong\": \"18045851443427018681\"," +
        "\"traceparent\": \"00-76d5bcae3d49ff2e1b5ace9f0dcbee42-fa6fbe46daf007b9-01\"," +
        "\"TRACE_TRANSACTION_ID\": \"bfacf7c0-d583-11ee-adfa-bcd074a0357f\"," +
        "\"traceIdLongLowPart\": \"1971114969454603842\"" +
        "}";

    ObjectMapper objectMapper = new ObjectMapper();
    Object dataObject = objectMapper.readValue(jsonString, Map.class);
    spyMessageAttributes.setOTelContextObject(dataObject);

    // Call method
    customLogger.log(spyLogProperties,
        spyMessageAttributes,
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        mock(ComponentLocation.class),
        correlationId,
        applicationName,
        applicationVersion,
        environment,
        defaultCategory,
        enableV1Compatibility, false);

    // Assertions
    // Assert log properties
    Assert.assertEquals(null, spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertTrue(spyMessageAttributes.getOTelContextObject() != null);
    Assert.assertEquals(4, spyMessageAttributes.getAttributeList().size());
    Assert.assertEquals("76d5bcae3d49ff2e1b5ace9f0dcbee42", spyMessageAttributes.getAttributes().get("traceId"));
    Assert.assertEquals("1971114969454603842", spyMessageAttributes.getAttributes().get("traceIdLongLowPart"));
    Assert.assertEquals("fa6fbe46daf007b9", spyMessageAttributes.getAttributes().get("spanId"));
    Assert.assertEquals("18045851443427018681", spyMessageAttributes.getAttributes().get("spanIdLong"));
  }
}
