package org.mule.extension;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

public class BasicOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void runFlow() throws Exception {
    flowRunner("dep-testFlow").run();
    System.out.println();
  }
}
