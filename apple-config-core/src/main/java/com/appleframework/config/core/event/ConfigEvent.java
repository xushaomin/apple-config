package com.appleframework.config.core.event;

import java.util.EventObject;
import java.util.Properties;

/**
 * 事件对象的定义
 * 
 * @author administrator
 * 
 */
public class ConfigEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;

	public ConfigEvent(Properties props) {
		super(props);
	}
}