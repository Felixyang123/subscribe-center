package com.wly.center.core.client;

import com.wly.center.core.protocal.ExchangeResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ExchangeClientHandler extends SimpleChannelInboundHandler<ExchangeResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeResponse response) throws Exception {
        // 收到响应，完成对应的Future
        log.debug("Receive exchangeResponse: {}", response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exchange request fail:", cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Exchange connection lost");
        // 连接断开时，清除本地channel
        ChannelManager.removeChannel(ctx.channel());
        //TODO 删除该channel创建的临时node
        super.channelInactive(ctx);
    }
}