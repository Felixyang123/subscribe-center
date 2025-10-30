package com.wly.center.core.factory;

import com.wly.center.core.helper.NodeOptHelper;
import com.wly.center.core.helper.RenewNodeHelper;
import com.wly.center.core.helper.RestClientHelper;
import com.wly.center.core.helper.RestExchangeClient;
import com.wly.center.core.server.ExchangeServer;
import com.wly.center.core.watcher.WatchClient;
import lombok.Builder;
import org.springframework.http.HttpHeaders;

import java.util.Optional;
import java.util.function.Consumer;

@Builder
public record ExchangeServerFactory(int port,
                                    String defaultBaseUrl,
                                    Long renewIntervalSeconds,
                                    RestClientHelper defaultRestClient,
                                    RestExchangeClient defaultExchangeClient,
                                    WatchClient watchClient,
                                    RenewNodeHelper defaultRenewNodeHelper,
                                    NodeOptHelper defaultNodeOptHelper) {

    public ExchangeServerFactory(int port,
                                 String defaultBaseUrl,
                                 Long renewIntervalSeconds,
                                 RestClientHelper defaultRestClient,
                                 RestExchangeClient defaultExchangeClient,
                                 WatchClient watchClient,
                                 RenewNodeHelper defaultRenewNodeHelper,
                                 NodeOptHelper defaultNodeOptHelper) {
        this.port = port;
        this.defaultBaseUrl = defaultBaseUrl;
        this.renewIntervalSeconds = renewIntervalSeconds;
        this.defaultRestClient = Optional.ofNullable(defaultRestClient).orElseGet(() -> simpleRestClient(this.defaultBaseUrl));
        this.defaultExchangeClient = Optional.ofNullable(defaultExchangeClient).orElseGet(() -> new RestExchangeClient(this.defaultRestClient));
        this.watchClient = Optional.ofNullable(watchClient).orElseGet(() -> new WatchClient(this.defaultExchangeClient, this.port));
        this.defaultRenewNodeHelper = Optional.ofNullable(defaultRenewNodeHelper).orElseGet(() -> new RenewNodeHelper(this.renewIntervalSeconds));
        this.defaultNodeOptHelper = Optional.ofNullable(defaultNodeOptHelper).orElseGet(() -> new NodeOptHelper(this.watchClient, this.defaultRenewNodeHelper));
    }

    public RestClientHelper simpleRestClient(String baseUrl) {
        return restClient(baseUrl, null);
    }

    public RestClientHelper restClient(String baseUrl, Consumer<HttpHeaders> headersConsumer) {
        return RestClientHelper.builder()
                .defaultHeaders(headersConsumer)
                .baseUrl(baseUrl)
                .build();
    }

    public void start() {
        this.defaultRenewNodeHelper.start();

        ExchangeServer.init(port, this.watchClient);
    }

    public void stop() {
        this.defaultRenewNodeHelper.stop();
    }
}
