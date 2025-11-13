package com.mobility.mobility_backend.service.cache;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class MemoryCacheService {
	
	  private static final Logger logger = LoggerFactory.getLogger(MemoryCacheService.class);
	    
	    private final ConcurrentHashMap<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
	    
	    private static final long DEFAULT_TTL = 30; // 30 minutes

	    private static class CacheEntry {
	        Object data;
	        long timestamp;
	        long ttl;
	        
	        CacheEntry(Object data, long ttl) {
	            this.data = data;
	            this.timestamp = System.currentTimeMillis();
	            this.ttl = ttl;
	        }
	        
	        boolean isExpired() {
	            return System.currentTimeMillis() - timestamp > ttl;
	        }
	    }

	    public void set(String key, Object value) {
	        set(key, value, DEFAULT_TTL, TimeUnit.MINUTES);
	    }

	    public void set(String key, Object value, long timeout, TimeUnit unit) {
	        try {
	            long ttlMillis = unit.toMillis(timeout);
	            memoryCache.put(key, new CacheEntry(value, ttlMillis));
	            logger.debug("✅ Cache MEMORY SET - Key: {}, TTL: {} {}", key, timeout, unit);
	        } catch (Exception e) {
	            logger.error("❌ Erreur cache memory set - Key: {}, Error: {}", key, e.getMessage());
	        }
	    }

	    public Object get(String key) {
	        try {
	            CacheEntry entry = memoryCache.get(key);
	            if (entry != null) {
	                if (entry.isExpired()) {
	                    memoryCache.remove(key);
	                    logger.debug("❌ Cache MEMORY MISS (expired) - Key: {}", key);
	                    return null;
	                }
	                logger.debug("✅ Cache MEMORY HIT - Key: {}", key);
	                return entry.data;
	            }
	            logger.debug("❌ Cache MEMORY MISS - Key: {}", key);
	            return null;
	        } catch (Exception e) {
	            logger.error("❌ Erreur cache memory get - Key: {}, Error: {}", key, e.getMessage());
	            return null;
	        }
	    }

	    public boolean exists(String key) {
	        CacheEntry entry = memoryCache.get(key);
	        return entry != null && !entry.isExpired();
	    }

	    public void delete(String key) {
	        try {
	            memoryCache.remove(key);
	            logger.debug("🗑️ Cache MEMORY DELETE - Key: {}", key);
	        } catch (Exception e) {
	            logger.error("❌ Erreur cache memory delete - Key: {}, Error: {}", key, e.getMessage());
	        }
	    }

	    public void deleteByPattern(String pattern) {
	        try {
	            memoryCache.keySet().removeIf(key -> key.startsWith(pattern));
	            logger.debug("🗑️ Cache MEMORY DELETE PATTERN - Pattern: {}", pattern);
	        } catch (Exception e) {
	            logger.error("❌ Erreur cache memory deleteByPattern - Pattern: {}, Error: {}", pattern, e.getMessage());
	        }
	    }

	    public Long getTtl(String key) {
	        try {
	            CacheEntry entry = memoryCache.get(key);
	            if (entry != null && !entry.isExpired()) {
	                long remaining = entry.timestamp + entry.ttl - System.currentTimeMillis();
	                return Math.max(0, remaining / 1000); // Retourne en secondes
	            }
	            return -1L;
	        } catch (Exception e) {
	            logger.error("❌ Erreur cache memory TTL - Key: {}, Error: {}", key, e.getMessage());
	            return -1L;
	        }
	    }

}
