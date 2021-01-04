package com.appleframework.config.core.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class SystemPropertiesUtil {
	
	public static void set(Properties props) {
		Iterator<Entry<Object, Object>> it = props.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			judgeSet(key, value);
		}
	}
	
	public static void set(Map<String, Properties> propsMap) {
		for(Properties props : propsMap.values()){
		    set(props);
		}
	}
		
	public static void set(Object key, Object value) {
		String systemKey = key.toString().trim();
		String systemValue = value.toString().trim();
		System.setProperty(systemKey, systemValue);
	}
	
	public static void judgeSet(Object key, Object value) {
		if (key.toString().startsWith("-D")) {
			String systemKey = key.toString().trim().substring(2);
			String systemValue = value.toString().trim();
			System.setProperty(systemKey, systemValue);
			//logger.warn(key.toString() + "=" + systemValue);
			key = systemKey;
		}
	}

}
