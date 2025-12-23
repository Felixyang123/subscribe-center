package com.wly.center.core.factory;

import com.wly.center.core.helper.*;
import com.wly.center.core.server.ExchangeServer;
import com.wly.center.core.watcher.LocalWatcherManager;
import com.wly.center.core.watcher.WatchClient;
import lombok.Builder;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Builder
public record ExchangeServerFactory(int port, String registerName, String defaultBaseUrl, Long renewIntervalSeconds,
                                    String adminServiceName, List<String> discoveryAddresses,
                                    Integer refreshClusterIntervalSeconds, Integer healthCheckIntervalSeconds,
                                    RestClientHelper defaultRestClient, ClusterClient defaultClusterClient,
                                    RestExchangeClient defaultExchangeClient, LocalWatcherManager defaultWatcherManager,
                                    WatchClient watchClient, RenewNodeHelper defaultRenewNodeHelper,
                                    NodeOptHelper defaultNodeOptHelper) {
    public ExchangeServerFactory(int port,
                                 String registerName,
                                 String defaultBaseUrl,
                                 Long renewIntervalSeconds,
                                 String adminServiceName, List<String> discoveryAddresses,
                                 Integer refreshClusterIntervalSeconds, Integer healthCheckIntervalSeconds,
                                 RestClientHelper defaultRestClient,
                                 ClusterClient defaultClusterClient,
                                 RestExchangeClient defaultExchangeClient,
                                 LocalWatcherManager defaultWatcherManager,
                                 WatchClient watchClient,
                                 RenewNodeHelper defaultRenewNodeHelper,
                                 NodeOptHelper defaultNodeOptHelper) {
        this.port = port;
        this.registerName = registerName;
        this.defaultBaseUrl = defaultBaseUrl;
        this.renewIntervalSeconds = renewIntervalSeconds;
        this.adminServiceName = adminServiceName;
        this.discoveryAddresses = discoveryAddresses;
        this.refreshClusterIntervalSeconds = refreshClusterIntervalSeconds;
        this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
        this.defaultRestClient = Optional.ofNullable(defaultRestClient).orElseGet(() -> simpleRestClient(this.defaultBaseUrl));
        this.defaultClusterClient = Optional.ofNullable(defaultClusterClient).orElseGet(() -> new ClusterClient(adminServiceName, discoveryAddresses, refreshClusterIntervalSeconds, healthCheckIntervalSeconds));
        this.defaultExchangeClient = Optional.ofNullable(defaultExchangeClient).orElseGet(() -> new RestExchangeClient(this.defaultClusterClient));
        this.defaultWatcherManager = Optional.ofNullable(defaultWatcherManager).orElseGet(LocalWatcherManager::new);
        this.watchClient = Optional.ofNullable(watchClient).orElseGet(() -> new WatchClient(this.defaultExchangeClient, this.defaultWatcherManager, this.port));
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
        this.defaultClusterClient.destroy();
    }

    public long getExpireTimestamp() {
        return this.renewIntervalSeconds * 1000 * 3 + System.currentTimeMillis();
    }
}
