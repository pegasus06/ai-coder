package com.ruizhou.aicoder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruizhou.aicoder.ai.model.enums.CodeGenTypeEnum;
import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.common.ResultUtils;
import com.ruizhou.aicoder.constant.AppConstant;
import com.ruizhou.aicoder.entity.App;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.exception.BusinessException;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.exception.ThrowUtils;
import com.ruizhou.aicoder.mapper.AppMapper;
import com.ruizhou.aicoder.model.dto.app.*;
import com.ruizhou.aicoder.model.vo.AppVO;
import com.ruizhou.aicoder.model.vo.UserVO;
import com.ruizhou.aicoder.service.AppService;
import com.ruizhou.aicoder.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/liyupi">ruizhou</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private static final String DEFAULT_APP_NAME = "未命名应用";
    @Resource
    private UserService userService;
    @Resource
    private AppService appService;

    @Override
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null || StrUtil.isBlank(appAddRequest.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        String initPrompt = appAddRequest.getInitPrompt();
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setId(loginUser.getId());
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 暂时设置为多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        boolean save = appService.save(app);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建失败");
        }
        return ResultUtils.success(app.getId());
    }

    @Override
    public BaseResponse<Boolean> updateMyApp(AppUserUpdateRequest req, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (req == null || req.getId() == null || req.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(req.getAppName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }
        App old = this.getById(req.getId());

        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!old.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App patch = new App();
        patch.setId(req.getId());
        patch.setAppName(req.getAppName());
        patch.setEditTime(LocalDateTime.now());
        boolean updateById = appService.updateById(patch);
        ThrowUtils.throwIf(!updateById, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @Override
    public AppVO getMyAppVo(long id, User loginUser) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return getAppVO(app, true);
    }

    @Override
    public Page<AppVO> listMyAppVoByPage(AppUserQueryRequest queryRequest, User loginUser) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long pageNum = queryRequest.getPageNum();
        int pageSize = Math.min(Math.max(queryRequest.getPageSize(), 1), AppConstant.MAX_PAGE_SIZE_USER);
        QueryWrapper qw = QueryWrapper.create()
                .eq("userId", loginUser.getId());
        if (StrUtil.isNotBlank(queryRequest.getAppName())) {
            qw.like("appName", queryRequest.getAppName());
        }
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("createTime", false);
        }
        Page<App> page = this.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        voPage.setRecords(getAppVoList(page.getRecords()));
        return voPage;
    }

    @Override
    public Page<AppVO> listFeaturedAppVoByPage(AppFeaturedQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long pageNum = queryRequest.getPageNum();
        int pageSize = Math.min(Math.max(queryRequest.getPageSize(), 1), AppConstant.MAX_PAGE_SIZE_USER);
        QueryWrapper qw = QueryWrapper.create();
        if (StrUtil.isNotBlank(queryRequest.getAppName())) {
            qw.like("appName", queryRequest.getAppName());
        }
        qw.orderBy("priority", false).orderBy("createTime", false);
        Page<App> page = this.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        voPage.setRecords(getAppVoList(page.getRecords()));
        return voPage;
    }

    @Override
    public boolean deleteAppByAdmin(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public boolean updateAppByAdmin(AppAdminUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App old = this.getById(request.getId());
        if (old == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        App patch = new App();
        patch.setId(request.getId());
        if (StrUtil.isNotBlank(request.getAppName())) {
            patch.setAppName(request.getAppName());
        }
        if (request.getCover() != null) {
            patch.setCover(request.getCover());
        }
        if (request.getPriority() != null) {
            patch.setPriority(request.getPriority());
        }
        patch.setEditTime(LocalDateTime.now());
        boolean ok = this.updateById(patch);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    public Page<AppVO> listAppVoByPageForAdmin(AppAdminQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long pageNum = queryRequest.getPageNum();
        int pageSize = Math.max(queryRequest.getPageSize(), 1);
        QueryWrapper qw = getAdminQueryWrapper(queryRequest);
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("createTime", false);
        }
        Page<App> page = this.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        voPage.setRecords(getAppVoList(page.getRecords()));
        return voPage;
    }

    @Override
    public App getAppByAdmin(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return app;
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest request) {
        Long id = request.getId();
        String appName = request.getAppName();
        String cover = request.getCover();
        String initPrompt = request.getInitPrompt();
        String codeGenType = request.getCodeGenType();
        String deployKey = request.getDeployKey();
        Integer priority = request.getPriority();
        Long userId = request.getUserId();
        QueryWrapper qw = QueryWrapper.create();
        if (id != null) {
            qw.eq("id", id);
        }
        if (StrUtil.isNotBlank(appName)) {
            qw.like("appName", appName);
        }
        if (StrUtil.isNotBlank(cover)) {
            qw.like("cover", cover);
        }
        if (StrUtil.isNotBlank(initPrompt)) {
            qw.like("initPrompt", initPrompt);
        }
        if (StrUtil.isNotBlank(codeGenType)) {
            qw.eq("codeGenType", codeGenType);
        }
        if (StrUtil.isNotBlank(deployKey)) {
            qw.eq("deployKey", deployKey);
        }
        if (priority != null) {
            qw.eq("priority", priority);
        }
        if (userId != null) {
            qw.eq("userId", userId);
        }
        return qw;
    }

    @Override
    public AppVO getAppVO(App app) {
        return getAppVO(app, true);
    }

    @Override
    public AppVO getAppVO(App app, boolean includeInitPrompt) {
        if (app == null) {
            return null;
        }
        AppVO vo = new AppVO();
        BeanUtil.copyProperties(app, vo);
        Long userId = app.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            vo.setUser(userVO);
        }

        return vo;
    }

    @Override
    public List<AppVO> getAppVoList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }
}
