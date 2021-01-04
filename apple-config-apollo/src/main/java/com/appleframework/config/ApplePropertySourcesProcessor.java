package com.appleframework.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.event.ConfigListener;
import com.appleframework.config.core.util.SystemPropertiesUtil;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

public class ApplePropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

	private static final Log logger = LogFactory.getLog(ApplePropertySourcesProcessor.class);

	private static final Multimap<Integer, String> NAMESPACE_NAMES = LinkedHashMultimap.create();
	private static final Multiset<String> NAMESPACE_SETS = HashMultiset.create();

	private static final Set<BeanFactory> AUTO_UPDATE_INITIALIZED_BEAN_FACTORIES = Sets.newConcurrentHashSet();

	private final ConfigPropertySourceFactory configPropertySourceFactory 
			= SpringInjector.getInstance(ConfigPropertySourceFactory.class);
	
	private final ConfigUtil configUtil = ApolloInjector.getInstance(ConfigUtil.class);
	private ConfigurableEnvironment environment;

	public static void addNamespaces(Collection<String> namespaces, int order) {
		for (String namespace : namespaces) {
			addNamespace(namespace, order);
		}
	}

	public static void addNamespace(String namespace, int order) {
		if (!NAMESPACE_SETS.contains(namespace)) {
			NAMESPACE_NAMES.put(order, namespace);
			NAMESPACE_SETS.add(namespace);
		} else {
			logger.warn("the namespace " + namespace + " is exist!");
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		initializePropertySources();
		initializeAutoUpdatePropertiesFeature(beanFactory);
	}

	private void initializePropertySources() {
		if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME)) {
			// already initialized
			//return;
			logger.warn("the propertySources " + PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME + " is exist!");
		}
		CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME);

		// sort by order asc
		ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
		Iterator<Integer> iterator = orders.iterator();

		while (iterator.hasNext()) {
			int order = iterator.next();
			for (String namespace : NAMESPACE_NAMES.get(order)) {
				Config config = ConfigService.getConfig(namespace);
				boolean hasBigValue = false;
				Properties newProperties = new Properties();
				Set<String> propertyNames = config.getPropertyNames();
				if (propertyNames.size() > 0) {
					logger.info("The namespace [" + namespace + "] properties: ");
				} else {
					logger.info("The namespace [" + namespace + "] properties is null !!!");
				}

				for (String key : propertyNames) {
					String value = config.getProperty(key, null);
					if (null != value) {
						newProperties.put(key, value);
						PropertyConfigurer.setPropertyAll(namespace, key, value);
						logger.info("    " + key + "=" + value);
					}
				}
				addListener(namespace);
				if (hasBigValue) {
					if (config instanceof DefaultConfig) {
						DefaultConfig defaultConfig = (DefaultConfig) config;
						defaultConfig.onRepositoryChange(namespace, newProperties);
						ConfigPropertySource ps = configPropertySourceFactory.getConfigPropertySource(namespace, defaultConfig);
						composite.addPropertySource(ps);
					} else {
						ConfigPropertySource ps = configPropertySourceFactory.getConfigPropertySource(namespace, config);
						composite.addPropertySource(ps);
					}
				} else {
					ConfigPropertySource ps = configPropertySourceFactory.getConfigPropertySource(namespace, config);
					composite.addPropertySource(ps);
				}
			}
		}

		// clean up
		NAMESPACE_NAMES.clear();

		// add after the bootstrap property source or to the first
		if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
			// ensure ApolloBootstrapPropertySources is still the first
			ensureBootstrapPropertyPrecedence(environment);
			environment.getPropertySources().addAfter(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, composite);
		} else {
			environment.getPropertySources().addFirst(composite);
		}
	}

	private void ensureBootstrapPropertyPrecedence(ConfigurableEnvironment environment) {
		MutablePropertySources propertySources = environment.getPropertySources();

		PropertySource<?> bootstrapPropertySource = propertySources
				.get(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);

		// not exists or already in the first place
		if (bootstrapPropertySource == null || propertySources.precedenceOf(bootstrapPropertySource) == 0) {
			return;
		}

		propertySources.remove(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
		propertySources.addFirst(bootstrapPropertySource);
	}

	private void initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory beanFactory) {
		if (!configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()
				|| !AUTO_UPDATE_INITIALIZED_BEAN_FACTORIES.add(beanFactory)) {
			return;
		}

		AutoUpdateConfigChangeListener autoUpdateConfigChangeListener 
			= new AutoUpdateConfigChangeListener(environment,
				beanFactory);

		List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
		for (ConfigPropertySource configPropertySource : configPropertySources) {
			configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		// it is safe enough to cast as all known environment is derived from
		// ConfigurableEnvironment
		this.environment = (ConfigurableEnvironment) environment;
	}

	@Override
	public int getOrder() {
		// make it as early as possible
		return Ordered.HIGHEST_PRECEDENCE;
	}

	// for test only
	static void reset() {
		NAMESPACE_NAMES.clear();
		AUTO_UPDATE_INITIALIZED_BEAN_FACTORIES.clear();
	}

	private void addListener(String namespace) {
		ConfigService.getConfig(namespace).addChangeListener(new InnerConfigChangeListener(namespace));
	}

	private void change(ConfigChangeEvent changeEvent) {
		try {
			Map<String, Properties> propsMap = new HashMap<String, Properties>();
			String deletedKey = Constants.KEY_DELETEED;

			for (String key : changeEvent.changedKeys()) {
				ConfigChange change = changeEvent.getChange(key);
				String namespace = change.getNamespace().trim();

				if (PropertyChangeType.DELETED.equals(change.getChangeType())) {
					PropertyConfigurer.removePropertyAll(namespace, key);

					if (null == propsMap.get(deletedKey)) {
						Properties props = new Properties();
						props.put(namespace, key);
						propsMap.put(deletedKey, props);
					} else {
						Properties props = propsMap.get(deletedKey);
						props.put(namespace, key);
						propsMap.put(deletedKey, props);
					}

				} else {

					// 新增或修改，可直接put
					// 大value 协议转化
					String value = change.getNewValue();
					String bigValue = null;
					if (null != value) {
						PropertyConfigurer.setPropertyAll(namespace, key, value);
						logger.info("    " + key + "=" + value);

						if (null == propsMap.get(namespace)) {
							Properties props = new Properties();
							props.put(key, (null != bigValue) ? bigValue : value);
							propsMap.put(namespace, props);
						} else {
							Properties props = propsMap.get(namespace);
							props.put(key, (null != bigValue) ? bigValue : value);
							propsMap.put(namespace, props);
						}
					}
				}

			}
			notifyPropertiesChanged(propsMap);
		} catch (Exception e) {
			logger.error("notify error", e);
		}
	}

	public void notifyPropertiesChanged(Map<String, Properties> propsMap) {
		if (propsMap.isEmpty()) {
			return;
		}
		SystemPropertiesUtil.set(propsMap);
		// load by spi
		try {
			ConfigListener spiConfigListener = null;
			ServiceLoader<ConfigListener> serviceLoader = ServiceLoader.load(ConfigListener.class);
			Iterator<ConfigListener> iterator = serviceLoader.iterator();
			while (iterator.hasNext()) {
				spiConfigListener = iterator.next();
				spiConfigListener.receiveConfigInfo(propsMap);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	class InnerConfigChangeListener implements ConfigChangeListener {

		private String namespace;

		public InnerConfigChangeListener(String namespace) {
			this.namespace = namespace;
		}

		@Override
		public void onChange(ConfigChangeEvent changeEvent) {
			change(changeEvent);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof InnerConfigChangeListener)) {
				return false;
			}
			InnerConfigChangeListener that = (InnerConfigChangeListener) o;
			return Objects.equals(namespace, that.namespace);
		}

		@Override
		public int hashCode() {
			return Objects.hash(namespace);
		}
	}
}
