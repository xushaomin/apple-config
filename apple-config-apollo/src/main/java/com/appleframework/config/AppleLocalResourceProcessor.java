package com.appleframework.config;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.StringUtils;

public class AppleLocalResourceProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

	private static Logger logger = LoggerFactory.getLogger(AppleLocalResourceProcessor.class);

	private static String KEY_DEPLOY_NAMESPACES = "apollo.bootstrap.namespaces";

	private ConfigurableEnvironment environment;

	private int order = 100;
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (PropertySource<?> ps : environment.getPropertySources()) {
			resloveLocalProperties(ps);
		}
		
		Set<String> namespaceSet = new HashSet<String>();
		String namespaces = this.getDeployNamespaces();
		if (!StringUtils.isNullOrEmpty(namespaces)) {
			String[] namespaceArr = namespaces.trim().split(",");
			for (String namespace : namespaceArr) {
				namespaceSet.add(namespace);
			}
		}
		
		if(namespaceSet.size() > 0) {
			ApplePropertySourcesProcessor.addNamespaces(namespaceSet, order++);
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

	private void resloveLocalProperties(PropertySource<?> propertySource) {
		String name = propertySource.getName();
		Set<String> namespaceSet = new HashSet<String>();
		if (name.indexOf("applicationConfig") > -1) {
			logger.info("The local [ {} ] properties: ", name);
			EnumerablePropertySource<?> mps = (EnumerablePropertySource<?>) propertySource;
			for (String key : mps.getPropertyNames()) {
				Object value = mps.getProperty(key);
				PropertyConfigurer.put(key, value);
				logger.info("    {}={}", key, value);
			}
		}		
		
		if(namespaceSet.size() > 0) {
			ApplePropertySourcesProcessor.addNamespaces(namespaceSet, order++);
		}
		
	}

	private String getDeployNamespaces() {
		String namespaces = System.getProperty(KEY_DEPLOY_NAMESPACES);
		if(null == namespaces) {
			namespaces = PropertyConfigurer.getString(KEY_DEPLOY_NAMESPACES);
		}
		return namespaces;
	}
}
