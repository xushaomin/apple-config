package com.appleframework.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ConfigNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("property-placeholder", new ConfigBeanDefinitionParser());
    }
}

