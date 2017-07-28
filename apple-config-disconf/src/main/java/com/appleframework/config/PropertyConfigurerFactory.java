package com.appleframework.config;

import java.util.Properties;

import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	public PropertyConfigurerFactory() {

	}

	public PropertyConfigurerFactory(Properties props) {
		convertLocalProperties(props);
	}

	public PropertyConfigurerFactory(String fileName) {
		this.systemPropertyFile = fileName;
	}

	public void init() {
		
		Version.logVersion();

		initSystemProperties();

		initEventListener();

	}

}