package com.ruizhou.aicoder.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史视图
 */
@Data
public class ChatHistoryVO implements Serializable {

    private Long id;

    private String message;

    private String messageType;

    private Long appId;

    private Long userId;

    private LocalDateTime createTime;

    /**
     * 管理员列表场景返回
     */
    private String appName;

    /**
     * 管理员列表场景返回
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;
}
