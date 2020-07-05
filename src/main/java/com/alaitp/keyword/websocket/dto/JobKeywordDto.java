package com.alaitp.keyword.websocket.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class JobKeywordDto {
    private String jobId;
    private String requestId;
    private List<KeywordDto> keywordList;
    private Set<String> categoryList;

    /**
     * inner class for a single KeywordDto
     */
    @Data
    public static class KeywordDto {
        private String keyword;
        private String category;
        private String startIdx;
        private String endIdx;
    }
}
