package com.ruizhou.aicoder.controller;

import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.common.ResultUtils;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAppQueryRequest;
import com.ruizhou.aicoder.model.vo.ChatHistoryCursorVO;
import com.ruizhou.aicoder.service.ChatHistoryService;
import com.ruizhou.aicoder.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史（用户端）
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 游标分页查询某个应用的对话历史（应用创建者和管理员可见）
     */
    @PostMapping("/app/list")
    public BaseResponse<ChatHistoryCursorVO> listAppChatHistory(@RequestBody ChatHistoryAppQueryRequest request,
                                                                  HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        ChatHistoryCursorVO cursorVO = chatHistoryService.listAppChatHistory(request, loginUser);
        return ResultUtils.success(cursorVO);
    }
}
