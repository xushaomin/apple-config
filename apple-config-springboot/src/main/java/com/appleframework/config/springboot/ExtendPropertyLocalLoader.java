package com.appleframework.config.springboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.SystemPropertiesUtil;
import com.appleframework.config.springboot.utils.YamlLoaderUtils;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

public class ExtendPropertyLocalLoader  {

	private static final Log log = LogFactory.get(ExtendPropertyLocalLoader.class);

	private static PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
		
	private static boolean isPropertiesResource(String name) {
		if(name.indexOf("properties") > -1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static List<PropertySource<?>> load(String name, Resource resource) throws IOException {
		log.info("load local resource file {}", name);
		
		Properties properties = new Properties();
		if(isPropertiesResource(name)) {
			properties = PropertiesLoaderUtils.loadProperties(resource);
		}
		else {
			properties = YamlLoaderUtils.loadProperties(resource);
		}
		if(properties.isEmpty()) {
			return null;
		}
		printProperties(properties);
		
		log.info("======================================================================");
		
		Set<String> propertyNames = properties.stringPropertyNames();
		
		for (String key : propertyNames) {
			String value = properties.getProperty(key);
			value = propertyPlaceholderHelper.replacePlaceholders(value, properties);
			if(null == value) {
				continue;
			}
			properties.put(key, value);
		}
		
		SystemPropertiesUtil.set(properties);
		
		PropertyConfigurer.merge(properties);
		
		if (!properties.isEmpty()) {
			List<PropertySource<?>> propertySourceList = new ArrayList<PropertySource<?>>();

			if(isPropertiesResource(name)) {
				propertySourceList.add(new PropertiesPropertySource(name, properties));
			}
			else {
				propertySourceList.add(new PropertiesPropertySource(name, properties));
				propertySourceList.addAll(new YamlPropertySourceLoader().load(resource.getFilename(), resource));
			}
			return propertySourceList;
		}
		else {
			return null;
		}
	}
	
	private static void printProperties(Properties properties) {		
		Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Object, Object> entry = iterator.next();
			Object propertyName = entry.getKey();
			Object propertyValue = entry.getValue();
			if(propertyName.toString().contains("password")) {
				log.info("    " + propertyName + "= ******");
			}
			else {
				log.info("    " + propertyName + "=" + propertyValue);
			}
		}
	}
	
}
