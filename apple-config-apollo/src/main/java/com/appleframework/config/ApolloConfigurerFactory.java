package com.appleframework.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appleframework.config.core.PropertyConfigurer;

/**
* 专门提供apple-boot-java以
* -Dconfig-factory=com.appleframework.config.PropertyConfigurerFactory
* 模式启动
* 
* @author xushaomin
*/
public class ApolloConfigurerFactory extends PropertyConfigurerFactory {

	private static Logger logger = LoggerFactory.getLogger(ApolloConfigurerFactory.class);
	
	public ApolloConfigurerFactory() {
		
	}
	
	public ApolloConfigurerFactory(Properties props) {
		convertLocalProperties(props);
	}
	
	public void init() {
		
		// read system.properties
		URL resource = Thread.currentThread().getContextClassLoader().getResource("system.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			try {
				sysConfig.load(new FileReader(new File(resource.getPath())));
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.info("[system.properties] is not exist !");
		}
		
		// read application.properties
		resource = Thread.currentThread().getContextClassLoader().getResource("application.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			try {
				sysConfig.load(new FileReader(new File(resource.getPath())));
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.info("[application.properties] is not exist !");
		}
		
		resource = Thread.currentThread().getContextClassLoader().getResource("bootstrap.properties");
		if (resource != null) {
			Properties sysConfig = new Properties();
			try {
				sysConfig.load(new FileReader(new File(resource.getPath())));
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			PropertyConfigurer.merge(sysConfig);
		} else {
			logger.info("[bootstrap.properties] is not exist !");
		}
		
		super.init();
				
		Properties properties = PropertyConfigurer.getProps();		
		Map<String, Properties> remotePropsMap = getAllRemoteProperties();
		if(null != remotePropsMap && remotePropsMap.size() > 0) {
			for (Map.Entry<String, Properties> prop : remotePropsMap.entrySet()) {
				Set<Entry<Object, Object>> entrySet = prop.getValue().entrySet();
				String namespace = prop.getKey();
				for (Entry<Object, Object> entry : entrySet) {
					// local configurer first
					if (super.isRemoteFirst() == false ) {
						if(properties.containsKey(entry.getKey()) || properties.containsKey(namespace + "." + entry.getKey())) {
							logger.info("config[" + entry.getKey() + "] exists in location,skip~");
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
		super.onLoadFinish(properties);
	}
		
}