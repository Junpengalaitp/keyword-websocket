package com.alaitp.keyword.websocket.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Set<String> categories() {
        Set<String> categories = new HashSet<>();
        for (KeywordDto keywordDto: keywordList) {
            categories.add(keywordDto.getCategory());
        }
        return categories;
    }
}
