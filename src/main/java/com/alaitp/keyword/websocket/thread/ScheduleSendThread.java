package com.alaitp.keyword.websocket.thread;

import com.alaitp.keyword.websocket.message.ChartOptionSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleSendThread extends Thread {
    private ChartOptionSession chartOptionSession;

    public ScheduleSendThread(ChartOptionSession chartOptionSession) {
        this.chartOptionSession = chartOptionSession;
    }

    @Override
    public void run() {
        super.run();
        startTimer();
    }

    private void startTimer() {
        while (!chartOptionSession.sessionEnd()) {
            try {
                Thread.sleep(chartOptionSession.getSEND_INTERVAL());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chartOptionSession.sendOnInterval();
        }
    }
}
