package com.aero.jpcap.consumer.handler.packet;

import com.aero.common.utils.BytesUtil;
import com.aero.jpcap.consumer.utils.EscapeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ByteProcessor;
import lombok.extern.slf4j.Slf4j;

import static com.aero.jpcap.consumer.utils.Constants.*;

import java.util.List;
import java.util.Objects;

@Slf4j
public class FrameSplitHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
//            byte[] packet = new byte[in.readableBytes()];
//            in.readBytes(packet);
            ByteBuf split = split(in);
            if (Objects.nonNull(split)) {
                byte[] escapePacket = new byte[split.readableBytes()];
                split.readBytes(escapePacket);
                ByteBuf buf = Unpooled.wrappedBuffer(escapePacket);
                EscapeUtil.unescape(buf);
                byte[] packet = new byte[buf.readableBytes()-2];
                buf.readByte();
                buf.readBytes(packet);
                out.add(packet);
                buf.release();
                split.release();
            }
        } finally {
            resetBuffer(in);
        }
    }


    private void resetBuffer(ByteBuf buffer){
        int left = buffer.readableBytes();
        int start = buffer.readerIndex();
        if (left == 0 && buffer.readerIndex() > 0){
            buffer.setIndex(0, 0);
            return;
        }
        if (left > 0 && buffer.readerIndex() > 0) {
            for (int index = 0; index < left; index++) {
                buffer.setByte(index, buffer.getByte(index + start));
            }
        }
        buffer.setIndex(0, left);
    }

    public static ByteBuf split(ByteBuf in){
        int readableLen = in.readableBytes();
        if (readableLen < 2) {
            return null;
        }
        int startSignIndex = in.forEachByte(new ByteProcessor.IndexOfProcessor(SIGN_CODE));
        if(startSignIndex==-1){
            return null;
        }
        //将readerIndex置为起始符下标+1
        //因为起始符结束符是一样的，如果不往后移一位，下次到的还是起始下标
        in.readerIndex(startSignIndex + 1);

        //找到第一个报文结束符的下标
        int endSignIndex = in.forEachByte(new ByteProcessor.IndexOfProcessor(SIGN_CODE));
        if(endSignIndex == -1 || endSignIndex < startSignIndex){
            in.readerIndex(startSignIndex);
            return null;
        }


        //计算报文的总长度
        //此处不能去操作writerIndex,否则只能截取到第一条完整报文
        int length = endSignIndex + 1 - startSignIndex;

        //如果长度还小于最小长度，就丢掉这条消息
        if(length < 2){
            byte[] errMsg = new byte[length];
            for(int i= startSignIndex; i< (endSignIndex + 1); i++){
                int errIndex = i-startSignIndex;
                errMsg[errIndex] = in.getByte(i);
            }

            String hexMsg = BytesUtil.bytes2HexWithBlank(errMsg, true);
//            log.error("异常消息：{}", hexMsg);
            System.out.println("异常消息：" + hexMsg);
            in.readerIndex(endSignIndex);
            return null;
        }

        //将报文内容写入符串，并返回
        byte[] data = new byte[length];
        in.readerIndex(startSignIndex);
        in.readBytes(data);
        return Unpooled.wrappedBuffer(data);
    }
}
