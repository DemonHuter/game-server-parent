package com.game.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间工具类
 */
public class TimeUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);
    
    /** 标准日期时间格式 */
    public static final String STANDARD_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** 标准日期格式 */
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
    
    /** 标准时间格式 */
    public static final String STANDARD_TIME_FORMAT = "HH:mm:ss";
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern(STANDARD_DATETIME_FORMAT);
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern(STANDARD_DATE_FORMAT);
    
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern(STANDARD_TIME_FORMAT);
    
    /**
     * 获取当前时间戳（毫秒）
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取当前时间
     */
    public static Date getCurrentTime() {
        return new Date();
    }
    
    /**
     * 时间戳转Date
     */
    public static Date timestampToDate(long timestamp) {
        return new Date(timestamp);
    }
    
    /**
     * Date转时间戳
     */
    public static long dateToTimestamp(Date date) {
        return date != null ? date.getTime() : 0;
    }
    
    /**
     * 格式化当前时间
     */
    public static String formatCurrentTime() {
        return formatTime(LocalDateTime.now());
    }
    
    /**
     * 格式化时间
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }
    
    /**
     * 格式化日期
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }
    
    /**
     * 格式化时间部分
     */
    public static String formatTimeOnly(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "";
    }
    
    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }
    
    /**
     * LocalDateTime转Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return localDateTime != null ? 
                Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
    
    /**
     * 计算时间差（毫秒）
     */
    public static long timeDifference(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        return end.getTime() - start.getTime();
    }
    
    /**
     * 检查时间间隔是否超过指定毫秒数
     */
    public static boolean isIntervalExceeded(Date lastTime, long intervalMs) {
        if (lastTime == null) {
            return true;
        }
        return timeDifference(lastTime, getCurrentTime()) >= intervalMs;
    }
    
    /**
     * 计算两个时间戳的差值（秒）
     */
    public static long getTimestampDifferenceSeconds(long timestamp1, long timestamp2) {
        return Math.abs(timestamp2 - timestamp1) / 1000;
    }
    
    /**
     * 检查时间戳是否在有效范围内（不超过指定秒数的差异）
     */
    public static boolean isTimestampValid(long timestamp, long maxDifferenceSeconds) {
        long currentTimestamp = getCurrentTimestamp();
        long differenceSeconds = getTimestampDifferenceSeconds(timestamp, currentTimestamp);
        return differenceSeconds <= maxDifferenceSeconds;
    }
}