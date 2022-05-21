package com.ck.jd;

import com.ck.jd.control.ScribeThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class JdApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdApplication.class, args);
        ScribeThread scribeThread = new ScribeThread();
        scribeThread.run();
    }
}
