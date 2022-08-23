package com.avioconsulting.mule.logger.api.processor.facade;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;

import java.util.function.Supplier;

/**
 * Helper class that provides an interface to create new instances for properties objects
 */
public class PropertiesFactory {

	private PropertiesFactory() {

	}

	public static Supplier<AdditionalProperties> additionalPropertiesSupplier(){
		return AdditionalProperties::new;
	}

	public static Supplier<ExceptionProperties> exceptionPropertiesSupplier(){
		return ExceptionProperties::new;
	}

	public static Supplier<MessageAttributes> messageAttributesSupplier(){
		return MessageAttributes::new;
	}

	public static Supplier<LogProperties> logPropertiesSupplier(){
		return LogProperties::new;
	}

}
