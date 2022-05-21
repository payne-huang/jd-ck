package com.ck.jd.control;

import com.ck.jd.common.util.SpringContextUtil;
import com.ck.jd.control.service.IndexService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScribeThread extends Thread {

    private IndexService indexService;

    public ScribeThread() {
        indexService = (IndexService) SpringContextUtil.getBean("indexService");
    }

    @Override
    public void run() {
        indexService.initSubscribes();
        indexService.subscribe();
    }
}
