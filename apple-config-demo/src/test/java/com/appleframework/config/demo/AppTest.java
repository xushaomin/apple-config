package com.appleframework.config.demo;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:config/*.xml" })
public class AppTest {
	
	@Resource
	private AppService appService;

	@Test
	public void testAddOpinion1() {
		try {
			for (int i = 0; i < 10000; i++) {
				//System.out.println(appService.getName());
				
				System.out.println(System.getProperty("dtest"));
				Thread.sleep(1000);
			}
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
