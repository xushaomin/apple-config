package com.appleframework.config;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.event.ConfigListener;
import com.appleframework.config.core.util.StringUtils;
import com.appleframework.config.core.Constants;

public class AppleConfigEventProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

	private static Logger logger = LoggerFactory.getLogger(AppleLocalResourceProcessor.class);

	private static Properties properties = new Properties();

	private ConfigurableEnvironment environment;
	
	private String resources = "config-event-properties";
	
	private CompositePropertySource composite = new CompositePropertySource(resources);

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		configEvent();
		configAppId();
		configEnv();
		addCompositePropertySource();
	}
	
	private void addCompositePropertySource(){
        // add after the bootstrap property source or to the first
        if (environment.getPropertySources().contains(resources)) {
            // ensure ApolloBootstrapPropertySources is still the first
            ensureBootstrapPropertyPrecedence(environment);
            environment.getPropertySources().addAfter(resources, composite);
        } else {
            environment.getPropertySources().addFirst(composite);
        }
    }

	private void ensureBootstrapPropertyPrecedence(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> bootstrapPropertySource = propertySources.get(resources);
        // not exists or already in the first place
        if (bootstrapPropertySource == null || propertySources.precedenceOf(bootstrapPropertySource) == 0) {
            return;
        }
        propertySources.remove(resources);
        propertySources.addFirst(bootstrapPropertySource);
    }

//	private void initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory beanFactory) {
//		AutoUpdateConfigChangeListener autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(environment, beanFactory);
//		List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
//		for (ConfigPropertySource configPropertySource : configPropertySources) {
//			configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
//		}
//	}
	
	private void configAppId() {
		String appId = System.getProperty(Constants.KEY_APP_ID); //系统读取 app.id
		if(StringUtils.isNullOrEmpty(appId)) {
			appId = PropertyConfigurer.getString(Constants.KEY_APP_ID); //配置文件读取 app.id
		}
//		if(!StringUtils.isNullOrEmpty(appId)) {
//			System.setProperty(Constants.KEY_APP_ID, appId);
//		}
	}
	
	private void configEnv() {
		String env = System.getProperty(Constants.KEY_APP_ENV); //系统读取 app.env
		if(StringUtils.isNullOrEmpty(env)) {
			env = System.getProperty(Constants.KEY_ENV); //系统读取 env
			if(StringUtils.isNullOrEmpty(env)) {
				env = PropertyConfigurer.getString(Constants.KEY_APP_ENV); //配置文件读取 app.env
				if(StringUtils.isNullOrEmpty(env)) {
					env = PropertyConfigurer.getString(Constants.KEY_ENV); //配置文件读取 env
				} else {
					env = "dev";
				}
			}
		}
		
		if(!StringUtils.isNullOrEmpty(env)) {
			System.setProperty(Constants.KEY_APP_ENV, env);
			System.setProperty(Constants.KEY_ENV, env);
		}
	}

	
	private void configEvent() {
		Map<String, Properties> propsMap = PropertyConfigurer.getPropsMap();
		// load by spi
		try {
			ConfigListener spiConfigListener = null;
			ServiceLoader<ConfigListener> serviceLoader = ServiceLoader.load(ConfigListener.class);
			Iterator<ConfigListener> iterator = serviceLoader.iterator();
			while (iterator.hasNext()) {
				spiConfigListener = iterator.next();
				Properties props = spiConfigListener.receiveConfigInfo(propsMap);
				if(null != props && !props.isEmpty()) {
					properties.putAll(props);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		if(!properties.isEmpty()) {
			PropertiesPropertySource pps = new PropertiesPropertySource(resources, properties);
			composite.addPropertySource(pps);
		}

	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = (ConfigurableEnvironment) environment;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
