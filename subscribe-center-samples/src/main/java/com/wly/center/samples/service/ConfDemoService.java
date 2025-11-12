package com.wly.center.samples.service;

import com.wly.center.common.conf.ConfClient;
import com.wly.center.common.conf.ConfData;
import com.wly.center.core.pojo.req.OpenUpdateNodeReq;
import com.wly.center.starter.annotation.DynamicConf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfDemoService {

    @DynamicConf(key = "demo.conf.key01", refresh = true)
    private String conf1 = "default conf1";

    @DynamicConf(key = "demo.conf.key02", refresh = true)
    private Integer conf2 = 0;

    private final ConfClient confClient;

    public Map<String, Object> confList() {
        return Map.of("conf1", conf1, "conf2", conf2);
    }

    public Object conf(String key) {
        return Optional.ofNullable(confClient.get(key, String.class)).map(ConfData::getData).orElse(null);
    }

    public void update(Map<String, Object> confParams) {
        List<OpenUpdateNodeReq> reqs = confParams.entrySet().stream().map(entry -> OpenUpdateNodeReq
                .builder().name(entry.getKey()).data(String.valueOf(entry.getValue())).build()).toList();
        for (OpenUpdateNodeReq req : reqs) {
            confClient.getWatchClient().restExchangeClient().updateNode(req);
        }
    }
}
