package com.ruizhou.aicoder.ai.core;

import com.ruizhou.aicoder.ai.model.enums.CodeGenTypeEnum;
import com.ruizhou.aicoder.ai.service.AiService;
import com.ruizhou.aicoder.config.AiCodeGeneratorServiceFactory;
import com.ruizhou.aicoder.exception.BusinessException;
import com.ruizhou.aicoder.exception.ErrorCode;
import com.ruizhou.aicoder.parser.CodeParserExecutor;
import com.ruizhou.aicoder.saver.CodeFileSaverExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {
    @Resource
    @Lazy
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, long appid) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appid);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }


    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, long appid) {
        AiService aiService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appid);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appid);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appid);
            }
            case VUE_PROJECT -> {
                Flux<String> codeStream = aiService.generateVueProjectCodeStream(appid, userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.VUE_PROJECT, appid);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

}
