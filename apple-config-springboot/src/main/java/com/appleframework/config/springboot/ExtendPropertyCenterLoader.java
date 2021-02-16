package com.appleframework.config.springboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.PropertyPlaceholderHelper;

import com.appleframework.config.core.Constants;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.util.SystemPropertiesUtil;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

public class ExtendPropertyCenterLoader  {

	private static final Log log = LogFactory.get(ExtendPropertyCenterLoader.class);
	
	private static PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
	
	private static ConfigurerFactory configurerFactory;

	private static void init() throws IOException {
		//load by spi
		try {
			ServiceLoader<ConfigurerFactory> serviceLoader = ServiceLoader.load(ConfigurerFactory.class);
	        Iterator<ConfigurerFactory> iterator = serviceLoader.iterator();
	        if(iterator.hasNext()){
	        	configurerFactory = iterator.next();
	        }
		} catch (Exception e) {
			//load by class.forName
			try {
				Class<?> clazz = Class.forName("com.appleframework.config.PropertyConfigurerFactory");
				configurerFactory = (ConfigurerFactory) clazz.newInstance();
			} catch (Exception e1) {
				//return null;
			}
		}
		
		configurerFactory.init();
	}
	
	public static List<PropertySource<?>> load() throws IOException {
		log.info("load config center ");
		init();

		Properties properties = new Properties();		
		Map<String, Properties> remotePropsMap = configurerFactory.getAllRemoteProperties();
		if(null != remotePropsMap && remotePropsMap.size() > 0) {
			for (Map.Entry<String, Properties> prop : remotePropsMap.entrySet()) {
				Set<Entry<Object, Object>> entrySet = prop.getValue().entrySet();
				String namespace = prop.getKey();
				if(namespace.equalsIgnoreCase(Constants.KEY_NAMESPACE)) {
					for (Entry<Object, Object> entry : entrySet) {
						// local configurer first
						if (configurerFactory.isRemoteFirst() == false ) {
							if(properties.containsKey(entry.getKey())) {
								continue;
							}
						}
						properties.put(entry.getKey(), entry.getValue());
						PropertyConfigurer.add(entry.getKey().toString(), entry.getValue().toString());
					}
				} else {
					for (Entry<Object, Object> entry : entrySet) {
						// local configurer first
						if (configurerFactory.isRemoteFirst() == false ) {
							if(properties.containsKey(entry.getKey()) || properties.containsKey(namespace + "." + entry.getKey())) {
								continue;
							}
						}
						properties.put(entry.getKey(), entry.getValue());
						properties.put(namespace + "." + entry.getKey(), entry.getValue());
						PropertyConfigurer.add(entry.getKey().toString(), entry.getValue().toString());
						PropertyConfigurer.add(namespace, entry.getKey().toString(), entry.getValue().toString());
					}
				}
		    }
		}

		configurerFactory.onLoadFinish(properties);
		
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
			propertySourceList.add(new PropertiesPropertySource("center", properties));
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