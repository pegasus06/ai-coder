package com.ruizhou.aicoder.controller;

import com.mybatisflex.core.paginate.Page;
import com.ruizhou.aicoder.annotation.AuthCheck;
import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.common.ResultUtils;
import com.ruizhou.aicoder.constant.UserConstant;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.ruizhou.aicoder.model.vo.ChatHistoryVO;
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
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryVoByPage(@RequestBody ChatHistoryAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Page<ChatHistoryVO> page = chatHistoryService.listChatHistoryVoByPageForAdmin(request);
        return ResultUtils.success(page);
    }
}
