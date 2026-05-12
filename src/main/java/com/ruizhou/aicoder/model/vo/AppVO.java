package com.ruizhou.aicoder.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AppVO implements Serializable {

    private Long id;

    private String appName;

    private String cover;

    /**
     * 详情或本人场景返回；精选列表等场景可为空
     */
    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private LocalDateTime deployedTime;

    private Integer priority;

    private Long userId;

    private LocalDateTime createTime;
    private UserVO user;


    private static final long serialVersionUID = 1L;
}
