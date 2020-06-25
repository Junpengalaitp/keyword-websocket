package com.alaitp.keyword.websocket;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        applicationContext = appContext;
    }

    private static ApplicationContext getContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getContext().getBean(clazz);
    }
}
