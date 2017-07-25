package com.appleframework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liaoqiqi
 * @version 2014-6-17
 */
public class DisconfDemoMain2 {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfDemoMain2.class);

    private static String[] fn = null;

    // 初始化spring文档
    private static void contextInitialized() {
        fn = new String[] {"applicationContext2.xml"};
    }

    /**
     * @param args
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        contextInitialized();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);

        DisconfDemoTask2 task = ctx.getBean("disconfDemoTask2", DisconfDemoTask2.class);

        int ret = task.run();

        System.exit(ret);
        
        ctx.close();
    }
}
