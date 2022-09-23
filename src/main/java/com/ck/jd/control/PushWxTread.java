package com.ck.jd.control;

import com.ck.jd.common.util.SpringContextUtil;
import com.ck.jd.control.service.IndexService;

public class PushWxTread extends Thread {

    private IndexService indexService;

    public PushWxTread() {
        indexService = (IndexService) SpringContextUtil.getBean("indexService");
    }

    @Override
    public void run() {
        indexService.pushWx();
    }
}
