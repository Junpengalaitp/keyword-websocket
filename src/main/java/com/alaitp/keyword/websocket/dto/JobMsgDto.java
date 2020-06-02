package com.alaitp.keyword.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobMsgDto {
    private ChartOptionDto chartOption;
    private JobKeywordDto jobKeyword;
}
