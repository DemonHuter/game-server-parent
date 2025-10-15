package com.game.server.controller;

import com.game.cache.CacheManager;
import com.game.netty.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏服务器管理控制器
 */
@RestController
@RequestMapping("/api/server")
public class ServerController {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private NettyServer nettyServer;
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("nettyRunning", nettyServer.isRunning());
        result.put("nettyPort", nettyServer.getPort());
        result.put("cacheHealthy", cacheManager.isHealthy());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取服务器状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("nettyServer", createNettyServerMap());
        result.put("cache", createCacheMap());
        result.put("jvm", createJvmMap());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<List<String>> cacheStats() {
        return ResponseEntity.ok(cacheManager.getCacheStats());
    }
    
    /**
     * 手动触发缓存持久化
     */
    @PostMapping("/cache/persist")
    public ResponseEntity<Map<String, String>> persistCache() {
        try {
            cacheManager.manualPersistence();
            Map<String, String> result = new HashMap<>();
            result.put("message", "Cache persistence triggered successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to trigger cache persistence: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 手动触发缓存清理
     */
    @PostMapping("/cache/cleanup")
    public ResponseEntity<Map<String, String>> cleanupCache() {
        try {
            cacheManager.manualCleanup();
            Map<String, String> result = new HashMap<>();
            result.put("message", "Cache cleanup triggered successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to trigger cache cleanup: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 重新加载缓存
     */
    @PostMapping("/cache/reload")
    public ResponseEntity<Map<String, String>> reloadCache() {
        try {
            cacheManager.reloadAllCaches();
            Map<String, String> result = new HashMap<>();
            result.put("message", "Cache reloaded successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to reload cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 创建Netty服务器状态Map
     */
    private Map<String, Object> createNettyServerMap() {
        Map<String, Object> nettyMap = new HashMap<>();
        nettyMap.put("running", nettyServer.isRunning());
        nettyMap.put("port", nettyServer.getPort());
        return nettyMap;
    }
    
    /**
     * 创建缓存状态Map
     */
    private Map<String, Object> createCacheMap() {
        Map<String, Object> cacheMap = new HashMap<>();
        cacheMap.put("healthy", cacheManager.isHealthy());
        cacheMap.put("stats", cacheManager.getCacheStats());
        return cacheMap;
    }
    
    /**
     * 创建JVM状态Map
     */
    private Map<String, Object> createJvmMap() {
        Map<String, Object> jvmMap = new HashMap<>();
        jvmMap.put("totalMemory", Runtime.getRuntime().totalMemory());
        jvmMap.put("freeMemory", Runtime.getRuntime().freeMemory());
        jvmMap.put("maxMemory", Runtime.getRuntime().maxMemory());
        jvmMap.put("processors", Runtime.getRuntime().availableProcessors());
        return jvmMap;
    }
}