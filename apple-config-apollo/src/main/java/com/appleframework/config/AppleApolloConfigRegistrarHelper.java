package com.appleframework.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.appleframework.config.core.spi.NamespaceLoadSpi;
import com.ctrip.framework.apollo.core.spi.Ordered;
import com.ctrip.framework.apollo.spring.annotation.ApolloAnnotationProcessor;
import com.ctrip.framework.apollo.spring.annotation.ApolloJsonValueProcessor;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.SpringValueProcessor;
import com.ctrip.framework.apollo.spring.property.SpringValueDefinitionProcessor;
import com.ctrip.framework.apollo.spring.spi.ApolloConfigRegistrarHelper;
import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.google.common.collect.Lists;

public class AppleApolloConfigRegistrarHelper implements ApolloConfigRegistrarHelper {

	  @Override
	  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
	    AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableApolloConfig.class.getName()));
	    String[] namespaces = attributes.getStringArray("value");
	    int order = attributes.getNumber("order");
	    ApplePropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), order++);
	    
	    ServiceLoader<NamespaceLoadSpi> serviceLoader = ServiceLoader.load(NamespaceLoadSpi.class);
		Iterator<NamespaceLoadSpi> iterator = serviceLoader.iterator();
		while (iterator.hasNext()) {
			NamespaceLoadSpi namespaceLoadSpi = iterator.next();
			Set<String> namespaceSet = namespaceLoadSpi.load();
			ApplePropertySourcesProcessor.addNamespaces(namespaceSet, order++);
		}

	    Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
	    // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
	    propertySourcesPlaceholderPropertyValues.put("order", 0);

	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(), PropertySourcesPlaceholderConfigurer.class, propertySourcesPlaceholderPropertyValues);
	    
	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, AppleLocalResourceProcessor.class.getName(), AppleLocalResourceProcessor.class);
	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApplePropertySourcesProcessor.class.getName(), ApplePropertySourcesProcessor.class);
	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, AppleConfigEventProcessor.class.getName(), AppleConfigEventProcessor.class);

	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(), ApolloAnnotationProcessor.class);
	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueDefinitionProcessor.class.getName(), SpringValueDefinitionProcessor.class);
	    BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloJsonValueProcessor.class.getName(), ApolloJsonValueProcessor.class);
	  }

	  @Override
	  public int getOrder() {
	    return Ordered.LOWEST_PRECEDENCE - 1;
	  }
	}
