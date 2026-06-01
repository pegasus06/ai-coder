package com.ruizhou.aicoder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruizhou.aicoder.ai.core.AiCodeGeneratorFacade;
import com.ruizhou.aicoder.ai.model.enums.CodeGenTypeEnum;
import com.ruizhou.aicoder.constant.ChatHistoryConstant;
import com.ruizhou.aicoder.constant.UserConstant;
import com.ruizhou.aicoder.entity.App;
import com.ruizhou.aicoder.entity.ChatHistory;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.exception.BusinessException;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.mapper.ChatHistoryMapper;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAppQueryRequest;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryQueryRequest;
import com.ruizhou.aicoder.model.enums.MessageTypeEnum;
import com.ruizhou.aicoder.model.vo.ChatHistoryCursorVO;
import com.ruizhou.aicoder.model.vo.ChatHistoryVO;
import com.ruizhou.aicoder.service.AppService;
import com.ruizhou.aicoder.service.ChatHistoryService;
import com.ruizhou.aicoder.service.UserService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Autowired
    @Lazy
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public Long saveUserMessage(Long appId, Long userId, String message) {
        return saveMessage(appId, userId, message, MessageTypeEnum.USER);
    }

    @Override
    public Long saveAiMessage(Long appId, Long userId, String message) {
        return saveMessage(appId, userId, message, MessageTypeEnum.AI);
    }

    @Override
    public Long saveErrorMessage(Long appId, Long userId, String errorMessage) {
        String message = StrUtil.isBlank(errorMessage) ? "AI 回复失败" : errorMessage;
        return saveMessage(appId, userId, message, MessageTypeEnum.ERROR);
    }

    private Long saveMessage(Long appId, Long userId, String message, MessageTypeEnum messageType) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ChatHistory chatHistory = ChatHistory.builder()
                .message(message)
                .messageType(messageType.getValue())
                .appId(appId)
                .userId(userId)
                .build();
        boolean saved = this.save(chatHistory);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "保存对话历史失败");
        return chatHistory.getId();
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        QueryWrapper qw = QueryWrapper.create().eq("appId", appId);
        return this.remove(qw);
    }

    @Override
    public ChatHistoryCursorVO listAppChatHistory(ChatHistoryAppQueryRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long appId = request.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.userConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        if (!app.getUserId().equals(loginUser.getId()) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        int pageSize = request.getPageSize() == null ? ChatHistoryConstant.DEFAULT_PAGE_SIZE : request.getPageSize();
        pageSize = Math.min(Math.max(pageSize, 1), ChatHistoryConstant.MAX_PAGE_SIZE);

        QueryWrapper qw = QueryWrapper.create().eq("appId", appId);
        if (request.getLastCreateTime() != null) {
            qw.lt("createTime", request.getLastCreateTime());
        }
        qw.orderBy("createTime", false).limit(pageSize + 1);

        List<ChatHistory> chatHistoryList = this.list(qw);
        boolean hasMore = chatHistoryList.size() > pageSize;
        if (hasMore) {
            chatHistoryList = new ArrayList<>(chatHistoryList.subList(0, pageSize));
        }
        Collections.reverse(chatHistoryList);

        ChatHistoryCursorVO cursorVO = new ChatHistoryCursorVO();
        cursorVO.setRecords(getChatHistoryVoList(chatHistoryList));
        cursorVO.setHasMore(hasMore);
        if (CollUtil.isNotEmpty(chatHistoryList)) {
            cursorVO.setNextCursor(chatHistoryList.get(0).getCreateTime());
        }
        return cursorVO;
    }


    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO vo = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, vo);
        return vo;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVoList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        return chatHistoryList.stream().map(this::getChatHistoryVO).collect(Collectors.toList());
    }

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public Flux<String> chatToGen(Long appId, String message, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        if (codeGenType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型不能为空");
        }
        // 5. 通过校验后，添加用户消息到对话历史
        this.addChatMessage(appId, message, MessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6. 调用 AI 生成代码（流式）
        Flux<String> content = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenType, appId);
        StringBuilder aiResponseBuilder = new StringBuilder();
        return content.map(chunk -> {
                    aiResponseBuilder.append(chunk);
                    return chunk;
                }
        ).doOnComplete(
                () -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponse)) {
                        this.addChatMessage(appId, aiResponse, MessageTypeEnum.AI.getValue(), loginUser.getId());
                    }
                }
        ).doOnError(
                error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    this.addChatMessage(appId, errorMessage, MessageTypeEnum.AI.getValue(), loginUser.getId());
                }
        );
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        queryWrapper.eq("id", id).like("message", message).eq("messageType", messageType)
                .eq("appid", appId).eq("userid", userId);
        if (lastCreateTime != null) {
            queryWrapper.lt("createtime", lastCreateTime);
        }
        if (StrUtil.isNotBlank(sortField) && StrUtil.isNotBlank(sortOrder)) {
            queryWrapper.orderBy(sortField, "asc".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createtime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.userConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            Collections.reverse(historyList);
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }


}

