package com.ck.jd.control.service;

import com.ck.jd.control.vo.CkDTO;
import com.ck.jd.control.vo.CkVO;

import java.io.IOException;
import java.util.List;

public interface IndexService {
    List<CkVO> getCk() throws IOException;

    List<CkVO> getCkNoToken() throws IOException;

    void ckPut(CkDTO ckDTO) throws IOException;

    void subscribe();

    void execPush();
}
