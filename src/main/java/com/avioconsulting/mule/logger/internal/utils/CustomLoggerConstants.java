package com.avioconsulting.mule.logger.internal.utils;

public class CustomLoggerConstants {
    public static final String DEFAULT_CATEGORY_SUFFIX = ".category";
    public static final String DEFAULT_EXCEPTION_TYPE = "#[if (error != null) ((error.errorType.namespace default '') ++ \":\" ++ (error.errorType.identifier default '')) else null]";
    public static final String DEFAULT_EXCEPTION_DETAIL = "#[if (error != null) (error.detailedDescription) else null]";
}
