package com.ruizhou.aicoder.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史游标分页结果
 */
@Data
public class ChatHistoryCursorVO implements Serializable {

    /**
     * 消息列表（按时间升序）
     */
    private List<ChatHistoryVO> records;

    /**
     * 是否还有更早的历史消息
     */
    private Boolean hasMore;

    /**
     * 下一页游标：当前批次最早消息的创建时间
     */
    private LocalDateTime nextCursor;

    private static final long serialVersionUID = 1L;
}
