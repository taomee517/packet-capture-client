package com.aero.jpcap.consumer.handler.packet;

import com.aero.common.utils.ProtobufUtil;
import com.aero.jpcap.consumer.entity.PacketInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ChannelHandler.Sharable
public class PacketConvertHandler extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        if (msg instanceof byte[]) {
            byte[] packet = (byte[]) msg;
            if (packet.length>0) {
                PacketInfo packetInfo = ProtobufUtil.deserialize(packet, PacketInfo.class);
                out.add(packetInfo);
            }
        }else {
            log.error("收到的数据格式错误！ type = {}", msg.getClass().getName());
        }
    }
}
