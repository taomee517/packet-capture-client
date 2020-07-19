package com.aero.jpcap.consumer.netty;

import com.aero.jpcap.consumer.handler.device.BytdDevice;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class VirtualBytdClient implements InitializingBean, DisposableBean {

    @Value("${iot.platform.bytd.host}")
    String ip;

    @Value("${iot.platform.bytd.port}")
    int port;

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
                        pipeline.addLast("virtual-device", ch.attr(BytdDevice.DEVICE).get());
                    }
                });
        BytdDevice.client = client;
        BytdDevice.workers = workers;
        BytdDevice.remoteAddr = remoteAddr;
    }

    @Override
    public void destroy() throws Exception {
    }
}