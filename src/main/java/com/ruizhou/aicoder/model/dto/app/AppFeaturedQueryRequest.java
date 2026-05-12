package com.ruizhou.aicoder.model.dto.app;

import com.ruizhou.aicoder.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分页查询精选应用（按优先级、创建时间排序；列表不返回 initPrompt）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppFeaturedQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用名称（模糊）
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
