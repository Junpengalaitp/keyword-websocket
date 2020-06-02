package com.alaitp.keyword.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JobMsgDto {
    private List<ChartOptionDto> chartOptions;
    private JobKeywordDto jobKeyword;
}
