/*
 * Copyright (c) 2017, Chanakadkb. (http://medium.com/geek-with-chanaka) All Rights Reserved.
 */

package http2.sample.server;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.*;

public class FrameListener extends Http2EventAdapter{

    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
        Http2HeadersFrame frame=new DefaultHttp2HeadersFrame(headers,endStream,padding).setStreamId(streamId);
        ctx.fireChannelRead(frame);
    }

    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
        Http2DataFrame frame=new DefaultHttp2DataFrame(data,endOfStream,padding).setStreamId(streamId);
        ctx.fireChannelRead(frame);
        return data.readableBytes() + padding;
    }
}
