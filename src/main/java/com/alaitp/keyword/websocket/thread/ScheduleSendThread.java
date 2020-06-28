package com.alaitp.keyword.websocket.thread;

import com.alaitp.keyword.websocket.message.ChartOptionSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleSendThread extends Thread {
    private ChartOptionSession chartOptionSession;

    public ScheduleSendThread(ChartOptionSession chartOptionSession) {
        this.chartOptionSession = chartOptionSession;
    }

    @SneakyThrows
    @Override
    public void run() {
        super.run();
        startTimer();
    }

    private void startTimer() throws InterruptedException {
        // sleep one second on start
        Thread.sleep(1000);
        chartOptionSession.sendOnInit();
        while (!chartOptionSession.sessionEnd()) {
            Thread.sleep(ChartOptionSession.SEND_INTERVAL);
            chartOptionSession.sendOnInterval();
        }
    }
}
