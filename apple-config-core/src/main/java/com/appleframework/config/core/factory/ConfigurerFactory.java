package com.appleframework.config.core.factory;

import java.util.Collection;
import java.util.Properties;

import com.appleframework.config.core.event.ConfigListener;

public interface ConfigurerFactory {

	public boolean isLoadRemote();

	public void setLoadRemote(boolean loadRemote);

	public void setEventListenerClass(String eventListenerClass);

	public void setEventListener(ConfigListener eventListener);
	
	public void setEventListeners(Collection<ConfigListener> eventListeners);

	public void setEventListenerClasss(Collection<String> eventListenerClasss);
	
	public void setSystemPropertyFile(String systemPropertyFile);
	
	public void init();

	public Properties getProps();
	
	public void setProperties(Properties properties);
	
	/**
     * 通过listener去通知 reload
     *
     * @param oldProperties
     */
    public void notifyPropertiesChanged(Properties oldProperties);
    
    public void loadFileProperties(String fileName);
	
	public void loadSystemProperties();
	
}