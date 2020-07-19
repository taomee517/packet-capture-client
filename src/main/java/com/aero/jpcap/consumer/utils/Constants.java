package com.aero.jpcap.consumer.utils;

public class Constants {
    /**起始符和结束符*/
    public static final byte SIGN_CODE = 0x7e;

    /**转义符号*/
    public static final byte ESCAPE_SIGN = 0x7d;

    /**转义后的7D*/
    public static final byte[] ESCAPE_7D = {0x7d,0x00};

    /**转义后的7E*/
    public static final byte[] ESCAPE_7E = {0x7d,0x01};
}
