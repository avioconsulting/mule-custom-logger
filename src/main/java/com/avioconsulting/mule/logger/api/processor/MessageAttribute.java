package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.Alias;

@Alias("attribute")
public class MessageAttribute extends KeyValuePair {

  public MessageAttribute() {
    super();
  }

  public MessageAttribute(String key, String value) {
    super(key, value);
  }
}
