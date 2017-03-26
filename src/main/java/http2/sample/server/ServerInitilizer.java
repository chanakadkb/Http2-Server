/*
 * Copyright (c) 2017, Chanakadkb. (http://medium.com/@chanakadkb) All Rights Reserved.
 */

package http2.sample.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import static io.netty.handler.logging.LogLevel.DEBUG;
import static io.netty.handler.logging.LogLevel.INFO;

public class ServerInitilizer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;

    public ServerInitilizer() {
        this.sslCtx = createSSLContext();
    }

    private static final Http2FrameLogger logger =
            new Http2FrameLogger(INFO, ServerInitilizer.class);

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        Http2Connection connection = new DefaultHttp2Connection(true);
        FrameListener frameListener = new FrameListener();
        Http2ConnectionHandler connectionHandler = new Http2ConnectionHandlerBuilder().connection
                (connection).
                frameListener(frameListener).frameLogger(logger).build();
	    SourceHandler sourceHandler=new SourceHandler(connection,connectionHandler.encoder());
        socketChannel.pipeline().addLast(sslCtx.newHandler(socketChannel.alloc()), connectionHandler,
                sourceHandler);
    }

    private SslContext createSSLContext(){
        SslContext sslCtx;

        try {
            SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(provider)
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,

                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1))
                    .build();
        }catch (Exception e){
            sslCtx=null;
        }
        return  sslCtx;
    }
}
