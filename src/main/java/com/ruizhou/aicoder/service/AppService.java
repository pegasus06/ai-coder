package com.ruizhou.aicoder.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.ruizhou.aicoder.common.BaseResponse;
import com.ruizhou.aicoder.entity.App;
import com.ruizhou.aicoder.entity.User;
import com.ruizhou.aicoder.model.dto.app.*;
import com.ruizhou.aicoder.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 */
public interface AppService extends IService<App> {

    BaseResponse<Long> addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    BaseResponse<Boolean> updateMyApp(AppUserUpdateRequest appUserUpdateRequest, HttpServletRequest request);


    AppVO getMyAppVo(long id, User loginUser);

    Page<AppVO> listMyAppVoByPage(AppUserQueryRequest queryRequest, User loginUser);

    Page<AppVO> listFeaturedAppVoByPage(AppFeaturedQueryRequest queryRequest);

    boolean deleteApp(long id);

    boolean deleteAppByAdmin(long id);

    boolean updateAppByAdmin(AppAdminUpdateRequest request);

    Page<AppVO> listAppVoByPageForAdmin(AppAdminQueryRequest queryRequest);

    App getAppByAdmin(long id);

    QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest queryRequest);

    AppVO getAppVO(App app);

    AppVO getAppVO(App app, boolean includeInitPrompt);

    List<AppVO> getAppVoList(List<App> appList);

     Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);
}
