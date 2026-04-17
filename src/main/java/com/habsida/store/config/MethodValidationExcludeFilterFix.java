package com.habsida.store.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

/**
 * Workaround for a Spring Framework 7.x bug where ConstructorResolver selects
 * the two-argument overload of MethodValidationExcludeFilter.byAnnotation() even
 * though EnableConfigurationPropertiesRegistrar only registers one explicit
 * constructor argument. The resolver then fails to satisfy the second parameter
 * (MergedAnnotations.SearchStrategy) because it is an enum, not a Spring bean.
 *
 * This post-processor runs after EnableConfigurationPropertiesRegistrar (which is
 * an ImportBeanDefinitionRegistrar processed by the PriorityOrdered
 * ConfigurationClassPostProcessor) and injects the missing SearchStrategy value
 * that the one-argument byAnnotation() would have defaulted to anyway.
 */
@Component
class MethodValidationExcludeFilterFix implements BeanDefinitionRegistryPostProcessor, Ordered {

    private static final String BEAN_NAME =
            "org.springframework.boot.context.properties.EnableConfigurationPropertiesRegistrar.methodValidationExcludeFilter";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (registry.containsBeanDefinition(BEAN_NAME)) {
            registry.getBeanDefinition(BEAN_NAME)
                    .getConstructorArgumentValues()
                    .addIndexedArgumentValue(1, MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}