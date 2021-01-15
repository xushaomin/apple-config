package com.appleframework.config.core;

import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appleframework.config.core.exception.ConfigException;
import com.appleframework.config.core.util.ObjectUtils;
import com.appleframework.config.core.util.StringUtils;

public class PropertyConfigurer extends AbstractPropertyConfigurer {

	private static Logger logger = LoggerFactory.getLogger(PropertyConfigurer.class);

	private static String configInfo;

	public static Properties getProps() {
		return getProps(DEFAULT_KEY);
	}

	public static void setProps(String key, String value) {
		setProperty(DEFAULT_KEY, key, value);
	}

	public static void load(String configInfo) {
		PropertyConfigurer.configInfo = configInfo;
		load(new StringReader(configInfo));
	}

	public static void load(StringReader reader) {
		load(DEFAULT_KEY, reader);
	}

	public static void load(InputStream inputStream) {
		load(DEFAULT_KEY, inputStream);
	}

	public static void load(Properties defaultProps) {
		convertProperties(defaultProps);
	}

	public static void setProperty(String key, String value) {
		try {
			setProps(key, value);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void put(Object key, Object value) {
		try {
			put(DEFAULT_KEY, key, value);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Convert the given merged properties, converting property values if necessary.
	 * The result will then be processed.
	 * <p>
	 * The default implementation will invoke {@link #convertPropertyValue} for each
	 * property value, replacing the original with the converted value.
	 * 
	 * @param defaultProps the Properties to convert
	 * @see #processProperties
	 */
	public static void convertProperties(Properties defaultProps) {
		Iterator<Entry<Object, Object>> it = defaultProps.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object propertyName = entry.getKey();
			Object propertyValue = entry.getValue();
			if (ObjectUtils.isNotEmpty(propertyName)) {
				put(propertyName, propertyValue);
			}
		}
	}

	public static Object getObjectProperty(String key) {
		Properties properties = getProps();
		if (null == properties) {
			return null;
		}
		return properties.get(key);
	}

	public static String getValue(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return (String) object;
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static String getValue(String key, String defaultValue) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return (String) object;
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultValue;
		}
	}

	public static String getString(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return String.valueOf(object);
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static String getString(String key, String defaultString) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return String.valueOf(object);
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultString;
		}
	}

	public static Long getLong(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Long.parseLong(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Long getLong(String key, long defaultLong) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Long.parseLong(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultLong;
		}
	}

	public static Integer getInteger(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Integer.parseInt(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Integer getInteger(String key, int defaultInt) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Integer.parseInt(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultInt;
		}
	}

	public static String getString(String key, Object[] array) {
		String message = getValue(key);
		if (null != message) {
			return MessageFormat.format(message, array);
		} else {
			return null;
		}
	}

	public static String getValue(String key, Object... array) {
		String message = getValue(key);
		if (null != message) {
			return MessageFormat.format(message, array);
		} else {
			return null;
		}
	}

	public static Boolean getBoolean(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Boolean.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Boolean getBoolean(String key, boolean defaultBoolean) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Boolean.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultBoolean;
		}
	}

	public static Double getDouble(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Double.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Double getDouble(String key, double defaultDouble) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Double.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultDouble;
		}
	}

	public static Short getShort(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Short.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Short getShort(String key, short defaultShort) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Short.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultShort;
		}
	}

	public static Float getFloat(String key) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Float.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Float getFloat(String key, float defaultFloat) {
		Object object = getObjectProperty(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Float.valueOf(object.toString());
		} else {
			logger.debug("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultFloat;
		}
	}
	
    public static long getDuration(String key, TimeUnit unit) {
    	String object = getValue(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return unit.convert(
                    parseDuration(object),
                    TimeUnit.NANOSECONDS);
		} else {
			throw new ConfigException("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
		}
    }

	public static Duration getDuration(String key) {
		String object = getValue(key);
		if (ObjectUtils.isNotEmpty(object)) {
			long nanos = parseDuration(object);
			return Duration.ofNanos(nanos);
		} else {
			throw new ConfigException("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
		}
	}
	
	public static long getDuration(String key, TimeUnit unit, long defaultLong) {
    	String object = getValue(key);
		if (ObjectUtils.isNotEmpty(object)) {
			return unit.convert(
                    parseDuration(object),
                    TimeUnit.NANOSECONDS);
		} else {
			return defaultLong;
		}
    }

	public static Duration getDuration(String key, TimeUnit unit, Duration defaultUnit) {
		String object = getValue(key);
		if (ObjectUtils.isNotEmpty(object)) {
			long nanos = parseDuration(object);
			return Duration.ofNanos(nanos);
		} else {
			return defaultUnit;
		}
	}

	public synchronized static void merge(Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return;
		}
		merge(DEFAULT_KEY, properties);
	}

	public synchronized static void add(String key, String value) {
		if (StringUtils.isEmptyString(key) || StringUtils.isEmptyString(value)) {
			return;
		}
		add(DEFAULT_KEY, key, value);
	}

//	public static boolean containsProperty(String key) {
//		return getProps().containsKey(key);
//	}

	public static boolean containsKey(String key) {
		return getProps().containsKey(key);
	}

	public static String getConfigInfo() {
		return configInfo;
	}

	public static void setConfigInfo(String configInfo) {
		PropertyConfigurer.configInfo = configInfo;
	}

	public static void setProps(Properties props) {
		setProps(DEFAULT_KEY, props);
	}
	
}