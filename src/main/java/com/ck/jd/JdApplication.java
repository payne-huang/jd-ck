package com.ck.jd;

import com.ck.jd.control.ScribeThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JdApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdApplication.class, args);
        ScribeThread scribeThread = new ScribeThread();
        scribeThread.start();
    }
}
