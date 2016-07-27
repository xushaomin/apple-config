package com.appleframework.config;

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

public class Version {

	private static Logger logger = Logger.getLogger(Version.class);

	public static void logVersion() {
		try {
			Enumeration<URL> resources = Version.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes attrs = manifest.getMainAttributes();
				if (attrs == null) {
					continue;
				}
				String name = attrs.getValue("Bundle-Name");
				if (name != null && name.indexOf("apple-config") > -1) {
					logger.info(name + " " + attrs.getValue("Bundle-Version"));
					break;
				}
			}
		} catch (Exception E) {
			// skip it
		}
	}

}