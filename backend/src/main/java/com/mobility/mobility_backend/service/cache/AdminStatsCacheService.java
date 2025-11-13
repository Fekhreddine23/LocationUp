package com.mobility.mobility_backend.service.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import com.mobility.mobility_backend.dto.AdminStatsDTO;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AdminStatsCacheService {
	
	
	 private static final Logger logger = LoggerFactory.getLogger(AdminStatsCacheService.class);

	    @Autowired
	    private MemoryCacheService memoryCacheService;

	    private static final String STATS_KEY = "admin:stats";
	    private static final String USERS_KEY_PREFIX = "admin:users:";
	    private static final String ACTIVITY_KEY = "admin:recent_activity";

	    public void cacheStats(AdminStatsDTO stats) {
	        memoryCacheService.set(STATS_KEY, stats, 5, TimeUnit.MINUTES);
	    }

	    public AdminStatsDTO getCachedStats() {
	        Object cached = memoryCacheService.get(STATS_KEY);
	        if (cached instanceof AdminStatsDTO) {
	            logger.info("✅ Statistiques récupérées du cache MEMORY");
	            return (AdminStatsDTO) cached;
	        }
	        return null;
	    }

	    public void cacheUsers(int page, int size, Object users) {
	        String key = USERS_KEY_PREFIX + "page:" + page + ":size:" + size;
	        memoryCacheService.set(key, users, 10, TimeUnit.MINUTES);
	    }

	    public Object getCachedUsers(int page, int size) {
	        String key = USERS_KEY_PREFIX + "page:" + page + ":size:" + size;
	        Object cached = memoryCacheService.get(key);
	        if (cached != null) {
	            logger.info("✅ Utilisateurs récupérés du cache MEMORY");
	        }
	        return cached;
	    }

	    public void cacheRecentActivity(Object activity) {
	        memoryCacheService.set(ACTIVITY_KEY, activity, 2, TimeUnit.MINUTES);
	    }

	    public Object getCachedRecentActivity() {
	        Object cached = memoryCacheService.get(ACTIVITY_KEY);
	        if (cached != null) {
	            logger.info("✅ Activité récente récupérée du cache MEMORY");
	        }
	        return cached;
	    }

	    public void invalidateStatsCache() {
	        memoryCacheService.delete(STATS_KEY);
	        logger.info("🗑️ Cache statistiques invalidé");
	    }

	    public void invalidateAllAdminCache() {
	        memoryCacheService.deleteByPattern("admin:");
	        logger.info("🗑️ Tout le cache admin invalidé");
	    }

}
