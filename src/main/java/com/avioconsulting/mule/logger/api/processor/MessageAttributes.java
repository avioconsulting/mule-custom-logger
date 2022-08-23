package com.avioconsulting.mule.logger.api.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@Getter
public class MessageAttributes {

  @Parameter
  @DisplayName("Message Attributes")
  @Optional
  @NullSafe
  @Summary("Discrete data elements you want to log as key value pairs.  Useful for adding data fields for reporting in log aggregation tools")
  private List<MessageAttribute> messageAttributeList;

  public MessageAttributes() {
    this.messageAttributeList = new ArrayList<>();
  }

  //TODO defensive copy
  public LinkedHashMap<String, String> getAttributesAsMapCopy() {
    return messageAttributeList.stream()
            .collect(Collectors.toMap(MessageAttribute::getKey, MessageAttribute::getValue,(e1, e2) -> e2,
                    LinkedHashMap::new));
  }

}
