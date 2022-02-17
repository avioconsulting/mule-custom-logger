package com.avioconsulting.mule.logger;

import org.junit.Assert;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.event.CoreEvent;

public class CustomLoggerArtifactTest extends MuleArtifactFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "custom-logger-config.xml";
  }

  @Test
  public void testLoggerConfigForCorrelationId() throws Exception {
    //TODO: Intercept logs and validate entries
   CoreEvent coreEvent = flowRunner("custom-logger-configFlow").run();
    Assert.assertNotNull(coreEvent);
  }
}
