package com.appleframework.config.core.event;

import java.util.EventListener;

/** 
 * ConfigEvent事件监听器接口 
 * @author administrator 
 * 
 */  
public interface ConfigEventListener extends EventListener {
  
    public void processEvent(ConfigEvent demoEvent);  
  
}