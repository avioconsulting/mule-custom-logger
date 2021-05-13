package com.avio.customlogger.utils;

public class CustomLoggerConstants {
    public static final String DEFAULT_CATEGORY_PREFIX = "com.avioconsulting";
    public static final String DEFAULT_CATEGORY_SUFFIX = ".category";
    public static final String DEFAULT_APP_NAME = "#[app.name]";
    public static final String EXAMPLE_APP_VERSION = "#[p('appVersion')]";
    public static final String DEFAULT_ENV = "#[p('env')]";
    public static final String DEFAULT_EXCEPTION_TYPE = "#[if (error != null) ((error.errorType.namespace default '') ++ \":\" ++ (error.errorType.identifier default '')) else null]";
    public static final String DEFAULT_EXCEPTION_DETAIL = "#[if (error != null) (error.detailedDescription) else null]";
}
