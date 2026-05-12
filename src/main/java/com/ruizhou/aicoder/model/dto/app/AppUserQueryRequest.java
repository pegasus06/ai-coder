package com.ruizhou.aicoder.model.dto.app;

import com.ruizhou.aicoder.common.PageRequest;
import com.ruizhou.aicoder.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户分页查询自己的应用
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppUserQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用名称（模糊）
     */
    private String appName;
    private Long userId;

    private static final long serialVersionUID = 1L;
}
