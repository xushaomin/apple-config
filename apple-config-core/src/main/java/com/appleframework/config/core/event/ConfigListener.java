package com.appleframework.config.core.event;

import java.util.EventListener;
import java.util.Properties;

public interface ConfigListener extends EventListener {
	
	public void receiveConfigInfo(Properties props);
	
}
