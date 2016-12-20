package com.appleframework.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.appleframework.config.core.annotation.AppleConfig;

@Service("demoAnn")
public class Demo {

	@AppleConfig("demo.config1")
	private String config1;

	@AppleConfig
	private Integer config2;

	private String config3;
	private Integer config4;

	public void setConfig3(String config3) {
		this.config3 = config3;
	}

	public void setConfig4(Integer config4) {
		this.config4 = config4;
	}

	public void printConfigAnn() {
		System.out.println("{ config1 = " + config1 + ", config2 = " + config2 + "}");
	}

	public void printConfigXML() {
		System.out.println("{ config3 = " + config3 + ", config4 = " + config4 + "}");
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext appCtx = new ClassPathXmlApplicationContext("config/applicationContext.xml");

		Demo demoAnn = (Demo) appCtx.getBean("demoAnn");
		demoAnn.printConfigAnn();

		Demo demoXML = (Demo) appCtx.getBean("demoXML");
		demoXML.printConfigXML();
	}
}
