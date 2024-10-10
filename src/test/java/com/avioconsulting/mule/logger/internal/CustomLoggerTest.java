package com.avioconsulting.mule.logger.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.TypedComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.dsl.api.component.config.DefaultComponentLocation;
import org.mule.runtime.module.extension.internal.runtime.resolver.StaticParameterResolver;

public class CustomLoggerTest {

  private String examplePayload;
  private String correlationId;
  private CustomLoggerConfiguration loggerConfiguration;

  @Before
  public void init() {
    examplePayload = "Example Payload Text";
    correlationId = "ab9195f8-0ff6-4611-ab78-63c60c824c95";
    String applicationName = "example-app-name";
    String applicationVersion = "1.0.0";
    String environment = "dev";
    String defaultCategory = "com.avioconsulting.mule";
    Boolean enableV1Compatibility = false;

    loggerConfiguration = mock(CustomLoggerConfiguration.class);
    when(loggerConfiguration.getApplicationName()).thenReturn(applicationName);
    when(loggerConfiguration.getApplicationVersion()).thenReturn(applicationVersion);
    when(loggerConfiguration.getEnvironment()).thenReturn(environment);
    when(loggerConfiguration.getDefaultCategory()).thenReturn(defaultCategory);
    when(loggerConfiguration.isEnableV1Compatibility()).thenReturn(enableV1Compatibility);
    when(loggerConfiguration.isFormatAsJson()).thenReturn(false);

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
        loggerConfiguration,
        mock(ComponentLocation.class),
        correlationId);

    // Assertions
    // Assert log properties
    Assert.assertNull(spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertNull(spyMessageAttributes.getOTelContext());
    ArrayList emptyList = new ArrayList();
    Assert.assertEquals(emptyList, spyMessageAttributes.getAttributeList());

  }

  @Test
  public void log_verifyLogPropertiesWithPayloadAttributes_test() {
    // Initialize, create mocks and spys
    CustomLogger customLogger = new CustomLogger();
    LogProperties logProperties = new LogProperties();
    logProperties.setPayload(new StaticParameterResolver<>(examplePayload));
    LogProperties spyLogProperties = spy(logProperties);
    MessageAttributes messageAttributes = new MessageAttributes();
    MessageAttributes spyMessageAttributes = spy(messageAttributes);
    when(spyLogProperties.getPayload()).thenReturn(null);

    // Call method
    customLogger.log(spyLogProperties,
        spyMessageAttributes,
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfiguration,
        mock(ComponentLocation.class),
        correlationId);

    // Assertions
    // Assert log properties
    Assert.assertNull(spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertNull(spyMessageAttributes.getOTelContext());
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
    when(loggerConfiguration.isFormatAsJson()).thenReturn(true);
    // Call method
    customLogger.log(spyLogProperties,
        spyMessageAttributes,
        mock(ExceptionProperties.class),
        mock(AdditionalProperties.class),
        loggerConfiguration,
        mock(ComponentLocation.class),
        correlationId);

    // Assertions
    // Assert log properties
    Assert.assertNull(spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertNull(spyMessageAttributes.getOTelContext());
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
        loggerConfiguration,
        mock(ComponentLocation.class),
        correlationId);

    // Assertions
    // Assert log properties
    Assert.assertNull(spyLogProperties.getPayload());
    // Assert Message Attributes
    Assert.assertNotNull(spyMessageAttributes.getOTelContextObject());
    Assert.assertEquals(4, spyMessageAttributes.getAttributeList().size());
    Assert.assertEquals("76d5bcae3d49ff2e1b5ace9f0dcbee42", spyMessageAttributes.getAttributes().get("traceId"));
    Assert.assertEquals("1971114969454603842", spyMessageAttributes.getAttributes().get("traceIdLongLowPart"));
    Assert.assertEquals("fa6fbe46daf007b9", spyMessageAttributes.getAttributes().get("spanId"));
    Assert.assertEquals("18045851443427018681", spyMessageAttributes.getAttributes().get("spanIdLong"));
  }

  @Test
  public void getLocationInformation() {

    DefaultComponentLocation.DefaultLocationPart root = new DefaultComponentLocation.DefaultLocationPart(
        "root-flow",
        Optional.ofNullable(TypedComponentIdentifier.builder()
            .identifier(ComponentIdentifier.builder().name("flow").namespace("mule").build())
            .type(TypedComponentIdentifier.ComponentType.FLOW)
            .build()),
        Optional.of("test-config.xml"),
        Optional.of(1),
        Optional.of(1));
    DefaultComponentLocation.DefaultLocationPart part = new DefaultComponentLocation.DefaultLocationPart("1",
        Optional.ofNullable(TypedComponentIdentifier.builder()
            .identifier(ComponentIdentifier.builder().name("logger").namespace("mule").build())
            .type(TypedComponentIdentifier.ComponentType.OPERATION)
            .build()),
        Optional.of("test-config.xml"),
        Optional.of(10),
        Optional.of(1));
    DefaultComponentLocation defaultComponentLocation = new DefaultComponentLocation(Optional.of("mule:logger"),
        Arrays.asList(root, part));
    Map<String, String> locationInformation = CustomLogger.getLocationInformation(defaultComponentLocation);
    Assertions.assertThat(locationInformation)
        .containsEntry("component", "logger")
        .containsEntry("location", "root-flow/1")
        .containsEntry("rootContainer", "root-flow")
        .containsEntry("lineInFile", "10")
        .containsEntry("fileName", "test-config.xml");

  }

}
