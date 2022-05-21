package com.ck.jd.control.vo;

import lombok.Data;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;

@Data
public class Monitor {
    GHBranch ghBranch;
    GHRepository ghRepository;
    String commitId;
    String id;
}
