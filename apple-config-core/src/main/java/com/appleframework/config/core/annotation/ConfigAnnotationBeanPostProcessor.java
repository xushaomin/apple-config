package com.appleframework.config.core.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeansException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.appleframework.config.core.PropertyConfigurer;

@Component
public class ConfigAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

	private SimpleTypeConverter typeConverter = new SimpleTypeConverter();

	@Override
	public boolean postProcessAfterInstantiation(final Object bean, String beanName) throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				AppleConfig cfg = field.getAnnotation(AppleConfig.class);
				if (cfg != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@Config annotation is not supported on static fields");
					}

					String key = cfg.value().length() <= 0 ? field.getName() : cfg.value();
					Object value = PropertyConfigurer.getProperty(key);

					if (value != null) {
						Object object = typeConverter.convertIfNecessary(value, field.getType());
						ReflectionUtils.makeAccessible(field);
						field.set(bean, object);
					}
				}
			}
		});

		return true;
	}
}
