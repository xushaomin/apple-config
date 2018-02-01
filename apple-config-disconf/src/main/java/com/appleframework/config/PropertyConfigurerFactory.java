package com.appleframework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.baidu.disconf.client.support.utils.ConfigLoaderUtils;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Logger logger = Logger.getLogger(PropertyConfigurerFactory.class);

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
		Properties properties = new Properties();
		if (!isLoadRemote()) {
			return properties;
		}
		if (this.remotes != null) {
			for (Resource location : this.remotes) {
				String fileName = location.getFilename();
				if (fileName.equals("system.properties") || fileName.equals("application.properties")) {
					continue;
				}
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					Properties fileProperties = ConfigLoaderUtils.loadConfig(fileName);
					if (logger.isInfoEnabled()) {
						logger.info(location + ":\n " + fileProperties);
					}
					properties.putAll(fileProperties);
				} catch (Exception ex) {
					logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
					continue;
				}
			}
		}
		return properties;
	}
	
	@Override
	public String getAllRemoteConfigInfo() {
		String retConfigInfo = null;
		if (!isLoadRemote()) {
			return retConfigInfo;
		}
		if (this.remotes != null) {
			for (Resource location : this.remotes) {
				String fileName = location.getFilename();
				if (fileName.equals("system.properties") || fileName.equals("application.properties")) {
					continue;
				}
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					InputStream inputStream = ConfigLoaderUtils.loadFile(fileName);
					retConfigInfo = this.inputStream2String(inputStream);
					if (logger.isInfoEnabled()) {
						logger.info(location + ":\n " + retConfigInfo);
					}
				} catch (Exception ex) {
					logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
					continue;
				}
			}
		}
		return retConfigInfo;
	}
	
	private String inputStream2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	@Override
	public void onLoadFinish(Properties properties) {
	}

}