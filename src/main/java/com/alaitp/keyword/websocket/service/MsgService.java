package com.alaitp.keyword.websocket.service;

import com.alaitp.keyword.websocket.dto.ChartOptionDto;

import java.util.List;

public interface MsgService {
    List<ChartOptionDto> getChartOptions();

}
