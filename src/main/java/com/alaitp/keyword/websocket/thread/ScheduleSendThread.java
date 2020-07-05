package com.alaitp.keyword.websocket.thread;

import com.alaitp.keyword.websocket.message.ChartOptionSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 1. send chart options on the time interval.
 * 2. if current thread is finished, notify frontend to disconnect web socket, clear thread and cache related to this request.
 */
@Slf4j
public class ScheduleSendThread extends Thread {
    private final ChartOptionSession chartOptionSession;

    public ScheduleSendThread(ChartOptionSession chartOptionSession) {
        this.chartOptionSession = chartOptionSession;
    }

    @SneakyThrows
    @Override
    public void run() {
        super.run();
        if (!chartOptionSession.isSessionEnd()) {
            chartOptionSession.sendOnInterval();
        } else {
            ScheduleThreadPool.endTask(chartOptionSession.getRequestId());
            chartOptionSession.endSession();
            log.info("ScheduleSendThread for request id: {} ended.", chartOptionSession.getRequestId());
        }
    }
}
