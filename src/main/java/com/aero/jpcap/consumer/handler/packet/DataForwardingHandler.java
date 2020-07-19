package com.aero.jpcap.consumer.handler.packet;

import com.aero.common.constants.SensorProtocol;
import com.aero.common.utils.ProtobufUtil;
import com.aero.jpcap.consumer.entity.PacketInfo;
import com.aero.jpcap.consumer.handler.device.BytdDevice;
import com.aero.jpcap.consumer.handler.device.IDevice;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@ChannelHandler.Sharable
public class DataForwardingHandler extends ChannelDuplexHandler {
    private static Map<String, IDevice> deviceMap = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive");
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PacketInfo packetInfo = (PacketInfo) msg;
        String srcIp = packetInfo.getSrcIp();
        int srcPort = packetInfo.getSrcPort();
        String deviceAddr = StringUtils.joinWith(":", srcIp, srcPort);
        boolean flag = deviceMap.containsKey(deviceAddr);
        IDevice device = null;
        if(flag){
            device = deviceMap.get(deviceAddr);
        }else {
            if(packetInfo.getDestPort()== SensorProtocol.BYTD.getPort()){
                device = new BytdDevice(deviceAddr);
                deviceMap.put(deviceAddr,device);
            }else {
                //GK
            }
        }
        if(Objects.nonNull(device)){
            device.send(packetInfo.getContent());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        log.error("发生异常：cause: {}", cause);
        ctx.close();
    }
}
