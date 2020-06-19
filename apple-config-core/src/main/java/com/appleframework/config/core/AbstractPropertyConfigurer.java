package com.appleframework.config.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appleframework.config.core.util.ObjectUtils;
import com.appleframework.config.core.util.StringUtils;

public abstract class AbstractPropertyConfigurer {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractPropertyConfigurer.class);
		
	private static Map<String, Properties> propsMap = new HashMap<String, Properties>();
		
	public static Properties getProps(String namespace) {
		Properties props = propsMap.get(namespace);
		if(null == props) {
			props = new Properties();
			propsMap.put(namespace, props);
		}
		return props;
	}
	
	public static Map<String, Properties> getPropsMap() {
		return propsMap;
	}
	
	public static void setProps(String namespace, Properties props) {
		propsMap.put(namespace, props);
	}
	
	
	public static void load(String namespace, StringReader reader){
		try {
			Properties props = getProps(namespace);
			props.load(reader);
			setProps(namespace, props);
		} catch (IOException e) {			
			logger.error(e.getMessage());
		}
	}
	
	public static void load(String namespace, InputStream inputStream){
		try {
			Properties props = getProps(namespace);
			props.load(inputStream);
			setProps(namespace, props);
		} catch (IOException e) {			
			logger.error(e.getMessage());
		}
	}
	
	public static void load(String namespace, Properties defaultProps){
		convertProperties(namespace, defaultProps);
	}
	
	public static void setProperty(String namespace, String key, String value) {
		try {
			Properties props = getProps(namespace);
			props.setProperty(key, value);
			setProps(namespace, props);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void put(String namespace, Object key, Object value) {
		try {
			Properties props = getProps(namespace);
			if(null != value) {
				props.put(key.toString(), value.toString());
			}
			setProps(namespace, props);			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Convert the given merged properties, converting property values
	 * if necessary. The result will then be processed.
	 * <p>The default implementation will invoke {@link #convertPropertyValue}
	 * for each property value, replacing the original with the converted value.
	 * @param defaultProps the Properties to convert
	 * @see #processProperties
	 */
	public static void convertProperties(String namespace, Properties defaultProps) {
		Enumeration<?> propertyNames = defaultProps.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = defaultProps.getProperty(propertyName);
			if (ObjectUtils.isNotEmpty(propertyName)) {
				setProperty(namespace, propertyName, propertyValue);
			}
		}
	}

	public static Object getNSProperty(String namespace, String key) {
		Properties props = getProps(namespace);
		if(null != props) {
			return props.get(key);
		}
		else {
			return null;
		}
		
	}
	
	public static String getNSValue(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object) {
			return (String)object;
		}
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static String getNSValue(String namespace, String key, String defaultValue) {
		Object object = getNSProperty(namespace, key);
		if(null != object) {
			return (String)object;
		}
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultValue;
		}
	}
	
	public static String getNSString(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object) {
			return (String)object;
		}
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static String getNSString(String namespace, String key, String defaultString) {
		Object object = getNSProperty(namespace, key);
		if(null != object) {
			return (String)object;
		}
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultString;
		}
	}
	
	public static Long getNSLong(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Long.parseLong(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static Long getNSLong(String namespace, String key, long defaultLong) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Long.parseLong(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultLong;
		}
	}
	
	public static Integer getNSInteger(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object) {
			return Integer.parseInt(object.toString());
		}
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static Integer getNSInteger(String namespace, String key, int defaultInt) {
		Object object = getNSProperty(namespace, key);
		if(null != object) {
			return Integer.parseInt(object.toString());
		}
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultInt;
		}
	}
	
	public static String getNSString(String namespace, String key, Object[] array) {
		String message = getNSValue(namespace, key);
		if(null != message) {
			return MessageFormat.format(message, array);  
		}
		else {
			return null;
		}
	}
	
	public static String getNSValue(String namespace, String key, Object... array) {
		String message = getNSValue(namespace, key);
		if(null != message) {
			return MessageFormat.format(message, array);  
		}
		else {
			return null;
		}
	}
	
	public static Boolean getNSBoolean(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Boolean.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static Boolean getNSBoolean(String namespace, String key, boolean defaultBoolean) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Boolean.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultBoolean;
		}
	}
	
	public static Double getNSDouble(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Double.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static Double getNSDouble(String namespace, String key, double defaultDouble) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Double.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultDouble;
		}
	}
	
	public static Short getNSShort(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Short.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static Short getNSShort(String namespace, String key, short defaultShort) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Short.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultShort;
		}
	}
	
	public static Float getNSFloat(String namespace, String key) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Float.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}
	
	public static Float getNSFloat(String namespace, String key, float defaultFloat) {
		Object object = getNSProperty(namespace, key);
		if(null != object)
			return Float.valueOf(object.toString());
		else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultFloat;
		}
	}
	
	public synchronized static void merge(String namespace, Properties properties){
		if (properties == null || properties.isEmpty()) {
			return;
		}
		Properties props = getProps(namespace);
		if(null != properties) {
			props.putAll(properties);
		}
		setProps(namespace, props);
	}
	
	public synchronized static void add(String namespace, String key, String value) {
		if (StringUtils.isEmptyString(key) || StringUtils.isEmptyString(value)) {
			return;
		}
		Properties props = getProps(namespace);
		if(null != value) {
			props.put(key, value);
		}
		setProps(namespace, props);
	}
	
	
}