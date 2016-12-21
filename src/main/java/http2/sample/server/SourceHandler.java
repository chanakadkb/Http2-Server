/*
 * Copyright (c) 2017, Chanakadkb. (http://medium.com/geek-with-chanaka) All Rights Reserved.
 */

package http2.sample.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.handler.codec.http2.StreamBufferingEncoder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SourceHandler extends ChannelDuplexHandler{
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;

    public SourceHandler(Http2Connection connection, Http2ConnectionEncoder encoder) {
        this.connection=connection;
        this.encoder=encoder;
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame frame = (Http2HeadersFrame) msg;
                System.out.println("sending a html page with a css file as a server push");
                int prmiseId=connection.local().incrementAndGetNextStreamId();
                Http2Headers prmiseHeader=new DefaultHttp2Headers();
                prmiseHeader.scheme(frame.headers().scheme())
                        .path("/main.css")
                        .add(HttpHeaderNames.CONTENT_TYPE,"text/css");
                ChannelPromise promise=ctx.newPromise();

                Http2Headers responseHeader=new DefaultHttp2Headers();
                responseHeader.scheme(frame.headers().scheme())
                        .status(HttpResponseStatus.OK.codeAsText())
                        .add(HttpHeaderNames.CONTENT_TYPE,"text/html")
                        .add("link","<https://0.0.0.0:8888/main.css>; rel=preload; as=style");
                prmiseHeader.add("cache-control","max-age=31536000");
                prmiseHeader.add("link", "rel=preload");
                encoder.writePushPromise(ctx,frame.streamId(),prmiseId,prmiseHeader,0,promise);
                try {
                    ClassLoader classLoader = getClass().getClassLoader();
                    File cssFile=new File
                            ("/media/chanaka/Chanaka2/http2-sample/http2-server/src/main" +
                                    "/resources/main.css");
                    File htmlFile=new File("/media/chanaka/Chanaka2/http2-sample/http2-server/src/main" +
                            "/resources/main.html");
                    FileInputStream cssIn=new FileInputStream(cssFile);
                    FileInputStream htmlIn=new FileInputStream(htmlFile);
                    ByteBuf promiseBuf= Unpooled.copiedBuffer(IOUtils.toByteArray(cssIn));
                    ByteBuf htmlBuf= Unpooled.copiedBuffer(IOUtils.toByteArray(htmlIn));
                    encoder.writeHeaders(ctx,prmiseId,prmiseHeader,0,false,promise);
                    encoder.writeData(ctx,prmiseId,promiseBuf,0,true,promise);
                    ctx.flush();
                    promise=ctx.newPromise();
                    encoder.writeHeaders(ctx,frame.streamId(),responseHeader,0,false,promise);
                    encoder.writeData(ctx,frame.streamId(),htmlBuf,0,true,promise);
                    ctx.flush();
                    cssIn.close();
                    htmlIn.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
    }
}
