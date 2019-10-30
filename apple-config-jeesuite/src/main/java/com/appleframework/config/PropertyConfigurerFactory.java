package com.appleframework.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.json.JsonUtils;
import com.appleframework.config.utils.HttpUtils;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static PropertyConfigurerFactory instance = new PropertyConfigurerFactory();
	
	private ScheduledExecutorService scheduledExecutor;

	private String apiBaseUrl = "http://configinfo.appleframework.com";
	private String app;
	private String env;
	private String version;
	private String contentMd5;
	
	public PropertyConfigurerFactory() {}

	public void init() {
		
		Version.logVersion();
				
		initSystemProperties();

		initEventListener();
		
		initRemotePropertieManager();
	}
	
	private void initRemotePropertieManager() {
		
		String defaultAppName = getValue("spring.application.name");
		app = getValue("application.name", defaultAppName);
		
		String defaultConfigRemote = getValue("spring.config.remote");
		String configRemote = getValue("config.remote", defaultConfigRemote);
		if(null != configRemote) {
			loadRemote = Boolean.parseBoolean(configRemote);
		}

		String defaultEnv = getValue("spring.profiles.active");
		env = getValue("deploy.env", defaultEnv);

		setApiBaseUrl(getValue("deploy.confHost"));

		version = getValue("deploy.version", "0.0.0");

		scheduledExecutor = new ScheduledThreadPoolExecutor(1);
		scheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				String md5 = getRemoteMd5(app, env, version);
				if (null != md5 && !contentMd5.equals(md5)) {
					Properties remoteProperties = getAllRemoteProperties();
					PropertyConfigurer.merge(remoteProperties);
					notifyPropertiesChanged(remoteProperties);
				}
			}
		}, 60, 60, TimeUnit.SECONDS);
	}
	

	public static PropertyConfigurerFactory getInstance() {
		return instance;
	}
	

	public String getApiBaseUrl() {
		return apiBaseUrl;
	}

	public void setApiBaseUrl(String apiBaseUrl) {
		if (apiBaseUrl != null) {
			if (apiBaseUrl.endsWith("/")) {
				apiBaseUrl = apiBaseUrl.substring(0, apiBaseUrl.length() - 1);
			}
			if(!apiBaseUrl.startsWith("http://")) {
				apiBaseUrl = "http://" + apiBaseUrl;
			}
			this.apiBaseUrl = apiBaseUrl;
		}
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isRemoteFirst() {
		return remoteFirst;
	}
	
	private String getRemoteContent(String url) {
		String content = null;
		try {
			content = HttpUtils.getContent(url);
		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (Exception e2) {
			}
			content = HttpUtils.getContent(url);
		}
		return content;
	}
	
	private String getRemoteConfig(String app, String env, String version) {
		String formatUrl = "%s/api/fetch_all_configs?appName=%s&env=%s&version=%s";
		String url = String.format(formatUrl, apiBaseUrl, app, env, version);
		return getRemoteContent(url);
	}
	
	private String getRemoteMd5(String app, String env, String version) {
		String formatUrl = "%s/api/check_config_update?appName=%s&env=%s&version=%s";
		String url = String.format(formatUrl, apiBaseUrl, app, env, version);
		return getRemoteContent(url);
	}

	@SuppressWarnings("unchecked")
	public Properties getAllRemoteProperties() {
		if (!loadRemote) {
			return null;
		}
		if (StringUtils.isBlank(apiBaseUrl)) {
			return null;
		}
		Properties properties = new Properties();

		String jsonString = getRemoteConfig(app, env, version);
		Map<String, String> map = JsonUtils.toObject(jsonString, Map.class);
		if (map.containsKey("code")) {
			throw new RuntimeException(map.get("msg"));
		}

		Set<String> keys = map.keySet();
		for (String key : keys) {
			String configInfo = map.get(key);
			if (!StringUtils.isEmpty(configInfo)) {
				try {
					properties.load(new StringReader(configInfo));
				} catch (IOException e) {
				}
			}
			contentMd5 = key;
		}

		return properties;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getAllRemoteConfigInfo() {
		if (!loadRemote) {
			return null;
		}
		if (StringUtils.isBlank(apiBaseUrl)) {
			return null;
		}
		String retConfigInfo = "";

		String jsonString = getRemoteConfig(app, env, version);
		Map<String, String> map = JsonUtils.toObject(jsonString, Map.class);
		if (map.containsKey("code")) {
			throw new RuntimeException(map.get("msg"));
		}

		Set<String> keys = map.keySet();
		for (String key : keys) {
			String configInfo = map.get(key);
			if (!StringUtils.isEmpty(configInfo)) {
				retConfigInfo += "\n" + configInfo;
			}
			contentMd5 = key;
		}

		return retConfigInfo;
	}

	public void onLoadFinish(Properties properties){
		setSystemProperty(properties);
	}
	
	public void close() {
		scheduledExecutor.shutdown();
	}
		
	private String getValue(String key) {
		return getValue(key, null);
	}

	private String getValue(String key, String defVal) {
		String value = StringUtils.trimToNull(PropertyConfigurer.getString(key, defVal));
		if (StringUtils.isNotBlank(value)) {
			if (value.startsWith("${")) {
				String refKey = value.substring(2, value.length() - 1).trim();
				value = PropertyConfigurer.getString(refKey);
			}
		}
		return value;
	}
	
	@Override
	public Map<String, Properties> getAllRemotePropertiesMap() {
		return null;
	}
}
