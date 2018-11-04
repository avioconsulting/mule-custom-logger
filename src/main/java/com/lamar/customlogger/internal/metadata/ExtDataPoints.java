package com.lamar.customlogger.internal.metadata;

import java.util.Map;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

public class ExtDataPoints {

	@Parameter
	@Optional
	@DisplayName("")
	private Map<String, String> ext;

	public Map<String, String> getExt() {
		return ext;
	}

	public void setExt(Map<String, String> ext) {
		this.ext = ext;
	}

}
