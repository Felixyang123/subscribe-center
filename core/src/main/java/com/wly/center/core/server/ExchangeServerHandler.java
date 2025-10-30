package com.wly.center.core.server;

import com.alibaba.fastjson2.JSON;
import com.wly.center.core.enumeration.ExchangeType;
import com.wly.center.core.protocal.ExchangeRequest;
import com.wly.center.core.protocal.ExchangeResponse;
import com.wly.center.core.protocal.WatcherExchangeReq;
import com.wly.center.core.watcher.WatchClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RPC服务器请求处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class ExchangeServerHandler extends SimpleChannelInboundHandler<ExchangeRequest> {

    private final ExecutorService executorService;

    private final WatchClient watchClient;

    public ExchangeServerHandler(WatchClient watchClient) {
        // 使用可配置的线程池
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2
        );
        this.watchClient = watchClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeRequest request) throws Exception {
        log.debug("Receive exchange node request: {}", request.getRequestId());

        // 使用线程池处理请求，避免阻塞Netty的I/O线程
        executorService.submit(() -> {
            ExchangeResponse response = handleRequest(request);
            ctx.writeAndFlush(response).addListener(future -> {
                if (future.isSuccess()) {
                    log.debug("Send response success: {}", request.getRequestId());
                } else {
                    log.error("Send response fail: {}", request.getRequestId(), future.cause());
                }
            });
        });
    }

    private ExchangeResponse handleRequest(ExchangeRequest request) {
        if (ExchangeType.WATCHER.getCode().equals(request.getType())) {
            WatcherExchangeReq watcherExchangeReq = JSON.parseObject(JSON.toJSONString(request.getReq()), WatcherExchangeReq.class);
            watchClient.notify(watcherExchangeReq);
        }
        return ExchangeResponse.builder().requestId(request.getRequestId()).success(true).build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exchange node  fail: ", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Exchange server connected: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Exchange server connection lost: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 关闭线程池
     */
    @SneakyThrows
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Thread.sleep(1000);
            if (executorService.isShutdown()) {
                executorService.shutdownNow();
            }
        }
        log.info("Exchange server thread pool shut down");
    }
}