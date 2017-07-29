package com.appleframework.config;

import java.util.Collection;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.appleframework.config.core.event.ConfigListener;
import com.appleframework.config.core.factory.ConfigurerFactory;

public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {	
	
    private Collection<ConfigListener> eventListeners;
    
    private Collection<String> eventListenerClasss;

	private String eventListenerClass;

	private ConfigListener eventListener;

	private boolean loadRemote = true;

	public boolean isLoadRemote() {
		return loadRemote;
	}

	public void setLoadRemote(boolean loadRemote) {
		this.loadRemote = loadRemote;
	}

	public void setEventListenerClass(String eventListenerClass) {
		this.eventListenerClass = eventListenerClass;
	}

	public void setEventListener(ConfigListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
		ConfigurerFactory factory = new PropertyConfigurerFactory(props);
		factory.setLoadRemote(loadRemote);
		factory.setEventListener(eventListener);
		factory.setEventListenerClass(eventListenerClass);
		factory.setEventListenerClasss(eventListenerClasss);
		factory.setEventListeners(eventListeners);
		factory.init();
		super.processProperties(beanFactory, factory.getProps());
	}


	public void setEventListeners(Collection<ConfigListener> eventListeners) {
		this.eventListeners = eventListeners;
	}

	public void setEventListenerClasss(Collection<String> eventListenerClasss) {
		this.eventListenerClasss = eventListenerClasss;
	}

}