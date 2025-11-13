package com.wly.center.starter.processor;

import com.wly.center.common.conf.ConfClient;
import com.wly.center.common.conf.ConfData;
import com.wly.center.core.enumeration.WatcherExchangeType;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.watcher.CycleWatcher;
import com.wly.center.core.watcher.NodeDeletedWatcher;
import com.wly.center.starter.annotation.DynamicConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.UUID;

@Slf4j
public record DynamicConfAnnotationProcessor(ConfClient confClient) implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            field.setAccessible(true);
            DynamicConf dynamicConf = field.getAnnotation(DynamicConf.class);

            Object raw = bean;
            if (dynamicConf != null) {
                if (AopUtils.isAopProxy(bean)) {
                    raw = AopUtils.getTargetClass(bean);
                }

                Object defaultValue = field.get(raw);
                field.set(raw, confClient.value(dynamicConf.key(), defaultValue, field.getType()));

                if (dynamicConf.refresh()) {
                    try {
                        confClient.getWatchClient().watch(dynamicConf.key(), new DynamicConfDataWatcher(
                                confClient.getWatchClient().restExchangeClient(), raw, field));

                        confClient.getWatchClient().watch(dynamicConf.key(), new DynamicConfNodeWatcher(
                                raw, field, defaultValue));
                    } catch (Exception e) {
                        log.warn("dynamic conf annotation data node register watcher error, node: {}, error: ",
                                dynamicConf.key(), e);
                    }
                }
            }
        });
        return bean;
    }

    public record DynamicConfDataWatcher(RestExchangeClient exchangeClient, Object bean, Field field,
                                         String key) implements CycleWatcher {

        public DynamicConfDataWatcher(RestExchangeClient exchangeClient, Object bean, Field field) {
            this(exchangeClient, bean, field, UUID.randomUUID().toString().replace("-", ""));
        }

        @Override
        public void notify(String node, Integer exchangeType) {
            if (WatcherExchangeType.DATA_CHANGE.getCode().equals(exchangeType)) {
                log.debug("dynamic conf annotation data node changed, node: {}", node);
                try {
                    String data = exchangeClient.getNodeDetail(node).getData();
                    field.setAccessible(true);
                    if (!StringUtils.hasText(data)) {
                        field.set(bean, null);
                    } else {
                        ConfData confData = new ConfData(field.getType(), data);
                        field.set(bean, confData.getData());
                    }
                } catch (Exception e) {
                    log.error("dynamic conf annotation data node changed error, node: {}, error: ", node, e);
                }
            }
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public WatcherExchangeType exchangeType() {
            return WatcherExchangeType.DATA_CHANGE;
        }
    }

    public record DynamicConfNodeWatcher(Object bean, Field field,
                                         Object defaultValue, String key) implements NodeDeletedWatcher {

        public DynamicConfNodeWatcher(Object bean, Field field, Object defaultValue) {
            this(bean, field, defaultValue, UUID.randomUUID().toString().replace("-", ""));
        }

        @Override
        public void nodeDeleted(String nodeName) {
            log.debug("dynamic conf annotation data node deleted, node: {}", nodeName);

            try {
                field.setAccessible(true);
                field.set(bean, defaultValue);
            } catch (Exception e) {
                log.error("dynamic conf annotation data node deleted error, node: {}, error: ", nodeName, e);
            }
        }


        @Override
        public String key() {
            return key;
        }
    }
}
