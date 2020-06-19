package com.alaitp.keyword.websocket.service;

import com.alaitp.keyword.websocket.dto.ChartOptionDto;
import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.dto.JobMsgDto;

import java.util.List;

public interface MsgService {
    List<ChartOptionDto> getChartOptions(JobKeywordDto jobKeywordDto);

    JobMsgDto getPureJobKeywordMsg(JobKeywordDto jobKeywordDto);
}
