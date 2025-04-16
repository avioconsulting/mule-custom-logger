package com.avioconsulting.mule.logger;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.event.CoreEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CustomLoggerArtifactTest extends MuleArtifactFunctionalTestCase {

  private final Path UNIT_TEST_LOG_PATH = Paths.get("unit-test.log");

  @Before
  public void beforeTest() {
    Awaitility.reset();
    Awaitility.setDefaultPollDelay(100, MILLISECONDS);
    Awaitility.setDefaultPollInterval(2, SECONDS);
    Awaitility.setDefaultTimeout(30, SECONDS);

  }

  @Override
  protected String getConfigFile() {
    return "custom-logger-config.xml";
  }

  @Before
  public void cleanup() throws Exception {
    Files.write(UNIT_TEST_LOG_PATH, "".getBytes());
  }

  @Test
  public void testLoggerConfigForCorrelationId() throws Exception {
    // TODO: Intercept logs and validate entries
    CoreEvent coreEvent = flowRunner("custom-logger-configFlow")
        .withAttributes(Collections.singletonMap("some", "value")).run();
    Assert.assertNotNull(coreEvent);
  }

  @Test
  @Ignore(value = "When run individually, it succeeds. Running multiple tests doesn't work well for test file cleanup and assertion. TODO: Check")
  public void testFlowRefNotification() throws Exception {
    // TODO: Intercept logs and validate entries
    CoreEvent coreEvent = flowRunner("custom-logger-flow-ref").run();
    Assert.assertNotNull(coreEvent);
    Awaitility.await().untilAsserted(() -> Assertions.assertThat(Files.readAllLines(UNIT_TEST_LOG_PATH))
        .filteredOn(line -> line.contains("Flow-ref with target [simple-subflow] start")
            || line.contains("Flow-ref with target [simple-subflow] end"))
        .hasSize(2));
  }
}
