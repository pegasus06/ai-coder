package com.ruizhou.aicoder.saver;

import com.ruizhou.aicoder.ai.model.HtmlCodeResult;
import com.ruizhou.aicoder.ai.model.MultiFileCodeResult;
import com.ruizhou.aicoder.ai.model.enums.CodeGenTypeEnum;
import com.ruizhou.aicoder.exception.BusinessException;
import com.ruizhou.aicoder.exception.ErrorCode;
import org.springframework.util.DigestUtils;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();
   /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType,long appid) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult,appid);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult,appid);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}

