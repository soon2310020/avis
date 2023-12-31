package vn.com.twendie.avis.api.core;

import javax.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private volatile static ApplicationContext applicationContext;

    static void setAppInstance(ApplicationContext applicationContext) {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    @Override
    public synchronized void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    @PreDestroy
    public synchronized void clearApplicationContext() {
        ApplicationContextProvider.applicationContext = null;
    }

    public synchronized static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}

