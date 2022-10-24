package com.ck.jd.control.controller;


import com.ck.jd.common.bean.Response;
import com.ck.jd.common.controller.BaseController;
import com.ck.jd.control.service.IndexService;
import com.ck.jd.control.vo.CkDTO;
import com.ck.jd.control.vo.CkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class IndexController extends BaseController {

    @Autowired
    IndexService indexService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @PostMapping("/ck/put")
    @ResponseBody
    public Response ckPut(@RequestBody CkDTO ckDTO) throws IOException {
        try {
            indexService.ckPut(ckDTO);
        } catch (Exception e){
            return error();
        }
        return success();
    }

    @GetMapping("/ck/list")
    @ResponseBody
    public Response getCk() throws IOException {
        List<CkVO> ckVOList = indexService.getCkNoToken();
        return success(ckVOList);
    }

    @GetMapping("test")
    public Response test(){
        indexService.execPush();
        return success();
    }
}
