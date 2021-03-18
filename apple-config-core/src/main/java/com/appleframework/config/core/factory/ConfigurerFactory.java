package com.appleframework.config.core.factory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.Resource;

import com.appleframework.config.core.event.ConfigListener;

public interface ConfigurerFactory {

	public boolean isLoadRemote();
	
	public void setRemoteFirst(boolean remoteFirst);

	public void setLoadRemote(boolean loadRemote);
	
	public void setEventListenerClass(String eventListenerClass);

	public void setEventListener(ConfigListener eventListener);
	
	public void setEventListeners(Collection<ConfigListener> eventListeners);

	public void setEventListenerClasss(Collection<String> eventListenerClasss);
		
	public void init();
	
    public void notifyPropertiesChanged(Map<String, Properties> oldProperties);
    
    public void notifyPropertiesChanged(Properties props);
    	
	public void close();
	
	public Properties getRemoteProperties(String namespace);
	
	public String getRemoteConfigInfo(String namespace);
	
	public Map<String, Properties> getAllRemoteProperties();

	public Properties onLoadFinish(Map<String, Properties> properties);
	
	public Properties onLoadFinish(Properties props);
	
	public boolean isRemoteFirst();
		
	public void setRemotes(Resource... locations);
	
	public void setNamespaces(String namespaces);
	
}