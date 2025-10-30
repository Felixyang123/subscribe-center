package com.wly.center.core.server;

import com.wly.center.core.codec.JsonDecoder;
import com.wly.center.core.codec.JsonEncoder;
import com.wly.center.core.protocal.ExchangeRequest;
import com.wly.center.core.protocal.ExchangeResponse;
import com.wly.center.core.watcher.WatchClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExchangeServer {
    private final int port;
    private final ExchangeServerHandler serverHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public ExchangeServer(int port, WatchClient watchClient) {
        this.port = port;
        this.serverHandler = new ExchangeServerHandler(watchClient);
    }

    public static void init(int port, WatchClient watchClient) {
        ExchangeServer bootstrap = new ExchangeServer(port, watchClient);
        bootstrap.start();
    }

    private void start() {
        Thread bootstrapThread = new Thread(() -> {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));
                                pipeline.addLast(new LengthFieldPrepender(4));
                                pipeline.addLast(new JsonDecoder(ExchangeRequest.class));
                                pipeline.addLast(new JsonEncoder(ExchangeResponse.class));
                                pipeline.addLast(serverHandler);
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind(port).sync();
                log.info("Exchange server started on port {}", port);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                shutdown();
            }
        });
        bootstrapThread.setName("Exchange server start thread-" + port);
        bootstrapThread.start();
    }

    private void shutdown() {
        serverHandler.shutdown();
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        log.info("Exchange server shut down, port {}", port);
    }
}
