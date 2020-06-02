package com.alaitp.keyword.websocket.service;

import com.alaitp.keyword.websocket.dto.JobKeywordDto;
import com.alaitp.keyword.websocket.dto.JobMsgDto;

public interface MsgService {
    JobMsgDto getJobKeywordMsg(JobKeywordDto jobKeywordDto);
}
