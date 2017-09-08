package com.appleframework.config.springboot;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.appleframework.config.PropertyConfigurerFactory;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.factory.ConfigurerFactory;

public class ExtendPropertySourceLoader implements PropertySourceLoader, PriorityOrdered, DisposableBean {

	private ConfigurerFactory configurerFactory = PropertyConfigurerFactory.getInstance();

	@Override
	public String[] getFileExtensions() {
		return new String[] { "properties" };
	}

	@Override
	public PropertySource<?> load(String name, Resource resource, String profile) throws IOException {
		if (profile == null) {
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			PropertyConfigurer.merge(properties);

			configurerFactory.setSpringboot(true);
			configurerFactory.init();

			Properties remoteProperties = configurerFactory.getAllRemoteProperties();
			if (remoteProperties != null) {
				Set<Entry<Object, Object>> entrySet = remoteProperties.entrySet();
				for (Entry<Object, Object> entry : entrySet) {
					// 本地配置优先
					if (configurerFactory.isRemoteFirst() == false && properties.containsKey(entry.getKey()))
						continue;
					properties.put(entry.getKey(), entry.getValue());
					//
					PropertyConfigurer.add(entry.getKey().toString(), entry.getValue().toString());
				}
			}

			configurerFactory.onLoadFinish(properties);

			if (!properties.isEmpty()) {
				return new PropertiesPropertySource(name, properties);
			}
		}
		return null;
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	@Override
	public void destroy() throws Exception {
		configurerFactory.close();
	}

}
