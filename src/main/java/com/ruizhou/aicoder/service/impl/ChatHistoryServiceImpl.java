package com.ruizhou.aicoder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruizhou.aicoder.constant.ChatHistoryConstant;
import com.ruizhou.aicoder.constant.UserConstant;
import com.ruizhou.aicoder.entity.App;
import com.ruizhou.aicoder.entity.ChatHistory;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.exception.BusinessException;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.mapper.ChatHistoryMapper;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAppQueryRequest;
import com.ruizhou.aicoder.model.enums.MessageTypeEnum;
import com.ruizhou.aicoder.model.vo.ChatHistoryCursorVO;
import com.ruizhou.aicoder.model.vo.ChatHistoryVO;
import com.ruizhou.aicoder.model.vo.UserVO;
import com.ruizhou.aicoder.service.AppService;
import com.ruizhou.aicoder.service.ChatHistoryService;
import com.ruizhou.aicoder.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Resource
    private UserService userService;

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
        if (appId == null || appId <= 0) {
            return false;
        }
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
    public Page<ChatHistoryVO> listChatHistoryVoByPageForAdmin(ChatHistoryAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = request.getPageNum();
        int pageSize = Math.max(request.getPageSize(), 1);
        QueryWrapper qw = getAdminQueryWrapper(request);
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("createTime", false);
        }
        Page<ChatHistory> page = this.page(Page.of(pageNum, pageSize), qw);
        Page<ChatHistoryVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        voPage.setRecords(getChatHistoryVoListForAdmin(page.getRecords()));
        return voPage;
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(ChatHistoryAdminQueryRequest request) {
        Long id = request.getId();
        Long appId = request.getAppId();
        Long userId = request.getUserId();
        String messageType = request.getMessageType();
        String message = request.getMessage();
        QueryWrapper qw = QueryWrapper.create();
        if (id != null) {
            qw.eq("id", id);
        }
        if (appId != null) {
            qw.eq("appId", appId);
        }
        if (userId != null) {
            qw.eq("userId", userId);
        }
        if (StrUtil.isNotBlank(messageType)) {
            qw.eq("messageType", messageType);
        }
        if (StrUtil.isNotBlank(message)) {
            qw.like("message", message);
        }
        return qw;
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
    public List<ChatHistoryVO> getChatHistoryVoListForAdmin(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        Set<Long> appIds = chatHistoryList.stream().map(ChatHistory::getAppId).collect(Collectors.toSet());
        Set<Long> userIds = chatHistoryList.stream().map(ChatHistory::getUserId).collect(Collectors.toSet());
        Map<Long, App> appMap = appService.listByIds(appIds).stream()
                .collect(Collectors.toMap(App::getId, app -> app));
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return chatHistoryList.stream().map(chatHistory -> {
            ChatHistoryVO vo = getChatHistoryVO(chatHistory);
            App app = appMap.get(chatHistory.getAppId());
            if (app != null) {
                vo.setAppName(app.getAppName());
            }
            vo.setUser(userVOMap.get(chatHistory.getUserId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
