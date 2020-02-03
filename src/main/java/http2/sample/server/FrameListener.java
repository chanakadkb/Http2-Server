/*
 * Copyright (c) 2017, Chanakadkb. (http://medium.com/@chanakadkb) All Rights Reserved.
 */

package http2.sample.server;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.*;

public class FrameListener extends Http2EventAdapter {
    private Http2Connection connection;

    public FrameListener(Http2Connection connection) {
        this.connection = connection;
    }

    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
        final Http2Stream stream = connection.stream(streamId);
        Http2FrameStream frameStream;
        if (stream instanceof Http2FrameStream  ) {
            frameStream = (Http2FrameStream)stream;
        } else {
            frameStream = new Http2FrameStream() {
                @Override
                public int id() {
                    return stream.id();
                }

                @Override
                public Http2Stream.State state() {
                    return stream.state();
                }
            };
        }
        Http2HeadersFrame frame =
                new DefaultHttp2HeadersFrame(headers, endStream, padding).stream(frameStream);
        ctx.fireChannelRead(frame);
    }
/*    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
        Http2DataFrame frame=new DefaultHttp2DataFrame(data,endOfStream,padding).setStreamId(streamId);
        ctx.fireChannelRead(frame);
        return data.readableBytes() + padding;
    }*/
}
