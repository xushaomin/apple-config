package com.appleframework.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.appleframework.config.core.Constants;
import com.appleframework.config.core.EnvConfigurer;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.ObjectUtils;
import com.appleframework.config.core.util.StringUtils;
import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;

public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private static Logger logger = Logger.getLogger(ExtendedPropertyPlaceholderConfigurer.class);

	private String KEY_DEPLOY_GROUP = "deploy.group";
	private String KEY_DEPLOY_DATAID = "deploy.dataId";

	private Properties props;

	private String eventListenerClass;

	private ManagerListener eventListener;

	private boolean loadRemote = true;

	public boolean isLoadRemote() {
		return loadRemote;
	}

	public void setLoadRemote(boolean loadRemote) {
		this.loadRemote = loadRemote;
	}

	public void setEventListenerClass(String eventListenerClass) {
		this.eventListenerClass = eventListenerClass;
	}

	public void setEventListener(ManagerListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
		Version.logVersion();

		// 获取启动启动-D参数
		Properties systemProps = System.getProperties();
		Enumeration<?> systemEnum = systemProps.keys();
		while (systemEnum.hasMoreElements()) {
			String systemKey = systemEnum.nextElement().toString();
			if (!Constants.SET_SYSTEM_PROPERTIES.contains(systemKey)) {
				String systemValue = systemProps.getProperty(systemKey);
				props.setProperty(systemKey, systemValue);
			}
		}

		if (!isLoadRemote()) {
			super.processProperties(beanFactory, props);
			this.props = props;
			PropertyConfigurer.load(props);
			return;
		}

		String group = props.getProperty(KEY_DEPLOY_GROUP);
		String dataId = props.getProperty(KEY_DEPLOY_DATAID);

		logger.warn("配置项：group=" + group);
		logger.warn("配置项：dataId=" + dataId);

		if (!StringUtils.isEmpty(group) && !StringUtils.isEmpty(dataId)) {
			String env = this.getDeployEnv(props);
			if (!StringUtils.isEmpty(env)) {
				dataId += "-" + env;
				logger.warn("配置项：env=" + env);
			}

			List<ManagerListener> managerListeners = new ArrayList<>();

			ManagerListener springMamagerListener = new ManagerListener() {

				public Executor getExecutor() {
					return null;
				}

				public void receiveConfigInfo(String configInfo) {
					// 客户端处理数据的逻辑
					logger.warn("已改动的配置：\n" + configInfo);
					StringReader reader = new StringReader(configInfo);
					try {
						PropertyConfigurer.props.load(reader);
					} catch (IOException e) {
						logger.error(e);
					}
				}
			};
			managerListeners.add(springMamagerListener);

			// 定义事件源
			try {
				if (!StringUtils.isNullOrEmpty(eventListenerClass)) {
					// 定义并向事件源中注册事件监听器
					Class<?> clazz = Class.forName(eventListenerClass);
					ManagerListener managerListener = (ManagerListener) clazz.newInstance();
					managerListeners.add(managerListener);
				}
			} catch (Exception e) {
				logger.error(e);
			}

			try {
				if (ObjectUtils.isNotEmpty(eventListener)) {
					managerListeners.add(eventListener);
				}
			} catch (Exception e) {
				logger.error(e);
			}

			DiamondManager manager = new DefaultDiamondManager(group, dataId, managerListeners);

			try {
				String configInfo = manager.getAvailableConfigureInfomation(30000);
				logger.warn("配置项内容: \n" + configInfo);
				if (!StringUtils.isEmpty(configInfo)) {
					StringReader reader = new StringReader(configInfo);
					props.load(reader);
					PropertyConfigurer.load(props);
				} else {
					logger.error("在配置管理中心找不到配置信息");
				}
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			PropertyConfigurer.load(props);
		}
		super.processProperties(beanFactory, props);
		this.props = props;

		// 讲-D开头的的配置设置到系统变量
		Iterator<Entry<Object, Object>> it = props.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (key.toString().startsWith("-D")) {
				String systemKey = key.toString().trim().substring(2);
				String systemValue = value.toString().trim();
				System.setProperty(systemKey, systemValue);
				logger.warn(key.toString() + "=" + systemValue);
			}
		}

	}

	public Object getProperty(String key) {
		return props.get(key);
	}

	private String getDeployEnv(Properties props) {
		String env = System.getProperty(Constants.KEY_DEPLOY_ENV);
		if (StringUtils.isEmpty(env)) {
			env = System.getProperty(Constants.KEY_ENV);
			if (StringUtils.isEmpty(env)) {
				env = EnvConfigurer.env;
				if (StringUtils.isEmpty(env)) {
					env = props.getProperty(Constants.KEY_DEPLOY_ENV);
				}
			}
		}
		return env;
	}

}