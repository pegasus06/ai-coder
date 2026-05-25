package com.ruizhou.aicoder.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用对话历史游标查询请求
 */
@Data
public class ChatHistoryAppQueryRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 每次加载条数，默认 10
     */
    private Integer pageSize = 10;

    /**
     * 游标：上一批最早消息的创建时间，首次加载不传
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 1L;
}
