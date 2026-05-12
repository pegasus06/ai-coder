package com.ruizhou.aicoder.model.dto.app;

import com.ruizhou.aicoder.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员分页查询应用（条件字段不含各类时间）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppAdminQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Integer priority;

    private Long userId;

    private static final long serialVersionUID = 1L;
}
