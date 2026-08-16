package com.yytnet.fms.ailab.chat.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatReq(
        // 用户本次真正提出的问题，会作为 User Prompt 发送给模型。
        @NotBlank(message = "信息不能为空")
        String msg,

        // 可选的额外系统提示词，用来临时补充本次请求的模型行为约束。
        // System Prompt 用来定义模型的角色、边界和回答风格；它不是用户问题本身。
        @Size(max = 1000, message = "系统提示词不能超过1000个字符")
        String systemPrompt,

        // 以下参数是请求级模型参数，只影响当前这一次模型调用。
        // temperature 控制回答的随机性和发散程度：
        // - 值越低，回答越稳定、保守、可重复，适合技术问答和业务场景。
        // - 值越高，回答越发散、有创造性，但也更容易不稳定。
        // 当前学习阶段建议常用 0.2 ~ 0.7。
        @DecimalMin(value = "0.0", message = "temperature不能小于0")
        @DecimalMax(value = "2.0", message = "temperature不能大于2")
        Double temperature,

        // topP 控制 nucleus sampling 的候选词概率范围：
        // - 模型只会在累计概率达到 topP 的候选 token 中采样。
        // - 值越小，候选范围越窄，回答越保守。
        // - 值越大，候选范围越宽，回答更多样。
        // 技术问答通常可从 0.7 ~ 0.9 开始观察效果。
        @DecimalMin(value = "0.0", message = "topP不能小于0")
        @DecimalMax(value = "1.0", message = "topP不能大于1")
        Double topP,

        // topK 控制每一步最多从概率最高的 K 个候选 token 中选择：
        // - topK 越小，模型选择空间越小，输出更稳定。
        // - topK 越大，模型选择空间越大，输出更多样。
        // 技术问答通常可从 20 ~ 40 开始观察效果。
        @Min(value = 1, message = "topK不能小于1")
        @Max(value = 100, message = "topK不能大于100")
        Integer topK,

        // numPredict 控制本次最多生成多少 token，可近似理解为最大输出长度：
        // - 值太小，回答可能被截断。
        // - 值太大，响应会更慢，并占用更多模型资源。
        // 简短回答可用 64 ~ 256，较完整解释可用 512 ~ 1024。
        @Min(value = 1, message = "numPredict不能小于1")
        @Max(value = 8192, message = "numPredict不能大于8192")
        Integer numPredict
) {
}
