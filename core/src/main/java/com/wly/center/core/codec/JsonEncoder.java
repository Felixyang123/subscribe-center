package com.wly.center.core.codec;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonEncoder extends MessageToByteEncoder<Object> {
    private final Class<?> genericClass;

    public JsonEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            byte[] data = JSON.toJSONBytes(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}