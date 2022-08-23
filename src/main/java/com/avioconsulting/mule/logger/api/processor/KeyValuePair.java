package com.avioconsulting.mule.logger.api.processor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * Represents key-value pair properties for message attributes
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class KeyValuePair {

  @Parameter
  private String key;

  @Parameter
  private String value;

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    KeyValuePair that = (KeyValuePair) o;
    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return key + "=" + value;
  }

  public static String keyValuePairToCSV(List<? extends KeyValuePair> pairs) {
    if (CollectionUtils.isEmpty(pairs))
      return StringUtils.EMPTY;
    return pairs.stream()
            .map(KeyValuePair::toString)
            .collect(Collectors.joining(","));
  }
}
