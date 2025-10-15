package com.game.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 校验工具类
 */
public class ValidationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);
    
    /** 用户名正则表达式：字母、数字、下划线，3-20位 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    /** 密码正则表达式：字母、数字、特殊字符，6-32位 */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9@#$%^&+=]{6,32}$");
    
    /** 邮箱正则表达式 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    /**
     * 校验用户名格式
     */
    public static boolean isValidUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * 校验密码格式
     */
    public static boolean isValidPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 校验邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 校验字符串长度
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * 校验聊天内容
     */
    public static boolean isValidChatContent(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }
        // 检查长度
        if (content.length() > 500) {
            return false;
        }
        // 检查是否包含非法字符
        return !containsIllegalCharacters(content);
    }
    
    /**
     * 检查是否包含非法字符
     */
    private static boolean containsIllegalCharacters(String content) {
        // 这里可以添加更多非法字符的检查
        return content.contains("<script>") || 
               content.contains("</script>") ||
               content.contains("javascript:") ||
               content.contains("onload=") ||
               content.contains("onerror=");
    }
    
    /**
     * 校验数值范围
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }
    
    /**
     * 校验数值范围
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * 校验ID是否有效（大于0）
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }
    
    /**
     * 校验字符串是否不为空且不为null
     */
    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotBlank(str);
    }
    
    /**
     * 校验对象是否不为null
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }
    
    /**
     * 批量校验对象是否都不为null
     */
    public static boolean areAllNotNull(Object... objects) {
        if (objects == null || objects.length == 0) {
            return false;
        }
        for (Object obj : objects) {
            if (obj == null) {
                return false;
            }
        }
        return true;
    }
}