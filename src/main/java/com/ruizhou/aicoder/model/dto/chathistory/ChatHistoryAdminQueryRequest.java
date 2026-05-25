package com.ruizhou.aicoder.model.dto.chathistory;

import com.ruizhou.aicoder.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员分页查询对话历史
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryAdminQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private Long appId;

    private Long userId;

    private String messageType;

    private String message;

    private static final long serialVersionUID = 1L;
}
