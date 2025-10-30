package com.wly.center.core.client;

import com.wly.center.core.codec.JsonDecoder;
import com.wly.center.core.codec.JsonEncoder;
import com.wly.center.core.protocal.ExchangeRequest;
import com.wly.center.core.protocal.ExchangeResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChannelManager {
    private static final ConcurrentMap<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, ChannelWrapper> CHANNEL_WRAPPER_MAP = new ConcurrentHashMap<>();

    private static final EventLoopGroup EVENTLOOPGROUP = new NioEventLoopGroup();

    private static final ExchangeClientHandler HANDLER = new ExchangeClientHandler();

    public static Channel getChannel(String ip, Integer port) {
        String channelKey = ip + ":" + port;
        return CHANNEL_MAP.computeIfAbsent(channelKey, k -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(EVENTLOOPGROUP)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new JsonDecoder(ExchangeResponse.class));
                            pipeline.addLast(new JsonEncoder(ExchangeRequest.class));
                            pipeline.addLast(HANDLER);
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            try {
                Channel channel = bootstrap.connect(ip, port).sync().channel();
                CHANNEL_WRAPPER_MAP.put(channel.id().asLongText(), new ChannelWrapper(channel, channelKey));
                return channel;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });
    }

    public static void removeChannel(Channel channel) {
        ChannelWrapper channelWrapper = CHANNEL_WRAPPER_MAP.remove(channel.id().asLongText());
        if (channelWrapper != null) {
            channelWrapper.getChannel().close();
            CHANNEL_MAP.remove(channelWrapper.getChannelKey());
        }
    }

    @Data
    @AllArgsConstructor
    public static class ChannelWrapper {
        private Channel channel;

        private String channelKey;
    }
}
