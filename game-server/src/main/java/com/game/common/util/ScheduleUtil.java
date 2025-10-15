package com.game.common.util;

import com.game.core.AbstractSystemShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度工具类
 * 提供静态方法来注册和管理定时任务
 * 
 * 使用示例:
 * 
 * // 每秒执行一次任务
 * ScheduleUtil.scheduleEverySecond(() -> {
 *     System.out.println("每秒执行的任务");
 * });
 * 
 * // 每分钟执行一次任务，初始延迟10秒
 * ScheduleUtil.scheduleEveryMinute(() -> {
 *     System.out.println("每分钟执行的任务");
 * }, 10);
 * 
 * // 每小时执行一次任务
 * ScheduleUtil.scheduleEveryHour(() -> {
 *     System.out.println("每小时执行的任务");
 * });
 * 
 * // 自定义调度
 * ScheduleUtil.scheduleAtFixedRate(() -> {
 *     System.out.println("自定义调度任务");
 * }, 0, 5, TimeUnit.SECONDS);
 */
@Component
public class ScheduleUtil extends AbstractSystemShutdown implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleUtil.class);
    private static ApplicationContext applicationContext;
    private static ScheduledExecutorService taskExecutor;

    /**
     * 获取TaskExecutor实例
     *
     * @return TaskExecutor实例
     */
    private static ScheduledExecutorService getTaskExecutor() {
        if (taskExecutor == null) {
            // 创建独立的线程池，避免与Netty的线程池冲突
            taskExecutor = Executors.newScheduledThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread thread = new Thread(r, "GameTaskScheduler-");
                    thread.setDaemon(false);
                    return thread;
                }
            );
        }
        return taskExecutor;
    }

    /**
     * 按固定频率调度任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间
     * @param period       执行周期
     * @param unit         时间单位
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return getTaskExecutor().scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 按固定延迟调度任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间
     * @param delay        执行延迟
     * @param unit         时间单位
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return getTaskExecutor().scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    /**
     * 调度一次性任务
     *
     * @param task  任务实例
     * @param delay 延迟时间
     * @param unit  时间单位
     * @return 任务句柄
     */
    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return getTaskExecutor().schedule(task, delay, unit);
    }

    // ==================== 以下为新增的便捷调度方法 ====================

    /**
     * 每秒执行一次任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间（秒）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEverySecond(Runnable task, long initialDelay) {
        return scheduleAtFixedRate(task, initialDelay, 1, TimeUnit.SECONDS);
    }

    /**
     * 每分执行一次任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间（分钟）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryMinute(Runnable task, long initialDelay) {
        return scheduleAtFixedRate(task, initialDelay, 1, TimeUnit.MINUTES);
    }

    /**
     * 每小时执行一次任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间（小时）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryHour(Runnable task, long initialDelay) {
        return scheduleAtFixedRate(task, initialDelay, 1, TimeUnit.HOURS);
    }

    /**
     * 每天执行一次任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间（天）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryDay(Runnable task, long initialDelay) {
        return scheduleAtFixedRate(task, initialDelay, 1, TimeUnit.DAYS);
    }

    /**
     * 每周执行一次任务
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间（周）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryWeek(Runnable task, long initialDelay) {
        return scheduleAtFixedRate(task, initialDelay, 7, TimeUnit.DAYS);
    }

    /**
     * 每月执行一次任务（近似，按30天计算）
     *
     * @param task         任务实例
     * @param initialDelay 初始延迟时间（月）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryMonth(Runnable task, long initialDelay) {
        return scheduleAtFixedRate(task, initialDelay, 30, TimeUnit.DAYS);
    }

    /**
     * 每秒执行一次任务（无初始延迟）
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEverySecond(Runnable task) {
        return scheduleEverySecond(task, 0);
    }

    /**
     * 每分执行一次任务（无初始延迟）
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryMinute(Runnable task) {
        return scheduleEveryMinute(task, 0);
    }

    /**
     * 每小时执行一次任务（无初始延迟）
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryHour(Runnable task) {
        return scheduleEveryHour(task, 0);
    }

    /**
     * 每天执行一次任务（无初始延迟）
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryDay(Runnable task) {
        return scheduleEveryDay(task, 0);
    }

    /**
     * 每周执行一次任务（无初始延迟）
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryWeek(Runnable task) {
        return scheduleEveryWeek(task, 0);
    }

    /**
     * 每月执行一次任务（无初始延迟，近似，按30天计算）
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleEveryMonth(Runnable task) {
        return scheduleEveryMonth(task, 0);
    }

    // ==================== 以下为精确时间调度方法 ====================

    /**
     * 在每小时的整点执行任务
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtHourly(Runnable task) {
        long initialDelay = getDelayToNextHour();
        return scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Hourly task execution failed", e);
            }
        }, initialDelay, 1, TimeUnit.HOURS);
    }

    /**
     * 在每天的指定时间执行任务
     *
     * @param task  任务实例
     * @param hour  小时（0-23）
     * @param minute 分钟（0-59）
     * @param second 秒（0-59）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtDaily(Runnable task, int hour, int minute, int second) {
        long initialDelay = getDelayToNextDay(hour, minute, second);
        return scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Daily task execution failed", e);
            }
        }, initialDelay, 1, TimeUnit.DAYS);
    }

    /**
     * 在每天的00:00:01执行任务
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtDaily(Runnable task) {
        return scheduleAtDaily(task, 0, 0, 1);
    }

    /**
     * 在每周的指定时间执行任务（每周日指定时间）
     *
     * @param task 任务实例
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @param second 秒（0-59）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtWeekly(Runnable task, int hour, int minute, int second) {
        long initialDelay = getDelayToNextWeek(hour, minute, second);
        return scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Weekly task execution failed", e);
            }
        }, initialDelay, 7, TimeUnit.DAYS);
    }

    /**
     * 在每周日的00:00:01执行任务
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtWeekly(Runnable task) {
        return scheduleAtWeekly(task, 0, 0, 1);
    }

    /**
     * 在每月的最后一天指定时间执行任务
     *
     * @param task 任务实例
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @param second 秒（0-59）
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtMonthly(Runnable task, int hour, int minute, int second) {
        long initialDelay = getDelayToNextMonth(hour, minute, second);
        // 由于每月天数不同，不能使用固定周期，需要每次执行后重新调度
        return schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Monthly task execution failed", e);
            }
            // 递归调度下一次执行
            scheduleAtMonthly(task, hour, minute, second);
        }, initialDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * 在每月最后一天的00:00:01执行任务
     *
     * @param task 任务实例
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleAtMonthly(Runnable task) {
        return scheduleAtMonthly(task, 0, 0, 1);
    }

    /**
     * 计算到下一个整点的延迟时间（毫秒）
     *
     * @return 延迟时间（毫秒）
     */
    private static long getDelayToNextHour() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        return Duration.between(now, nextHour).toMillis();
    }

    /**
     * 计算到下一天指定时间的延迟时间（毫秒）
     *
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @param second 秒（0-59）
     * @return 延迟时间（毫秒）
     */
    private static long getDelayToNextDay(int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay = now.withHour(hour).withMinute(minute).withSecond(second).withNano(0);
        if (!nextDay.isAfter(now)) {
            nextDay = nextDay.plusDays(1);
        }
        return Duration.between(now, nextDay).toMillis();
    }

    /**
     * 计算到下一天指定小时的延迟时间（毫秒）
     *
     * @param hour 小时（0-23）
     * @return 延迟时间（毫秒）
     */
    private static long getDelayToNextDay(int hour) {
        return getDelayToNextDay(hour, 0, 0);
    }

    /**
     * 计算到下周日指定时间的延迟时间（毫秒）
     *
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @param second 秒（0-59）
     * @return 延迟时间（毫秒）
     */
    private static long getDelayToNextWeek(int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextSunday = now.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                .withHour(hour).withMinute(minute).withSecond(second).withNano(0);
        if (!nextSunday.isAfter(now)) {
            nextSunday = nextSunday.plusWeeks(1);
        }
        return Duration.between(now, nextSunday).toMillis();
    }

    /**
     * 计算到下个月最后一天指定时间的延迟时间（毫秒）
     *
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @param second 秒（0-59）
     * @return 延迟时间（毫秒）
     */
    private static long getDelayToNextMonth(int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(hour).withMinute(minute).withSecond(second).withNano(0);
        if (!lastDayOfMonth.isAfter(now)) {
            // 如果本月最后一天已过，计算下个月最后一天
            lastDayOfMonth = now.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(hour).withMinute(minute).withSecond(second).withNano(0);
        }
        return Duration.between(now, lastDayOfMonth).toMillis();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    @Override
    public void shutdown() {
        ScheduledExecutorService scheduler = getTaskExecutor();
        // 停止定时任务
        scheduler.shutdown();

        try {
            // 等待任务完成
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Shutting down scheduler successfully...");
    }
}