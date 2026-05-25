package com.ruizhou.aicoder.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对话消息类型枚举
 */
@Getter
public enum MessageTypeEnum {

    USER("用户消息", "user"),
    AI("AI 消息", "ai"),
    ERROR("错误消息", "error");

    private final String text;

    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    private static final Map<String, MessageTypeEnum> VALUE_CACHE;

    static {
        VALUE_CACHE = Arrays.stream(values()).collect(Collectors.toMap(MessageTypeEnum::getValue, e -> e));
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static MessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_CACHE.get(value);
    }
}
