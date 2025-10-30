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

public record RestExchangeClient(RestClientHelper restClientHelper) {

    private <T> T extractData(Result<T> result) {
        if (result.getSuccess()) {
            return result.getData();
        }
        throw new BusinessException(result.getCode(), result.getMessage());
    }

    public OpenNodeDetailResp getNodeDetail(String nodeName) {
        Result<OpenNodeDetailResp> result = restClientHelper.get("/open/node/detail", new ParameterizedTypeReference<Result<OpenNodeDetailResp>>() {
        }, MultiValueMap.fromSingleValue(Map.of("nodeName", nodeName)));

        return extractData(result);
    }

    public OpenAddNodeResp addNode(OpenAddNodeReq req) {
        Result<OpenAddNodeResp> result = restClientHelper.post("/open/node/add", req, new ParameterizedTypeReference<Result<OpenAddNodeResp>>() {
        });
        return extractData(result);
    }

    public void removeNode(String nodeName) {
        Result<Void> result = restClientHelper.post("/open/node/remove", new ParameterizedTypeReference<Result<Void>>() {
        }, MultiValueMap.fromSingleValue(Map.of("nodeName", nodeName)));
        extractData(result);
    }

    public void updateNode(OpenUpdateNodeReq req) {
        Result<Void> result = restClientHelper.post("/open/node/update", req, new ParameterizedTypeReference<Result<Void>>() {
        });
        extractData(result);
    }

    public void watchNode(OpenWatchReq req) {
        Result<Void> result = restClientHelper.post("/open/watcher/watch", req, new ParameterizedTypeReference<Result<Void>>() {
        });
        extractData(result);
    }

    public List<OpenNodeDetailResp> children(String nodeName) {
        Result<List<OpenNodeDetailResp>> result = restClientHelper.get("/open/node/children", new ParameterizedTypeReference<Result<List<OpenNodeDetailResp>>>() {
        }, MultiValueMap.fromSingleValue(Map.of("parentName", nodeName)));
        return extractData(result);
    }

    public void renewNode(OpenRenewNodeReq req) {
        Result<Void> result = restClientHelper.post("/open/node/renew", req, new ParameterizedTypeReference<Result<Void>>() {
        });
        extractData(result);
    }
}
