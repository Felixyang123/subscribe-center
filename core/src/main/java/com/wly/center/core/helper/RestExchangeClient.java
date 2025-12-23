package com.wly.center.core.helper;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.pojo.Result;
import com.wly.center.core.pojo.req.OpenAddNodeReq;
import com.wly.center.core.pojo.req.OpenRenewNodeReq;
import com.wly.center.core.pojo.req.OpenUpdateNodeReq;
import com.wly.center.core.pojo.req.OpenWatchReq;
import com.wly.center.core.pojo.resp.OpenAddNodeResp;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public record RestExchangeClient(ClusterClient clusterClient) {

    private <T> T extractData(Result<T> result) {
        if (Boolean.TRUE.equals(result.getSuccess())) {
            return result.getData();
        }
        throw new BusinessException(result.getCode(), result.getMessage());
    }

    public OpenNodeDetailResp getNodeDetail(String nodeName) {
        return clusterClient.execute("/open/node/detail", (url, restClientHelper) -> {
            Result<OpenNodeDetailResp> result = restClientHelper.get(url, new ParameterizedTypeReference<Result<OpenNodeDetailResp>>() {
            }, MultiValueMap.fromSingleValue(Map.of("nodeName", nodeName)));
            return extractData(result);
        });
    }

    public OpenAddNodeResp addNode(OpenAddNodeReq req) {
        return clusterClient.execute("/open/node/add", (url, restClientHelper) -> {
            Result<OpenAddNodeResp> result = restClientHelper.post(url, req, new ParameterizedTypeReference<Result<OpenAddNodeResp>>() {
            });
            return extractData(result);
        });
    }

    public void removeNode(String nodeName) {
        clusterClient.execute("/open/node/remove", (url, restClientHelper) -> {
            Result<Void> result = restClientHelper.post(url, new ParameterizedTypeReference<Result<Void>>() {
            }, MultiValueMap.fromSingleValue(Map.of("nodeName", nodeName)));
            extractData(result);
            return null;
        });
    }

    public void updateNode(OpenUpdateNodeReq req) {
        clusterClient.execute("/open/node/update", (url, restClientHelper) -> {
            Result<Void> result = restClientHelper.post(url, req, new ParameterizedTypeReference<Result<Void>>() {
            });
            extractData(result);
            return null;
        });
    }

    public void watchNode(OpenWatchReq req) {
        clusterClient.execute("/open/watcher/watch", (url, restClientHelper) -> {
            Result<Void> result = restClientHelper.post(url, req, new ParameterizedTypeReference<Result<Void>>() {
            });
            extractData(result);
            return null;
        });
    }

    public List<OpenNodeDetailResp> children(String nodeName) {
        return clusterClient.execute("/open/node/children", (url, restClientHelper) -> {
            Result<List<OpenNodeDetailResp>> result = restClientHelper.get(url, new ParameterizedTypeReference<Result<List<OpenNodeDetailResp>>>() {
            }, MultiValueMap.fromSingleValue(Map.of("parentName", nodeName)));
            return extractData(result);
        });
    }

    public void renewNode(OpenRenewNodeReq req) {
        clusterClient.execute("/open/node/renew", (url, restClientHelper) -> {
            Result<Void> result = restClientHelper.post(url, req, new ParameterizedTypeReference<Result<Void>>() {
            });
            extractData(result);
            return null;
        });
    }
}
