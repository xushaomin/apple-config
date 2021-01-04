package com.appleframework.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition; 
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.appleframework.config.core.annotation.ConfigAnnotationBeanPostProcessor;
import com.appleframework.config.core.util.StringUtils;  
  
public class ConfigBeanDefinitionParser implements BeanDefinitionParser {
  
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String id = element.getAttribute("id");
        String location = element.getAttribute("location");
        String loadRemote = element.getAttribute("load-remote");
        String eventListenerClass = element.getAttribute("event-listener-class");
  
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(ExtendedPropertyPlaceholderConfigurer.class);
        
        if(!StringUtils.isNullOrEmpty(loadRemote)) {
        	beanDefinition.getPropertyValues().addPropertyValue("loadRemote", loadRemote);
        }
        List<String> list = new ArrayList<>();
        if(!StringUtils.isNullOrEmpty(location)) {
        	list.add(location);
        }
        else {
        	list.add("system.properties");
        }
        beanDefinition.getPropertyValues().addPropertyValue("locations", list);
        
        if(!StringUtils.isNullOrEmpty(eventListenerClass)) {
        	beanDefinition.getPropertyValues().addPropertyValue("eventListenerClass", eventListenerClass);
        }
        if(StringUtils.isNullOrEmpty(id)) {
        	id = "extendedPropertyPlaceholderConfigurer";
        }
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        
        RootBeanDefinition annDefinition = new RootBeanDefinition();
        annDefinition.setBeanClass(ConfigAnnotationBeanPostProcessor.class);
        parserContext.getRegistry().registerBeanDefinition("configAnnotationBeanPostProcessor", annDefinition);
  
        return beanDefinition;
    }  
}  