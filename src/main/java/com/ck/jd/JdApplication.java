package com.ck.jd;

import com.ck.jd.control.ScribeThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class JdApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdApplication.class, args);

        ScribeThread scribeThread = new ScribeThread();
        scribeThread.start();
        log.info("订阅启动成功");

        PushWxTread pushWxTread = new PushWxTread();
        pushWxTread.start();
        log.info("日期检查启动成功");
    }
}
