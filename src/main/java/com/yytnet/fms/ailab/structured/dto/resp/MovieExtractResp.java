package com.yytnet.fms.ailab.structured.dto.resp;

public record MovieExtractResp(
        // true 表示输入文本包含可识别的电影信息；false 表示输入与电影信息提取任务无关。
        Boolean movieRelated,

        // 电影标题。对外响应里无法确定时返回 null；模型原始 JSON 会先用空字符串表达缺失。
        String title,

        // 导演姓名。对外响应里无法确定时返回 null；不要让模型编造。
        String director,

        // 上映年份。对外响应里无法确定时返回 null；模型原始 JSON 会先用 0 表达缺失。
        Integer releaseYear,

        // 当 movieRelated=false 或字段缺失时，说明原因；正常提取成功时可以返回 null。
        String reason
) {
}
