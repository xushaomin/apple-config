package com.appleframework.config.core.event;

import java.util.EventListener;
import java.util.Map;
import java.util.Properties;

public interface ConfigListener extends EventListener {
	
	public Properties receiveConfigInfo(Map<String, Properties> propsMap);
		
}
