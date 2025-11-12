package com.wly.center.starter.confdata;

import com.wly.center.common.conf.ConfClient;
import com.wly.center.common.conf.ConfData;
import com.wly.center.core.enumeration.WatcherCycleEnum;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.watcher.Watcher;
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
                        confClient.getWatchClient().watch(dynamicConf.key(), new DynamicConfWatcher(
                                confClient.getWatchClient().restExchangeClient(), raw, field, defaultValue));
                    } catch (Exception e) {
                        log.warn("dynamic conf annotation data node register watcher error, node: {}, error: ", dynamicConf.key(), e);
                    }
                }
            }
        });
        return bean;
    }

    public record DynamicConfWatcher(RestExchangeClient exchangeClient, Object bean, Field field,
                                     Object defaultValue, String key) implements Watcher {

        public DynamicConfWatcher(RestExchangeClient exchangeClient, Object bean, Field field, Object defaultValue) {
            this(exchangeClient, bean, field, defaultValue, UUID.randomUUID().toString().replace("-", ""));
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
        public void nodeDataChanged(String nodeName) {
            log.debug("dynamic conf annotation data node changed, node: {}", nodeName);

            try {
                OpenNodeDetailResp node = exchangeClient.getNodeDetail(nodeName);
                String data = node.getData();
                field.setAccessible(true);
                if (!StringUtils.hasText(data)) {
                    field.set(bean, null);
                } else {
                    ConfData confData = new ConfData(field.getType(), data);
                    field.set(bean, confData.getData());
                }
            } catch (Exception e) {
                log.error("dynamic conf annotation data node changed error, node: {}, error: ", nodeName, e);
            }
        }

        @Override
        public WatcherCycleEnum cycleType() {
            return WatcherCycleEnum.CYCLE;
        }

        @Override
        public String key() {
            return key;
        }
    }
}
