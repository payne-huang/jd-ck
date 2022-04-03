package com.ck.jd.common.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class Response implements Serializable {
    /**
     * 信息内容
     */
    private Object content;
    /**
     * 状态
     */
    private String status;


    /**
     * 成功返回
     *
     * @param content
     * @return
     */
    public static Response success(Object content) {
        Response result = new Response();
        result.setContent(content);
        result.setStatus("ok");
        return result;
    }

    /**
     * 失败返回
     */
    public static Response error() {
        Response result = new Response();
        result.setStatus("error");
        return result;
    }
}