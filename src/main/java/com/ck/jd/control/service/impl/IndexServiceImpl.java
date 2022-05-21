package com.ck.jd.control.service.impl;

import com.alibaba.fastjson.JSON;
import com.ck.jd.control.service.IndexService;
import com.ck.jd.control.vo.CkDTO;
import com.ck.jd.control.vo.CkVO;
import com.ck.jd.control.vo.Monitor;
import com.ck.jd.control.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class IndexServiceImpl implements IndexService {

    @Value("${client.host}")
    String clientHost;

    @Value("${client.id}")
    String clientId;

    @Value("${client.secret}")
    String clientSecret;

    @Value("${github.token}")
    String gitHubToken;

    @Value("${github.subscribes}")
    String gitSubscribes;

    @Value("${push.plus.token}")
    String pushPlusToken;

    Set<Monitor> monitors = new HashSet<>();

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
            String value = kv.trim();
            if (value.startsWith("pt_key=")) {
                ptKey = value;
            }
            if (value.startsWith("pt_pin=")) {
                ptPin = value;
            }
        }
        if (ptKey != null && ptPin != null) {
            List<CkVO> ckVOS = getCk();
            CkVO ckVO = getCkByPin(ptPin, ckVOS);
            String url = "/envs";
            Map<String, String> param = new HashMap<>();
            String ck = ptKey + "; " + ptPin + ";";
            param.put("name", "JD_COOKIE");
            param.put("value", ck);
            if (!StringUtils.isEmpty(ckDTO.getComment())) {
                param.put("remarks", ckDTO.getComment());
            }
            if (ckVO != null) {
                param.put("_id", ckVO.get_id());
                if (StringUtils.isEmpty(param.get("remarks"))) {
                    param.put("remarks", ckVO.getRemarks());
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
                List<Map<String, String>> list = new ArrayList<>();
                list.add(param);
                Request.Post(clientHost + url)
                        .addHeader("Authorization", "Bearer " + getToken())
                        .bodyString(JSON.toJSONString(list), ContentType.APPLICATION_JSON).execute().returnContent().toString();
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

    @Scheduled(cron = "*/5  * * * *")
    private void subscribe() {
        if (monitors.size() == 0){
            initSubscribes();
        }
        try {
            for (Monitor monitor : monitors) {
                String newSha1 = monitor.getGhBranch().getSHA1();
                if (StringUtils.isNotBlank(newSha1) && StringUtils.equalsIgnoreCase(newSha1, monitor.getCommitId())) {
                    log.info("触发同步操作---");
                    monitor.setCommitId(newSha1);
                    callTaskRun(monitor.getId());
                    GHCommit ghCommit = monitor.getGhRepository().getCommit(newSha1);
                    String message = ghCommit.getCommitShortInfo().getMessage();
                    if (StringUtils.isNotBlank(pushPlusToken)) {
                        callPushWx(monitor.getGhRepository().getFullName(), message);
                    }
                }
            }
        } catch (Exception e) {
            log.error("订阅轮询失败");
        }
    }

    private void callPushWx(String name, String message) {
        try {
            Request.Get(String.format("http://www.pushplus.plus/send?token=%s&title=%s&content=%s&template=json", pushPlusToken, name + " 更新", message)).execute().returnContent().toString();
        } catch (IOException e) {
            log.error("请求推送失败！", e);
        }
    }

    private void callTaskRun(String id) {
        try {
            List<String> list = new ArrayList<>();
            list.add(id);
            Request.Put(clientHost + "/crons/run")
                    .addHeader("Authorization", "Bearer " + getToken())
                    .bodyString(JSON.toJSONString(list), ContentType.APPLICATION_JSON).execute().returnContent().toString();
        } catch (Exception e){
            log.error("触发任务失败", e);
        }
    }

    @Override
    public void initSubscribes() {
        try {
            GitHub gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();
            String[] subscribes = gitSubscribes.trim().split(";");
            for (String subscribe : subscribes) {
                String[] data = subscribe.split("@");
                if (data.length == 3) {
                    Monitor monitor = new Monitor();
                    GHRepository repository = gitHub.getRepository(data[0]);
                    GHBranch ghBranch = repository.getBranch(data[1]);
                    String sha1 = ghBranch.getSHA1();
                    monitor.setCommitId(sha1);
                    monitor.setGhBranch(ghBranch);
                    monitor.setId(data[2]);
                    monitors.add(monitor);
                }
            }
            log.info("订阅器初始化成功！");
        } catch (Exception e) {
            log.error("初始化订阅器失败，请重启服务！", e);
        }
    }
}
