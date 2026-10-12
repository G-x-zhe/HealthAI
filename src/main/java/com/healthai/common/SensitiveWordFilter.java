package com.healthai.common;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SensitiveWordFilter {
    // 医药广告违禁词
    private final List<String> words = Arrays.asList(
            "根治","治愈","保证","绝对","100%","无效退款","无毒副作用",
            "包治百病","长生不老","返老还童","祖传秘方","特效药",
            "最佳","第一","顶级","国家级","世界级","史无前例");

    public String filter(String text) {
        if (text == null) return null;
        for (String w : words) text = text.replaceAll(w, "***");
        return text;
    }
}