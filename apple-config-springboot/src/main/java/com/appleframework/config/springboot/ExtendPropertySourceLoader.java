package com.appleframework.config.springboot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ResourceUtils;

public class ExtendPropertySourceLoader implements EnvironmentPostProcessor {
	
	private static Logger log = LoggerFactory.getLogger(ExtendPropertySourceLoader.class);

	private final ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
	
	private static ConfigurableEnvironment env;

	private final List<PropertySourceLoader> propertySourceLoaders;
	
	private final static Set<String> resourceSet = new HashSet<>();
	    
    public ExtendPropertySourceLoader() {
    	super();
    	this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    }
        
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		env = environment;
		for (PropertySourceLoader loader : this.propertySourceLoaders) {
			for (String fileExtension : loader.getFileExtensions()) {
				if(fileExtension.equalsIgnoreCase("xml")) {
					continue;
				}
				String location = ResourceUtils.CLASSPATH_URL_PREFIX + "application." + fileExtension;
				try {
					if(resourceSet.contains(location)) {
						continue;
					}
					Resource[] resources = this.resourceLoader.getResources(location);
					for (Resource resource : resources) {
						List<PropertySource<?>> propertySources = ExtendPropertyLocalLoader.load(resource.getFilename(), resource);
						if(null != propertySources && !propertySources.isEmpty()) {
							propertySources.stream().forEach(environment.getPropertySources()::addLast);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage());
				} finally {
					resourceSet.add(location);
				}
			}
		}
		
		
		try {
			List<PropertySource<?>> propertySources = ExtendPropertyCenterLoader.load();
			if(null != propertySources && !propertySources.isEmpty()) {
				propertySources.stream().forEach(environment.getPropertySources()::addLast);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
	}

	public static ConfigurableEnvironment getEnv() {
    	return env;
	}
	
	
}