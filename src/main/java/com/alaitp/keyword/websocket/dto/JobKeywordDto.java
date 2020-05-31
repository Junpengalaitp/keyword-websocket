package com.alaitp.keyword.websocket.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobKeywordDto {
    private String jobId;
    private List<KeywordDto> keywordList;

    @Data
    public class KeywordDto {
        private String keyword;
        private String category;
        private String startIdx;
        private String endIdx;
    }
}
