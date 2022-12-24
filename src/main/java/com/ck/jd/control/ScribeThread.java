package com.ck.jd.control;

import com.ck.jd.common.util.SpringContextUtil;
import com.ck.jd.control.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@DependsOn("springContextUtil")
@Slf4j
public class ScribeThread {

    private IndexService indexService;

    public ScribeThread() {
        indexService = (IndexService) SpringContextUtil.getBean("indexService");
    }

    @Scheduled(cron ="0 0/2 * * * ?")
    public void subscribe(){
        try {
            indexService.subscribe();
        } catch (Exception e){
            log.error("订阅失败！");
        }
    }


    @Scheduled(cron ="0 0 0/2 * * ?")
    public void task(){
        try {
            indexService.execPush();
        } catch (Exception e){
            log.error("检测推送失败！");
        }
    }
}
