package com.wly.center.core.client;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.protocal.ExchangeRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExchangeClient {

    public static void send(ExchangeRequest request) {
        Channel channel = ChannelManager.getChannel(request.getIp(), request.getPort());

        if (channel == null) {
            throw new BusinessException("CONNECT_FAIL", "Connect to " + request.getIp() + ":" + request.getPort() + " fail");
        }

        log.debug("Send request: {}", request.getRequestId());
        ChannelFuture channelFuture = channel.writeAndFlush(request).addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                log.error("Send request fail: ", f.cause());
            }
        });

        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            log.error("Send request fail: ", e);
        }
    }
}
