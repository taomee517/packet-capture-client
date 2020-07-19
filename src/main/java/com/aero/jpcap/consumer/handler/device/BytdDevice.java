package com.aero.jpcap.consumer.handler.device;

import com.aero.common.utils.BytesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author 罗涛
 * @title BytdDevice
 * @date 2020/6/22 16:49
 */
@Slf4j
@ChannelHandler.Sharable
public class BytdDevice extends ChannelDuplexHandler implements IDevice{
    public static final AttributeKey<ChannelHandler> DEVICE = AttributeKey.valueOf("DEVICE");
    public static Bootstrap client;
    public static NioEventLoopGroup workers;
    public static InetSocketAddress remoteAddr;

    private Channel channel;

    private String imei;
    private int retry = 0;

    public BytdDevice(String imei){
        this.imei = imei;
        workers.schedule(connect, 0, TimeUnit.SECONDS);
    }

    Runnable connect = new Runnable() {
        @Override
        public void run() {
            ChannelFuture future = null;
            synchronized (DEVICE) {
                future = client.attr(DEVICE, BytdDevice.this)
                        .connect(remoteAddr);
                log.info("与BYTD服务端建立连接");
            }

            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(!future.isSuccess()){
                        reconnect();
                    }
                }
            });
        }
    };

    /**
     * 重连间隔时间
     * @return 间隔时间
     */
    private int getReconnectInterval(){
        return Math.min(15, retry);
    }

    /**
     * 重新连接平台
     */
    private void reconnect(){
        retry ++;
        int interval = getReconnectInterval();
        log.info("{}秒钟后发起重连！",interval);
        //重连间隔暂时写死
        workers.schedule(connect, interval, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        retry = 0;
        channel = ctx.channel();
        log.info("channelActive, imei: {}", this.imei);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("设备{}与平台断开连接！", this.imei);
        ctx.close();
        reconnect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        String hexMsg = BytesUtil.bytes2Hex(bytes);
        String asciiMsg = new String(bytes);
        log.info("设备{}收到测试服务器消息：hex = {}, ascii = {}", this.imei,  hexMsg, asciiMsg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        switch (idleStateEvent.state()){
            case WRITER_IDLE:
                sendRealMsg(ctx);
//                sendRealAsciiMsg(ctx);
                break;
            default:break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常：{}", cause);
        ctx.close();
    }


    @Override
    public void send(byte[] msg) {
        if(Objects.nonNull(channel)){
            channel.writeAndFlush(msg);
        }
    }

    private static boolean sendFlag = false;

    private void sendRealMsg(ChannelHandlerContext ctx){
        if (!sendFlag) {
            String msg = "2a 00 23 58 2b 05 00 00 00 98 d3 96 58 97 b6 97 fd 98 30 96 c1 89 4d 89 4d 85 60 85 64 85 59 85 58 85 4c 85 4d e2 20 e2 1f 02 2a";
            log.info("发送消息，msg = {}",msg);
            ctx.writeAndFlush(msg);
            sendFlag = true;
        }
    }

//    private void sendRealAsciiMsg(ChannelHandlerContext ctx){
//        if (!sendFlag) {
//            String msg = ":01240551200616210000+849.076+2142.98+742.741+2116.90+816.008+2131.70+812.457+2128.67+851.481+2112.02+811.711+2096.87+805.100+2117.29+877.116+2102.42+751.756+2109.69+863.411+2090.44+787.796+2069.53+841.919+2088.33+2058.72+2064.75+1699.02+2076.01+1835.25+2091.05+1745.04+2116.67+1812.51+2094.61+3141.24+2131.12+3244.64+2079.76E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E1000000E100000050\r\n";
//            log.info("发送消息，msg = {}",msg);
//            ctx.writeAndFlush(msg.getBytes());
//            sendFlag = true;
//        }
//    }
}
