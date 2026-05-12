package com.ruizhou.aicoder.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新自己的应用（仅名称）
 */
@Data
public class AppUserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
