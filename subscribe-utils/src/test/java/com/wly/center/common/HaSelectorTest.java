package com.wly.center.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wly.center.common.ha.HaSelector;
import com.wly.center.core.factory.ExchangeServerFactory;
import com.wly.center.core.pojo.resp.OpenNodeDetailResp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HaSelectorTest {

    @BeforeEach
    public void before() {

    }

    @Test
    void haRegisterTest() {
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .port(8300)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory.start();
        HaSelector selector = new HaSelector(factory);

        ExchangeServerFactory factory1 = ExchangeServerFactory.builder()
                .port(8301)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory1.start();
        HaSelector selector1 = new HaSelector(factory1);

        ExchangeServerFactory factory2 = ExchangeServerFactory.builder()
                .port(8302)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(60L)
                .build();
        factory2.start();
        HaSelector selector2 = new HaSelector(factory2);

        List<HaSelector> selectors = new ArrayList<>();

        selectors.add(selector);
        selectors.add(selector1);
        selectors.add(selector2);

        List<HaSelector> successSelectors = selectors.stream().filter(s -> s.select("test")).toList();
        Assertions.assertEquals(1, successSelectors.size());

        HaSelector first = successSelectors.getFirst();
        OpenNodeDetailResp node = first.serverFactory().defaultNodeOptHelper().getNodeDetail("test");
        Assertions.assertNotNull(node);
        JSONObject clientJson = JSON.parseObject(node.getData());
        Assertions.assertEquals(first.serverFactory().port(), clientJson.getLong("clientPort"));
    }


    @Test
    void haFailOverTest() throws Exception{
        ExchangeServerFactory factory = ExchangeServerFactory.builder()
                .port(8300)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(1L)
                .build();
        factory.start();
        HaSelector selector = new HaSelector(factory);

        ExchangeServerFactory factory1 = ExchangeServerFactory.builder()
                .port(8301)
                .defaultBaseUrl("http://127.0.0.1:8200")
                .renewIntervalSeconds(1L)
                .build();
        factory1.start();
        HaSelector selector1 = new HaSelector(factory1);

        List<HaSelector> selectors = Stream.of(selector, selector1).filter(s -> s.select("test1")).toList();
        Assertions.assertEquals(1, selectors.size());

        HaSelector master = selectors.getFirst();
        OpenNodeDetailResp node = master.serverFactory().defaultNodeOptHelper().getNodeDetail("test1");
        Assertions.assertNotNull(node);
        JSONObject clientJson = JSON.parseObject(node.getData());
        Assertions.assertEquals(master.serverFactory().port(), clientJson.getLong("clientPort"));

        Thread.sleep(4000);

        node = master.serverFactory().defaultNodeOptHelper().getNodeDetail("test1");
        Assertions.assertNotNull(node);
        clientJson = JSON.parseObject(node.getData());
        Assertions.assertEquals(master.serverFactory().port(), clientJson.getInteger("clientPort"));

        Integer slavePort = Stream.of(factory.port(), factory1.port()).filter(p -> p != master.serverFactory().port()).toList().getFirst();

        master.serverFactory().stop();
        Thread.sleep(4000);

        node = master.serverFactory().defaultNodeOptHelper().getNodeDetail("test1");
        Assertions.assertNotNull(node);
        clientJson = JSON.parseObject(node.getData());
        Assertions.assertEquals(slavePort, clientJson.getInteger("clientPort"));
    }
}
