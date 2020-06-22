package com.alaitp.keyword.websocket.service;

import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;

import java.util.List;

public interface MsgService {
    List<ChartOptionDto> getChartOptions(JobKeywordDto jobKeywordDto);

}
