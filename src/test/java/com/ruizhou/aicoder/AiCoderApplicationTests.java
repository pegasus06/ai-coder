package com.ruizhou.aicoder;

import cn.hutool.core.util.StrUtil;
import com.ruizhou.aicoder.ai.core.CodeParser;
import com.ruizhou.aicoder.ai.service.AiCodeGeneratorService;
import com.ruizhou.aicoder.ai.core.AiCodeGeneratorFacade;
import com.ruizhou.aicoder.ai.model.HtmlCodeResult;
import com.ruizhou.aicoder.ai.model.MultiFileCodeResult;
import com.ruizhou.aicoder.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AiCoderApplicationTests {

    @Test
    void contextLoads() {
    }

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    @Test
    void generateAndSaveCodeStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream("任务记录网站", CodeGenTypeEnum.VUE_PROJECT, 1L);
        List<String> result = stringFlux.collectList().block();
        assertNotNull(result);
        String join = String.join("", result);
        assertNotNull(join);
    }


//    @Test
//    void generateAndSaveCodeStream() {
//        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE);
//        // 阻塞等待所有数据收集完成
//        List<String> result = codeStream.collectList().block();
//        // 验证结果
//        Assertions.assertNotNull(result);
//        String completeContent = String.join("", result);
//        Assertions.assertNotNull(completeContent);
//    }
}
