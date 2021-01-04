package com.appleframework.config.springboot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.PropertyPlaceholderHelper;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.util.SystemPropertiesUtil;

public class ExtendPropertyApolloLoader  {

	private static Logger log = LoggerFactory.getLogger(ExtendPropertyApolloLoader.class);
	
	private static ConfigurerFactory configurerFactory = null;
				
	private static PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
		
	private static void initConfigurerFactory() {
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
		configurerFactory.setRemoteFirst(true);
		configurerFactory.init();
	}
	
	
	public static List<PropertySource<?>> load(Properties properties) {
		log.info("======================================================================");

		initConfigurerFactory();
		
		log.info("======================================================================");

		Map<String, Properties> remotePropsMap = configurerFactory.getAllRemoteProperties();
		if(null != remotePropsMap && remotePropsMap.size() > 0) {
			for (Map.Entry<String, Properties> prop : remotePropsMap.entrySet()) {
				Set<Entry<Object, Object>> entrySet = prop.getValue().entrySet();
				String namespace = prop.getKey();
				for (Entry<Object, Object> entry : entrySet) {
					// local configurer first
					Object key = entry.getKey();
					Object value = entry.getValue();
					SystemPropertiesUtil.judgeSet(key, value);
					if (configurerFactory.isRemoteFirst() == false ) {
						if(properties.containsKey(key) || properties.containsKey(namespace + "." + key)) {
							continue;
						}
					} else {
						if(properties.containsKey(key)) {
							log.info("the remote properties overwrite the load local properties {}", key);
							//continue;
						}
						if(properties.containsKey(namespace + "." + key)) {
							log.info("the remote properties overwrite the load local properties {}", namespace + "." + key);
							//continue;
						}
					}
					
					properties.put(key, value);
					properties.put(namespace + "." + key, value);
					PropertyConfigurer.setPropertyAll(namespace, key.toString(), value.toString());
				}
		    }
		}
		
		SystemPropertiesUtil.set(properties);
		
		Set<String> propertyNames = properties.stringPropertyNames();
		
		//占位符
		for (String key : propertyNames) {
			String value = properties.getProperty(key);
			value = propertyPlaceholderHelper.replacePlaceholders(value, properties);
			properties.put(key, value);
		}
		
		Map<String, Properties> propsMap = PropertyConfigurer.getPropsMap();
		for (Map.Entry<String, Properties> props : propsMap.entrySet()) {
			String namespace =  props.getKey();
			Properties popValue = props.getValue();
			
			propertyNames = popValue.stringPropertyNames();
			for (String key : propertyNames) {
				String value = popValue.getProperty(key);
				if(null == value) {
					continue;
				}
				try {
					value = propertyPlaceholderHelper.replacePlaceholders(value, popValue);
					PropertyConfigurer.setPropertyAll(namespace, key, value);
				} catch(Exception e) {
					log.error("占位符处理出错");
				}					
			}
	    }
		
		Properties notifyProps = configurerFactory.onLoadFinish(propsMap);
		if(null != notifyProps && !notifyProps.isEmpty()) {
			properties.putAll(notifyProps);
		}

		if (!properties.isEmpty()) {
			List<PropertySource<?>> propertySourceList = new ArrayList<PropertySource<?>>();
			propertySourceList.add(new PropertiesPropertySource("apollo", properties));
			return propertySourceList;
		}
		else {
			return null;
		}
	}

}
