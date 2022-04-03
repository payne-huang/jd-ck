package com.ck.jd.common.controller;

import com.ck.jd.common.bean.Response;

public abstract class BaseController {

    /**
     * 成功返回
     *
     * @param content
     * @return
     * @author dongdong.zhang
     * @date 2015年11月4日 下午5:20:14
     */
    protected Response success(Object content) {
        Response result = new Response();
        result.setContent(content);
        result.setStatus("ok");
        return result;
    }

    /**
     * 成功返回
     *
     * @return
     * @author dongdong.zhang
     * @date 2015年11月4日 下午5:20:14
     */
    protected Response success() {
        Response result = new Response();
        result.setStatus("ok");
        return result;
    }

    /**
     * 失败返回
     *
     * @return
     * @author dongdong.zhang
     * @date 2015年11月4日 下午5:20:20
     */
    protected Response error() {
        Response result = new Response();
        result.setStatus("error");
        return result;
    }

}