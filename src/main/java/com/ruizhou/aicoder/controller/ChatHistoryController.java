package com.ruizhou.aicoder.controller;

import com.mybatisflex.core.paginate.Page;
import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.common.ResultUtils;
import com.ruizhou.aicoder.entity.ChatHistory;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.model.dto.chathistory.ChatHistoryAppQueryRequest;
import com.ruizhou.aicoder.model.vo.ChatHistoryCursorVO;
import com.ruizhou.aicoder.service.ChatHistoryService;
import com.ruizhou.aicoder.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

}
