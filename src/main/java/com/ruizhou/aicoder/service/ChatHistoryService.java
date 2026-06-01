package com.ruizhou.aicoder.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ruizhou.aicoder.entity.ChatHistory;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAppQueryRequest;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryQueryRequest;
import com.ruizhou.aicoder.model.vo.ChatHistoryCursorVO;
import com.ruizhou.aicoder.model.vo.ChatHistoryVO;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息
     */
    Long saveUserMessage(Long appId, Long userId, String message);

    /**
     * 保存 AI 消息
     */
    Long saveAiMessage(Long appId, Long userId, String message);

    /**
     * 保存错误消息
     */
    Long saveErrorMessage(Long appId, Long userId, String errorMessage);

    /**
     * 删除指定应用的全部对话历史
     */
    boolean deleteByAppId(Long appId);

    /**
     * 游标分页查询应用对话历史（应用创建者和管理员可见）
     */
    ChatHistoryCursorVO listAppChatHistory(ChatHistoryAppQueryRequest request, User loginUser);

    /**
     * 管理员分页查询全部对话历史
     */

    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    List<ChatHistoryVO> getChatHistoryVoList(List<ChatHistory> chatHistoryList);


    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    Flux<String> chatToGen(Long appId, String message, User loginUser);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
