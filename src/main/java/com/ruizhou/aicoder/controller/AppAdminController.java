package com.ruizhou.aicoder.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ruizhou.aicoder.annotation.AuthCheck;
import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.common.DeleteRequest;
import com.ruizhou.aicoder.common.ResultUtils;
import com.ruizhou.aicoder.constant.UserConstant;
import com.ruizhou.aicoder.entity.App;
import com.ruizhou.aicoder.exception.BusinessException;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.model.dto.app.AppAdminQueryRequest;
import com.ruizhou.aicoder.model.dto.app.AppAdminUpdateRequest;
import com.ruizhou.aicoder.model.vo.AppVO;
import com.ruizhou.aicoder.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用（管理员）
 */
@RestController
@RequestMapping("/app/admin")
public class AppAdminController {

    @Resource
    private AppService appService;

    /**
     * 根据 id 删除任意应用
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.userConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        long id = deleteRequest.getId();
        App oldId = appService.getById(id);
        ThrowUtils.throwIf(oldId == null, ErrorCode.NOT_FOUND_ERROR);
        boolean ok = appService.deleteAppByAdmin(id);
        return ResultUtils.success(ok);
    }

    /**
     * 根据 id 更新任意应用（名称、封面、优先级）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.userConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppAdminUpdateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long id = request.getId();
        App oldId = appService.getById(id);
        ThrowUtils.throwIf(oldId == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtils.copyProperties(request, app);
        app.setEditTime(LocalDateTime.now());
        boolean ok = appService.updateAppByAdmin(request);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(ok);
    }

    /**
     * 分页查询应用列表（条件不含时间字段；分页大小不限）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.userConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVoByPage(@RequestBody AppAdminQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = queryRequest.getPageNum();
        int pageSize = queryRequest.getPageSize();
        QueryWrapper adminQueryWrapper = appService.getAdminQueryWrapper(queryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), adminQueryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVoList = appService.getAppVoList(appPage.getRecords());
        appVOPage.setRecords(appVoList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 根据 id 查看应用详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.userConstant.ADMIN_ROLE)
    public BaseResponse<App> getApp(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getAppByAdmin(id);
        return ResultUtils.success(app);
    }


}
