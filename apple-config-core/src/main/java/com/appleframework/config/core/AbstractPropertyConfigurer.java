package com.appleframework.config.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;

import com.appleframework.config.core.util.ClassUtil;
import com.appleframework.config.core.util.ObjectUtils;
import com.appleframework.config.core.util.StringUtils;

public abstract class AbstractPropertyConfigurer implements PropertyResolver {

	private static Logger logger = LoggerFactory.getLogger(AbstractPropertyConfigurer.class);

	private static Map<String, Properties> propsMap = new HashMap<String, Properties>();
	
	private boolean ignoreUnresolvableNestedPlaceholders = false;

	protected static String DEFAULT_KEY = "default";

	public static Properties getProps(String namespace) {
		return propsMap.get(namespace);
	}

	public static Properties genProps(String namespace) {
		Properties props = propsMap.get(namespace);
		if (null == props) {
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

	public static void load(String namespace, StringReader reader) {
		try {
			Properties props = genProps(namespace);
			props.load(reader);
			setProps(namespace, props);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static void load(String namespace, InputStream inputStream) {
		try {
			Properties props = genProps(namespace);
			props.load(inputStream);
			setProps(namespace, props);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static void load(String namespace, Properties defaultProps) {
		convertProperties(namespace, defaultProps);
	}

	public static void setProperty(String namespace, String key, String value) {
		try {
			Properties props = genProps(namespace);
			props.setProperty(key, value);
			setProps(namespace, props);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void setPropertyAll(String namespace, String key, String value) {
		try {
			Properties props = genProps(namespace);
			props.setProperty(key, value);
			setProps(namespace, props);

			setProperty(DEFAULT_KEY, key, value);
			setProperty(DEFAULT_KEY, namespace + "." + key, value);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public static void putPropertyAll(String namespace, Object key, Object value) {
		try {
			Properties props = genProps(namespace);
			props.put(key, value);
			setProps(namespace, props);

			put(DEFAULT_KEY, key, value);
			put(DEFAULT_KEY, namespace + "." + key, value);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void removeProperty(String namespace, String key) {
		try {
			Properties props = getProps(namespace);
			if (null != props) {
				props.remove(key);
				setProps(namespace, props);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void removePropertyAll(String namespace, String key) {
		try {
			removeProperty(namespace, key);
			removeProperty(DEFAULT_KEY, key);
			removeProperty(DEFAULT_KEY, namespace + "." + key);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void put(String namespace, Object key, Object value) {
		try {
			Properties props = genProps(namespace);
			if (null != value) {
				props.put(key.toString(), value.toString());
			}
			setProps(namespace, props);
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
	public static void convertProperties(String namespace, Properties defaultProps) {
		Iterator<Entry<Object, Object>> it = defaultProps.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object propertyName = entry.getKey();
			Object propertyValue = entry.getValue();
			if (ObjectUtils.isNotEmpty(propertyName)) {
				put(namespace, propertyName, propertyValue);
			}
		}
	}

	public static Object getObjectProperty(String namespace, String key) {
		Properties props = getProps(namespace);
		if (null != props) {
			return props.get(key);
		} else {
			return null;
		}

	}

	public static String getNSValue(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return (String) object;
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static String getNSValue(String namespace, String key, String defaultValue) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return (String) object;
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultValue;
		}
	}

	public static String getNSString(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return (String) object;
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static String getNSString(String namespace, String key, String defaultString) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return (String) object;
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultString;
		}
	}

	public static Long getNSLong(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Long.parseLong(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Long getNSLong(String namespace, String key, long defaultLong) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Long.parseLong(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultLong;
		}
	}

	public static Integer getNSInteger(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Integer.parseInt(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Integer getNSInteger(String namespace, String key, int defaultInt) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Integer.parseInt(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultInt;
		}
	}

	public static String getNSString(String namespace, String key, Object[] array) {
		String message = getNSValue(namespace, key);
		if (null != message) {
			return MessageFormat.format(message, array);
		} else {
			return null;
		}
	}

	public static String getNSValue(String namespace, String key, Object... array) {
		String message = getNSValue(namespace, key);
		if (null != message) {
			return MessageFormat.format(message, array);
		} else {
			return null;
		}
	}

	public static Boolean getNSBoolean(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Boolean.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Boolean getNSBoolean(String namespace, String key, boolean defaultBoolean) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Boolean.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultBoolean;
		}
	}

	public static Double getNSDouble(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Double.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Double getNSDouble(String namespace, String key, double defaultDouble) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Double.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultDouble;
		}
	}

	public static Short getNSShort(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Short.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Short getNSShort(String namespace, String key, short defaultShort) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Short.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultShort;
		}
	}

	public static Float getNSFloat(String namespace, String key) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Float.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return null;
		}
	}

	public static Float getNSFloat(String namespace, String key, float defaultFloat) {
		Object object = getObjectProperty(namespace, key);
		if (ObjectUtils.isNotEmpty(object)) {
			return Float.valueOf(object.toString());
		} else {
			logger.warn("配置项为" + key + "的配置未在配置中心或项目中添加或设置的内容为空");
			return defaultFloat;
		}
	}

	public synchronized static void merge(String namespace, Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return;
		}
		Properties props = genProps(namespace);
		if (null != properties) {
			props.putAll(properties);
		}
		setProps(namespace, props);
	}

	public synchronized static void add(String namespace, String key, String value) {
		if (StringUtils.isEmptyString(key) || StringUtils.isEmptyString(value)) {
			return;
		}
		Properties props = genProps(namespace);
		if (null != value) {
			props.put(key, value);
		}
		setProps(namespace, props);
	}

	@Override
	public boolean containsProperty(String key) {
		return getProps(DEFAULT_KEY).containsKey(key);
	}

	@Override
	public String getProperty(String key) {
		return getProps(DEFAULT_KEY).getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return getProps(DEFAULT_KEY).getProperty(key, defaultValue);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType) {
		return getProperty(key, targetType, false);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		T value = getProperty(key, targetType);
		return (value != null ? value : defaultValue);
	}

	@Override
	public String getRequiredProperty(String key) throws IllegalStateException {
		return getProperty(key);
	}

	@Override
	public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
		return getProperty(key, targetType);
	}

	@Override
	public String resolvePlaceholders(String text) {
		return text;
	}

	@Override
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		return text;
	}
	
	/**
	 * Resolve placeholders within the given string, deferring to the value of
	 * {@link #setIgnoreUnresolvableNestedPlaceholders} to determine whether any
	 * unresolvable placeholders should raise an exception or be ignored.
	 * <p>Invoked from {@link #getProperty} and its variants, implicitly resolving
	 * nested placeholders. In contrast, {@link #resolvePlaceholders} and
	 * {@link #resolveRequiredPlaceholders} do <i>not</i> delegate
	 * to this method but rather perform their own handling of unresolvable
	 * placeholders, as specified by each of those methods.
	 * @since 3.2
	 * @see #setIgnoreUnresolvableNestedPlaceholders
	 */
	protected String resolveNestedPlaceholders(String value) {
		return (this.ignoreUnresolvableNestedPlaceholders ? resolvePlaceholders(value) : resolveRequiredPlaceholders(value));
	}
	
	/**
	 * Convert the given value to the specified target type, if necessary.
	 * @param value the original property value
	 * @param targetType the specified target type for property retrieval
	 * @return the converted value, or the original value if no conversion
	 * is necessary
	 * @since 4.3.5
	 */
	@SuppressWarnings("unchecked")
	protected <T> T convertValueIfNecessary(Object value, Class<T> targetType) {
		if (targetType == null) {
			return (T) value;
		}
		if (ClassUtil.isAssignableValue(targetType, value)) {
			return (T) value;
		} else {
			return null;
		}
	}
	
	protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
		Object value = getProps(DEFAULT_KEY).getProperty(key);
		if (value != null) {
			if (resolveNestedPlaceholders && value instanceof String) {
				value = resolveNestedPlaceholders((String) value);
			}
			return convertValueIfNecessary(value, targetValueType);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Could not find key '" + key + "' in any property source");
		}
		return null;
	}
	
}