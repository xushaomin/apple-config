package com.appleframework.config.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.Resource;

public class YamlLoaderUtils {

	public static Properties loadProperties(Resource resource) {
		Properties properties = new Properties();
		try {
			Map<String, Object> data = YamlUtils.yamlHandler(resource);
			properties.putAll(data);
		} catch (IOException e) {
		}
		return properties;
	}
}
