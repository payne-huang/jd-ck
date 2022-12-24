package com.ck.jd.control;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.net.URLEncoder;

@Component
@Slf4j
public class SpringTaskJob {

    @Value("${push.plus.token}")
    String pushPlusToken;


    @Scheduled(cron = "0 0 8 * * ?")
    public void task() {
        try {
            String result = runProcess("/root/cloud189/cloudpan189-go sign");
            push(result);
        } catch (Exception e) {
            log.error("签到失败!");
        }
    }

    private void push(String content) {
        try {
            String url = String.format("http://www.pushplus.plus/send?token=%s&title=%s&content=%s&template=json", pushPlusToken, "签到", URLEncoder.encode(content, "utf-8"));
            Request.Get(url).execute().returnContent().toString();
        } catch (Exception e) {
            log.error("请求推送失败！", e);
        }
    }

    public static String runProcess(String command) throws IOException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        Process process = Runtime.getRuntime().exec(command);
        SequenceInputStream input = new SequenceInputStream(process.getInputStream(), process.getErrorStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String msg;
        while ((msg = reader.readLine()) != null) {
            builder.append(msg);
            builder.append(System.lineSeparator());
        }
        process.waitFor();
        process.destroy();
        if (builder.length() == 0) {
            return "";
        } else {
            return builder.substring(0, builder.length() - System.lineSeparator().length());
        }
    }
}