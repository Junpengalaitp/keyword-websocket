package com.alaitp.keyword.websocket.thread;

import com.alaitp.keyword.websocket.message.ChartOptionSession;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static com.alaitp.keyword.websocket.message.ChartOptionSession.SEND_INTERVAL;

@Slf4j
public class ScheduleThreadPool {
    public static final ConcurrentMap<String, ScheduledFuture<?>> requestIdScheduledFutureMap = new ConcurrentHashMap<>();
    /**
     * Because of this is a ScheduleThreadPool, the thread won't use cpu while on the fixed delay time interval
     * the pool size can be much larger than the number of cpu cores.
     */
    private static final int MAX_THREAD = 10 * Runtime.getRuntime().availableProcessors();
    public static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(MAX_THREAD);

    public static void submit(ChartOptionSession chartOptionSession) {
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new ScheduleSendThread(chartOptionSession), 500, SEND_INTERVAL, TimeUnit.MILLISECONDS);
        requestIdScheduledFutureMap.put(chartOptionSession.getRequestId(), scheduledFuture);
    }

    /**
     * cancel ScheduledFuture when a chart option session complete
     */
    public static void endTask(String requestId) {
        ScheduledFuture<?> scheduledFuture = requestIdScheduledFutureMap.get(requestId);
        scheduledFuture.cancel(false);
        requestIdScheduledFutureMap.remove(requestId);
        log.info("requestId: " + requestId + " end, canceled ScheduledFuture");
    }
}
