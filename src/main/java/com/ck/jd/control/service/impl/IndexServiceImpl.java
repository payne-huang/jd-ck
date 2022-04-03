package com.ck.jd.control.service.impl;

import com.alibaba.fastjson.JSON;
import com.ck.jd.control.service.IndexService;
import com.ck.jd.control.vo.CkDTO;
import com.ck.jd.control.vo.CkVO;
import com.ck.jd.control.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class IndexServiceImpl implements IndexService {

    @Value("${client.host}")
    String clientHost;

    @Value("${client.id}")
    String clientId;

    @Value("${client.secret}")
    String clientSecret;

    @Override
    public List<CkVO> getCk() throws IOException {
        String body = Request.Get(clientHost + "/envs")
                .addHeader("Authorization", "Bearer " + getToken()).execute().returnContent().toString();
        List<CkVO> ckVOS = JSON.parseArray(JSON.parseObject(body, ResultVO.class).getData(), CkVO.class);
        ckVOS.forEach(ckVO -> {
            String ptPin = ckVO.getValue().split("pt_pin")[1];
            ckVO.setValue("pt_pin" + ptPin);
        });
        return ckVOS;
    }


    public String getToken() {
        try {
            String body = Request.Get(clientHost + "/auth/token?client_id=" + clientId + "&client_secret=" + clientSecret)
                    .execute().returnContent().toString();
            return JSON.parseObject(JSON.parseObject(body, ResultVO.class).getData()).getString("token");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void ckPut(CkDTO ckDTO) throws IOException {

        String[] kvs = ckDTO.getPin().split(";");
        String ptKey = null;
        String ptPin = null;
        for (String kv : kvs) {
            if (kv.startsWith("pt_key=")) {
                ptKey = kv;
            }
            if (kv.trim().startsWith("pt_pin=")) {
                ptPin = kv;
            }
        }
        if (ptKey != null && ptPin != null) {
            List<CkVO> ckVOS = getCk();
            CkVO ckVO = getCkByPin(ptPin, ckVOS);
            String url = "/envs";
            Map<String, String> param = new HashMap<>();
            String ck = ptKey + ";" + ptPin + ";";
            param.put("name", "JD_COOKIE");
            param.put("value", ck);
            param.put("remarks", ckVO.getRemarks());
            if (ckVO != null) {
                param.put("_id", ckVO.get_id());
                if (!StringUtils.isEmpty(ckDTO.getComment())) {
                    param.put("remarks", ckDTO.getComment());
                }
                Request.Put(clientHost + url)
                        .addHeader("Authorization", "Bearer " + getToken())
                        .bodyString(JSON.toJSONString(param), ContentType.APPLICATION_JSON).execute().returnContent().toString();
                List<String> ids = new ArrayList<>();
                ids.add(ckVO.get_id());
                Request.Put(clientHost + "/envs/enable")
                        .addHeader("Authorization", "Bearer " + getToken())
                        .bodyString(JSON.toJSONString(ids), ContentType.APPLICATION_JSON).execute().returnContent().toString();
            } else {
                Request.Post(clientHost + url)
                        .addHeader("Authorization", "Bearer " + getToken())
                        .bodyString(JSON.toJSONString(param), ContentType.APPLICATION_JSON).execute().returnContent().toString();
            }
        }
    }

    private CkVO getCkByPin(String ptPin, List<CkVO> ckVOS) {
        for (CkVO ckVO : ckVOS) {
            if (ckVO.getValue().contains(ptPin.trim())) {
                return ckVO;
            }
        }
        return null;
    }

}
