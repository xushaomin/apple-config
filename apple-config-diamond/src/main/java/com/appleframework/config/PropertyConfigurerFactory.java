package com.appleframework.config;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.appleframework.config.core.Constants;
import com.appleframework.config.core.EnvConfigurer;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.event.ConfigListener;
import com.appleframework.config.core.factory.BaseConfigurerFactory;
import com.appleframework.config.core.factory.ConfigurerFactory;
import com.appleframework.config.core.util.StringUtils;
import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;

public class PropertyConfigurerFactory extends BaseConfigurerFactory implements ConfigurerFactory {

	private static Logger logger = Logger.getLogger(PropertyConfigurerFactory.class);

	private String KEY_DEPLOY_GROUP     = "deploy.group";
	private String KEY_DEPLOY_DATAID    = "deploy.dataId";
	private String KEY_DEPLOY_CONF_HOST = "deploy.confHost";
	
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
		
		if (!isLoadRemote()) {
			return;
		}

		String group = PropertyConfigurer.getString(KEY_DEPLOY_GROUP);
		String dataId = PropertyConfigurer.getString(KEY_DEPLOY_DATAID);
		
		String confHost = PropertyConfigurer.getString(KEY_DEPLOY_CONF_HOST);
		if (null != confHost) {
			com.taobao.diamond.common.Constants.DEFAULT_DOMAINNAME = confHost;
			com.taobao.diamond.common.Constants.DAILY_DOMAINNAME   = confHost;
		}

		logger.warn("配置项：group=" + group);
		logger.warn("配置项：dataId=" + dataId);

		if (!StringUtils.isEmpty(group) && !StringUtils.isEmpty(dataId)) {
			String env = this.getDeployEnv();
			if (!StringUtils.isEmpty(env)) {
				dataId += "-" + env;
				logger.warn("配置项：env=" + env);
			}
			
			ManagerListener springMamagerListener = new ManagerListener() {

				public Executor getExecutor() {
					return null;
				}

				public void receiveConfigInfo(String configInfo) {
					// 客户端处理数据的逻辑
					logger.warn("已改动的配置：\n" + configInfo);
					StringReader reader = new StringReader(configInfo);
					try {
						PropertyConfigurer.load(reader);
					} catch (Exception e) {
						logger.error(e);
					}
					setSystemProperty(PropertyConfigurer.getProps());
					
					//事件触发
					if(eventListenerSet.size() > 0) {
						Iterator<ConfigListener> iterator = eventListenerSet.iterator();
				        while (iterator.hasNext()) {
				        	ConfigListener listener = iterator.next();
				            listener.receiveConfigInfo(PropertyConfigurer.getProps());
				        }
					}
				}
			};

			DiamondManager manager = new DefaultDiamondManager(group, dataId, springMamagerListener);
			try {
				String configInfo = manager.getAvailableConfigureInfomation(30000);
				logger.warn("配置项内容: \n" + configInfo);
				if (!StringUtils.isEmpty(configInfo)) {
					StringReader reader = new StringReader(configInfo);
					PropertyConfigurer.load(reader);
					setSystemProperty(PropertyConfigurer.getProps());
				} else {
					logger.error("在配置管理中心找不到配置信息");
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private String getDeployEnv() {
		String env = System.getProperty(Constants.KEY_DEPLOY_ENV);
		if (StringUtils.isEmpty(env)) {
			env = System.getProperty(Constants.KEY_ENV);
			if (StringUtils.isEmpty(env)) {
				env = EnvConfigurer.env;
				if (StringUtils.isEmpty(env)) {
					env = PropertyConfigurer.getString(Constants.KEY_DEPLOY_ENV);
				}
			}
		}
		return env;
	}


	
}