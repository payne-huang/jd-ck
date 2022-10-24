package com.ck.jd.control;

import com.ck.jd.common.util.SpringContextUtil;
import com.ck.jd.control.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScribeThread {

    private IndexService indexService;

    public ScribeThread() {
        indexService = (IndexService) SpringContextUtil.getBean("indexService");
    }

    @Scheduled(cron ="0 0/2 * * * ?")
    public void subscribe(){
        indexService.subscribe();
    }


    @Scheduled(cron ="0 0 0/2 * * ?")
    public void task(){
        indexService.execPush();
    }
}
