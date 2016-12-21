/*
 * Copyright (c) 2017, Chanakadkb. (http://medium.com/geek-with-chanaka) All Rights Reserved.
 */

package http2.sample.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Server {

    private static final int PORT=8888;
    public static void main(String[] args) throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        EventLoopGroup group=new NioEventLoopGroup();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerInitilizer());
        try {

            Channel ch = b.bind(PORT).sync().channel();
            ch.closeFuture().sync();
        } finally {
              group.shutdownGracefully();
        }

    }
}
