package com.alaitp.keyword.websocket.thread;

import com.alaitp.keyword.websocket.message.ChartOptionSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

import static com.alaitp.keyword.websocket.message.ChartOptionSession.SEND_INTERVAL;

@Slf4j
public class ScheduleThreadPool {
    private static final ConcurrentMap<String, ScheduledFuture<?>> REQUEST_ID_SCHEDULED_FUTURE_MAP = new ConcurrentHashMap<>();
    /**
     * Because of this is a ScheduleThreadPool, the thread won't use cpu while on the fixed delay time interval
     * the pool size can be much larger than the number of cpu cores.
     */
    private static final int MAX_THREAD = 10 * Runtime.getRuntime().availableProcessors();
    private static final ThreadFactory NAMED_THREAD_FACTORY = new CustomizableThreadFactory("chart-schedule-%d");
    private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(MAX_THREAD, NAMED_THREAD_FACTORY);

    public static void submit(ChartOptionSession chartOptionSession) {
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new ScheduleSendThread(chartOptionSession), 500, SEND_INTERVAL, TimeUnit.MILLISECONDS);
        REQUEST_ID_SCHEDULED_FUTURE_MAP.put(chartOptionSession.getRequestId(), scheduledFuture);
    }

    /**
     * cancel ScheduledFuture when a chart option session complete
     */
    public static void endTask(String requestId) {
        ScheduledFuture<?> scheduledFuture = REQUEST_ID_SCHEDULED_FUTURE_MAP.get(requestId);
        if (scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(false);
        REQUEST_ID_SCHEDULED_FUTURE_MAP.remove(requestId);
        log.info("requestId: " + requestId + " end, canceled ScheduledFuture");
    }
}
