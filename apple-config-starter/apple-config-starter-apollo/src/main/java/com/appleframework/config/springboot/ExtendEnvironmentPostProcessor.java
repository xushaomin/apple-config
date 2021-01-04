package com.appleframework.config.springboot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ResourceUtils;

import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.StringUtils;

@Configuration
public class ExtendEnvironmentPostProcessor implements EnvironmentPostProcessor {
	
	private static Logger log = LoggerFactory.getLogger(ExtendEnvironmentPostProcessor.class);

	private final ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();

	private static ConfigurableEnvironment env;

	private final List<PropertySourceLoader> propertySourceLoaders;
	
	private final static Set<String> resourceSet = new HashSet<>();
	
	private static boolean isLoadApollo = false;
	
	private final String KEY_BOOTSTRAP_ENABLED = "apollo.bootstrap.enabled";
    
    public ExtendEnvironmentPostProcessor() {
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
						List<PropertySource<?>> propertySources = ExtendPropertySourceLoader.load(resource.getFilename(), resource);
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
		
		if(!isLoadApollo) {
			String bootstrapEnabled = System.getProperty(KEY_BOOTSTRAP_ENABLED);
			if(StringUtils.isNullOrEmpty(bootstrapEnabled)) {
				bootstrapEnabled = PropertyConfigurer.getString(KEY_BOOTSTRAP_ENABLED);
			}
					
			if(StringUtils.isNullOrEmpty(bootstrapEnabled) || bootstrapEnabled.equalsIgnoreCase("true")) {
				List<PropertySource<?>> propertySources = ExtendPropertyApolloLoader.load(PropertyConfigurer.getProps());
				if(null != propertySources && !propertySources.isEmpty()) {
					propertySources.stream().forEach(environment.getPropertySources()::addLast);
				}
			}
						
			isLoadApollo = true;
		}
				
		configHealthCheck();
	}

	public static ConfigurableEnvironment getEnv() {
    	return env;
	}
	
	private void configHealthCheck() {
		System.setProperty("management.health.rabbit.enabled", "false");
		System.setProperty("management.health.redis.enabled", "false");
		System.setProperty("management.health.mongo.enabled", "false");
	}
}
