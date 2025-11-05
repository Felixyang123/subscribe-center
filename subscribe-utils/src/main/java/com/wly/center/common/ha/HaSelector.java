package com.wly.center.common.ha;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import com.wly.center.core.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public record HaSelector(ExchangeServerFactory serverFactory) {

    public boolean select(String masterName) {
        try {
            OpenNodeDetailResp node = serverFactory.defaultNodeOptHelper().createTemporaryMasterNode(masterName,
                    Map.of("clientIp", NetworkUtils.getServerIp(), "clientPort", serverFactory.port()),
                    serverFactory.getExpireTimestamp());
            log.info("Master node created: {}", node);
            return true;
        } catch (Exception e) {
            if (e instanceof BusinessException businessException && BusinessExceptions.NODE_EXIST.name().equals(businessException.getCode())) {
                log.warn("Master node already exists: {}", masterName);
                serverFactory.watchClient().watch(masterName, new HaWatcher(this));
                return false;
            } else {
                throw e;
            }
        }
    }
}
