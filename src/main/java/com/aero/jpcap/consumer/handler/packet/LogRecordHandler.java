package com.aero.jpcap.consumer.handler.packet;

import com.aero.common.constants.SensorProtocol;
import com.aero.common.utils.BytesUtil;
import com.aero.jpcap.consumer.entity.PacketInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class LogRecordHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            PacketInfo packetInfo = (PacketInfo) msg;
            String srcIp = packetInfo.getSrcIp();
            int srcPort = packetInfo.getSrcPort();
            String destIp = packetInfo.getDestIp();
            int destPort = packetInfo.getDestPort();
            String deviceAddr = null;
            if(SensorProtocol.BYTD.getPort()==srcPort){
                //bytd down stream
                deviceAddr = StringUtils.joinWith(":", destIp, destPort);
                log.info("BYTD下行消息， addr: {}, msg: {}", deviceAddr, BytesUtil.bytes2HexWithBlank(packetInfo.getContent(), true));
            }else if(SensorProtocol.BYTD.getPort()==destPort){
                //bytd up stream
                deviceAddr = StringUtils.joinWith(":", srcIp, srcPort);
                log.info("BYTD上行消息， addr: {}, msg: {}", deviceAddr, BytesUtil.bytes2HexWithBlank(packetInfo.getContent(), true));
            }else if(SensorProtocol.BYTD_GK.getPort()==srcPort){
                //gk down stream
                deviceAddr = StringUtils.joinWith(":", destIp, destPort);
                log.info("GK下行消息， addr: {}, msg: {}", deviceAddr, new String(packetInfo.getContent()));
            }else if(SensorProtocol.BYTD_GK.getPort()==destPort){
                //gk up stream
                deviceAddr = StringUtils.joinWith(":", srcIp, srcPort);
                log.info("GK上行消息， addr: {}, msg: {}", deviceAddr, new String(packetInfo.getContent()));
            }
        } finally {
            ctx.fireChannelRead(msg);
        }

    }
}
