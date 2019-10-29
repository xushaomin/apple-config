package com.appleframework.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.StringUtils;

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
				
		Properties remoteProperties = null;
		
		String configInfo = getAllRemoteConfigInfo();
		if(null == configInfo) {
			remoteProperties = getAllRemoteProperties();
		}
		else {
			PropertyConfigurer.setConfigInfo(configInfo);
			remoteProperties = this.changeToProperties(configInfo);
		}
		
		if (remoteProperties != null) {
			Set<Entry<Object, Object>> entrySet = remoteProperties.entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				// local configurer first
				if (super.isRemoteFirst() == false && PropertyConfigurer.containsKey(entry.getKey().toString())) {
					logger.info("config[" + entry.getKey() + "] exists in location,skip~");
					continue;
				}
				PropertyConfigurer.add(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		super.onLoadFinish(remoteProperties);
	}
	
	private Properties changeToProperties(String configInfo) {
		Properties properties = new Properties();
		try {
			if (!StringUtils.isEmpty(configInfo)) {
				properties.load(new StringReader(configInfo));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return properties;
	}
	
}