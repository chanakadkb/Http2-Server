/*
 * Copyright (c) 2017, Chanakadkb. (http://medium.com/@chanakadkb) All Rights Reserved.
 */

package http2.sample.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;


public class SourceHandler extends ChannelDuplexHandler{
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;

    final String html="<html>\n" +
            "<head><title>ChanakaDKB</title><link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\"></head>\n" +
            "<body><h1>https://medium.com/@chanakadkb</h1></body>\n" +
            "</html>";
    final String css ="h1 {\n" +
            "    color: white;\n" +
            "    text-align: center;\n" +
            "    background-color: lightblue;\n" +
            "}";

    public SourceHandler(Http2Connection connection, Http2ConnectionEncoder encoder) {
        this.connection=connection;
        this.encoder=encoder;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame ) {
            Http2HeadersFrame frame = (Http2HeadersFrame) msg;
            String path = frame.headers().path().toString();

            if(path.equalsIgnoreCase("/all")){
                int prmiseId=connection.local().incrementAndGetNextStreamId();
                Http2Headers prmiseHeader=new DefaultHttp2Headers();
                prmiseHeader.scheme(frame.headers().scheme()).
                        status(HttpResponseStatus.OK.codeAsText()).
                        path("main.css").
                        add(HttpHeaderNames.CONTENT_TYPE,"text/css");

                ChannelPromise promise=ctx.newPromise();

                Http2Headers responseHeader=new DefaultHttp2Headers();
                responseHeader.scheme(frame.headers().scheme())
                        .status(HttpResponseStatus.OK.codeAsText()).
                        path("index.html").
                        add(HttpHeaderNames.CONTENT_TYPE,"text/html");

                encoder.writePushPromise(ctx,frame.streamId(),prmiseId,prmiseHeader,0,promise);

                ByteBuf promiseBuf= Unpooled.copiedBuffer(css.getBytes());
                encoder.writeHeaders(ctx,prmiseId,prmiseHeader,0,false,promise);
                encoder.writeData(ctx,prmiseId,promiseBuf,0,true,promise);

                ByteBuf htmlBuf= Unpooled.copiedBuffer(html.getBytes());
                encoder.writeHeaders(ctx,frame.streamId(),responseHeader,0,false,promise);
                encoder.writeData(ctx,frame.streamId(),htmlBuf,0,true,promise);

                ctx.flush();
            }else if(path.equalsIgnoreCase("/main.css")){
                ChannelPromise promise=ctx.newPromise();
                Http2Headers responseHeader=new DefaultHttp2Headers();
                responseHeader.scheme(frame.headers().scheme())
                        .status(HttpResponseStatus.OK.codeAsText()).
                        path("main.css").
                        add(HttpHeaderNames.CONTENT_TYPE,"text/css");
                ByteBuf cssBuf= Unpooled.copiedBuffer(css.getBytes());
                encoder.writeHeaders(ctx,frame.streamId(),responseHeader,0,false,promise);
                encoder.writeData(ctx,frame.streamId(),cssBuf,0,true,promise);
                ctx.flush();

            }else{
                System.out.println(path);
                ChannelPromise promise=ctx.newPromise();
                Http2Headers responseHeader=new DefaultHttp2Headers();
                responseHeader.scheme(frame.headers().scheme())
                        .status(HttpResponseStatus.OK.codeAsText()).
                        path("index.html").
                        add(HttpHeaderNames.CONTENT_TYPE,"text/html");
                ByteBuf htmlBuf= Unpooled.copiedBuffer(html.getBytes());
                encoder.writeHeaders(ctx,frame.streamId(),responseHeader,0,false,promise);
                encoder.writeData(ctx,frame.streamId(),htmlBuf,0,true,promise);
                ctx.flush();
            }
        }
    }
}
