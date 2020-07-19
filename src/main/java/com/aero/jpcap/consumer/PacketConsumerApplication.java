package com.aero.jpcap.consumer;

import com.aero.common.utils.SystemUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PacketConsumerApplication {
    public static void main(String[] args) {
        String localIp = SystemUtil.getRealIP();
        System.setProperty("local-ip",localIp);
        SpringApplication.run(PacketConsumerApplication.class);
    }
}
