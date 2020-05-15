package com.alaitp.keyword.websocket.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JobKeywordDto {
    private String jobId;
    private List<Map<String, String>> keywordList;
}
