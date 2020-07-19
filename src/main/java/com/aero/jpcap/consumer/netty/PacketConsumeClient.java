package com.aero.jpcap.consumer.netty;

import com.aero.jpcap.consumer.handler.packet.DataForwardingHandler;
import com.aero.jpcap.consumer.handler.packet.FrameSplitHandler;
import com.aero.jpcap.consumer.handler.packet.LogRecordHandler;
import com.aero.jpcap.consumer.handler.packet.PacketConvertHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PacketConsumeClient implements InitializingBean, DisposableBean {

    @Value("${packet.capture.server.host}")
    String ip;

    @Value("${packet.capture.server.port}")
    int port;

    @Autowired
    DataForwardingHandler dataForwardingHandler;

    @Autowired
    LogRecordHandler logRecordHandler;

    @Autowired
    PacketConvertHandler packetConvertHandler;

    Bootstrap client = new Bootstrap();
    NioEventLoopGroup workers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2);
    InetSocketAddress remoteAddr;

    @Override
    public void afterPropertiesSet() throws Exception {
        remoteAddr = new InetSocketAddress(ip,port);
        client.group(workers)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("split", new FrameSplitHandler());
                        pipeline.addLast("packet-convert", packetConvertHandler);
                        pipeline.addLast("log-record", logRecordHandler);
                        pipeline.addLast("data-redirect", dataForwardingHandler);
                    }
                });
        workers.schedule(connectAction,0,TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
    }

    Runnable connectAction = new Runnable() {
        @Override
        public void run() {
            ChannelFuture channelFuture = client.connect(remoteAddr);

            channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(!future.isSuccess()){
                        log.info("连接抓包服务端失败，ip={}， port={}", ip, port);
                        reconnect();
                    }
                }
            });
        }
    };

    private void reconnect(){
        int interval = 5;
        log.info("{}秒钟后发起重连！",interval);
        workers.schedule(connectAction, interval, TimeUnit.SECONDS);
    }
}