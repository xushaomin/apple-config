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

	public void init() {

		Version.logVersion();

		initSystemProperties();

		initEventListener();

	}

	@Override
	public void close() {
	}

	@Override
	public Properties getAllRemoteProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinish(Properties properties) {
		// TODO Auto-generated method stub

	}

}