package com.ck.jd.control.service.impl;

import com.alibaba.fastjson.JSON;
import com.ck.jd.control.service.IndexService;
import com.ck.jd.control.vo.CkDTO;
import com.ck.jd.control.vo.CkVO;
import com.ck.jd.control.vo.MessageVO;
import com.ck.jd.control.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(value = "indexService")
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

    @Value("${scribe.inter}")
    Integer inter;

    volatile Map<String, String> cache = new HashMap<>();


    @Override
    public List<CkVO> getCk() throws IOException {
        String body = Request.Get(clientHost + "/envs")
                .addHeader("Authorization", "Bearer " + getToken()).execute().returnContent().toString();
        List<CkVO> ckVOS = JSON.parseArray(JSON.parseObject(body, ResultVO.class).getData(), CkVO.class);
        AtomicInteger index = new AtomicInteger(1);
        ckVOS.forEach(ckVO -> {
            String ptPin = ckVO.getValue().split("pt_pin")[1];
            ckVO.setValue("pt_pin" + ptPin);
            ckVO.setIndex(index.get());
            index.getAndIncrement();
        });
        return ckVOS;
    }

    @Override
    public List<CkVO> getCkNoToken() throws IOException {
        List<CkVO> ckVOS = getCk();
        for (CkVO ckVO : ckVOS) {
            if (StringUtils.isNotBlank(ckVO.getRemarks())){
                if (ckVO.getRemarks().contains(":$")){
                    String noTokenRemark = ckVO.getRemarks().replaceAll(":\\$.*", "");
                    ckVO.setRemarks(noTokenRemark + "-通知");
                } else {
                    ckVO.setRemarks(ckVO.getRemarks());
                }

            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String format = df.format(new Date(ckVO.getTimestamp()));
            ckVO.setTimestamp(format);
            ckVO.setValue(null);
        }
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

        String ptKey = getByReg(ckDTO.getPin(), "(pt_key=[A-Za-z0-9_-]+);");
        String ptPin = getByReg(ckDTO.getPin(), "(pt_pin=[%A-Za-z0-9_-]+);");

        if (ptKey != null && ptPin != null) {
            List<CkVO> ckVOS = getCk();
            CkVO ckVO = getCkByPin(ptPin, ckVOS);
            String url = "/envs";
            Map<String, String> param = new HashMap<>();
            String ck = ptKey + "; " + ptPin + ";";
            param.put("name", "JD_COOKIE");
            param.put("value", ck);
            if (ckVO != null) {
                param.put("_id", ckVO.get_id());
                if (StringUtils.isNotBlank(ckDTO.getComment())){
                    if (StringUtils.isNotBlank(ckDTO.getToken())){
                        param.put("remarks", ckDTO.getComment() + ":$" + ckDTO.getToken());
                    } else {
                        if (StringUtils.isNotBlank(getMatchToken(ckVO.getRemarks()))){
                            param.put("remarks", ckDTO.getComment() + ":$" + getMatchToken(ckVO.getRemarks()));
                        } else {
                            param.put("remarks", ckDTO.getComment());
                        }
                    }
                } else {
                    String remarks = ckVO.getRemarks();
                    String comment = remarks.replace(":$" + getMatchToken(remarks), "");
                    if (StringUtils.isNotBlank(ckDTO.getToken())){
                        param.put("remarks", comment + ":$" + ckDTO.getToken());
                    } else {
                        if (StringUtils.isNotBlank(getMatchToken(ckVO.getRemarks()))){
                            param.put("remarks", comment + ":$" + getMatchToken(ckVO.getRemarks()));
                        } else {
                            param.put("remarks", comment);
                        }
                    }
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
                if (StringUtils.isNotBlank(ckDTO.getComment()) ){
                    if (StringUtils.isNotBlank(ckDTO.getToken())){
                        param.put("remarks", ckDTO.getComment() + ":$" + ckDTO.getToken());
                    } else {
                        param.put("remarks", ckDTO.getComment());
                    }
                } else {
                    if (StringUtils.isNotBlank(ckDTO.getToken())){
                        param.put("remarks", ":$" + ckDTO.getToken());
                    }
                }

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

    @Override
    public void subscribe() {
        while (true) {
            exec();
            try {
                Thread.sleep(1000 * 60 * inter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pushWx() {
        while (true) {
            execPush();
            try {
                Thread.sleep(1000 * 60 * 60 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void execPush() {
        try {
            List<CkVO> ckVOS = getCk();
            for (CkVO ckVO : ckVOS) {
                String remarks = ckVO.getRemarks();
                long useTime = System.currentTimeMillis() - new Date(ckVO.getTimestamp()).getTime();
                long expire = 30 - useTime / (1000 * 60 * 60 * 24);
                if (expire <= 3 && StringUtils.isNotBlank(remarks) && StringUtils.isNotBlank(getMatchToken(remarks))) {
                    log.info("通知用户={}", remarks);
                    String token = getMatchToken(remarks);
                    push(token, expire);
                }
            }
        } catch (Exception e) {
            log.error("推送失败", e);
        }
    }

    private void push(String token, long time) {
        try {
            String content = URLEncoder.encode("CK还剩下" + time + "天，速更新！");
            if (time < 0){
                content = URLEncoder.encode("CK已过期请更新，速更新！");
            }
            String url = String.format("http://www.pushplus.plus/send?token=%s&title=%s&content=%s&template=json", token, "CK更新通知", content);
            Request.Get(url).execute().returnContent().toString();
        } catch (Exception e) {
            log.error("请求推送失败！", e);
        }
    }

    private String getMatchToken(String s) {
        Pattern pattern = Pattern.compile(":\\$(.*)");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getByReg(String s, String reg) {
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void exec() {
        List<MessageVO> messageVOS = new ArrayList<>();

        GitHub gitHub = null;
        try {
            gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();
        } catch (IOException e) {
            log.error("初始化账号失败！={}", e.getMessage());
        }
        String[] subscribes = gitSubscribes.trim().split(";");
        for (String subscribe : subscribes) {
            try {
                String[] data = subscribe.split("@");
                GHRepository ghRepository = gitHub.getRepository(data[0]);
                String fullName = ghRepository.getFullName();
                GHBranch ghBranch = ghRepository.getBranch(data[1]);
                String newSha1 = ghBranch.getSHA1();
                if (StringUtils.isNotBlank(newSha1) && !StringUtils.equalsIgnoreCase(newSha1, cache.get(fullName))) {
                    cache.put(fullName, newSha1);
                    callTaskRun(data[2]);
                    if (StringUtils.isNotBlank(pushPlusToken)) {
                        MessageVO messageVO = new MessageVO();
                        String message = ghRepository.getCommit(newSha1).getCommitShortInfo().getMessage();
                        messageVO.setFullName(fullName);
                        messageVO.setMessage(message);
                        messageVOS.add(messageVO);
                    }
                }
            } catch (Exception e) {
                log.error("仓库【{}】订阅失败！={}", subscribe, e.getMessage());
            }
        }

        if (messageVOS.size() > 0) {
            callPushWx("仓库更新", messageVOS);
        }
    }

    private void callPushWx(String title, List<MessageVO> messageVOS) {
        try {
            String content = URLEncoder.encode(JSON.toJSONString(messageVOS));
            String url = String.format("http://www.pushplus.plus/send?token=%s&title=%s&content=%s&template=json", pushPlusToken, title, content);
            Request.Get(url).execute().returnContent().toString();
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("触发任务失败", e);
        }
    }
}
