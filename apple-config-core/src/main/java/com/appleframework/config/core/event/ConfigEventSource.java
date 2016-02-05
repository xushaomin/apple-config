package com.appleframework.config.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** 
 * 定义事件源 
 * @author cruise.xu 
 * 
 */  
public class ConfigEventSource {
	
    private List<ConfigEventListener> listeners = new ArrayList<ConfigEventListener>();  
  
    public ConfigEventSource() {
    }  
  
    public void addConfigListener(ConfigEventListener eventListener) {
        listeners.add(eventListener);  
    }  
  
    public void notifyConfigEvent(Properties props) {
        for (ConfigEventListener eventListener : listeners) {  
            ConfigEvent configEvent = new ConfigEvent(props);  
            eventListener.processEvent(configEvent);  
        }  
    }
} 