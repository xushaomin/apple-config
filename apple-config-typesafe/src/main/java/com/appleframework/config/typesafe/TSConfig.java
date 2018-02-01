package com.appleframework.config.typesafe;

import java.io.StringReader;

import com.appleframework.config.core.PropertyConfigurer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TSConfig {
	
    static Config cfg = load();

    static Config load() {
		String configInfo = PropertyConfigurer.getConfigInfo();
		if(null == configInfo) {
			return ConfigFactory.parseReader(new StringReader(configInfo));
		}
		else {
			return null;
		}
    }

	public static Config getCfg() {
		return cfg;
	}

}