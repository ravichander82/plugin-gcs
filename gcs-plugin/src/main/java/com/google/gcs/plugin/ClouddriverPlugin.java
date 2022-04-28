package com.google.gcs.plugin;

import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClouddriverPlugin extends PrivilegedSpringPlugin {

    public ClouddriverPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        BeanDefinition googleCredentialsRepository = primaryBeanDefinitionFor(GoogleCredentialsRepository.class);
        try {
            log.debug("Registering bean: {}", googleCredentialsRepository.getBeanClassName());
            registry.registerBeanDefinition("googleCredentialsRepository", googleCredentialsRepository);
        } catch (BeanDefinitionStoreException e) {
            log.error("Could not register bean {}", googleCredentialsRepository.getBeanClassName());

        }
        List<Class> classes = new ArrayList<>(Arrays.asList(
                GoogleCredentialsDefinitionSource.class,
                GcsSource.class,
                GoogleUtils.class,
                GCSConfig.class));
        for (Class classToAdd : classes) {
            BeanDefinition beanDefinition = beanDefinitionFor(classToAdd);
            try {
                log.debug("Registering bean: {}", beanDefinition.getBeanClassName());
                registerBean(beanDefinition, registry);
            } catch (ClassNotFoundException e) {
                log.error("Could not register bean {}", beanDefinition.getBeanClassName());
            }
        }

    }

    @Override
    public void start() {
        log.info("{} plugin started ******************************************", this.getClass().getSimpleName());
    }

    @Override
    public void stop() {
        log.info("{} plugin stopped******************************************", this.getClass().getSimpleName());
    }

}
