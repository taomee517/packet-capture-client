//package com.aero.jpcap.consumer.handler.packet;
//
//import com.aero.jpcap.consumer.utils.EscapeUtil;
//import com.aero.std.common.sdk.AeroParser;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
///**
// * @author 罗涛
// * @title UnescapeHandler
// * @date 2020/5/8 11:23
// */
//@Component
//@ChannelHandler.Sharable
//@Slf4j
//public class UnescapeHandler extends ChannelInboundHandlerAdapter {
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//
////        ByteBuf buf = ((ByteBuf) msg);
////        showHexMsg(buf,true);
//        EscapeUtil.unescape(buf);
////        showHexMsg(buf,false);
//        ctx.fireChannelRead(buf);
//    }
//
////    private void showHexMsg(ByteBuf buf, boolean before){
////        String escapeMsg = AeroParser.buffer2Hex(buf);
////        String beforeChar = before?"前":"后";
////        log.info("反转义{}的消息为：{}", beforeChar, escapeMsg);
////    }
//}
