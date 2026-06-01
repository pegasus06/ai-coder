package com.ruizhou.aicoder.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ruizhou.aicoder.annotation.AuthCheck;
import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.common.ResultUtils;
import com.ruizhou.aicoder.constant.UserConstant;
import com.ruizhou.aicoder.entity.ChatHistory;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryQueryRequest;
import com.ruizhou.aicoder.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史（管理员）
 */
@RestController
@RequestMapping("/chatHistory/admin")
public class ChatHistoryAdminController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询全部对话历史（默认按创建时间降序）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.userConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listChatHistoryVoByPage(@RequestBody ChatHistoryQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(request);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }

}
