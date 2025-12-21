package com.mobility.mobility_backend.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Expose les fichiers uploadés (images d'offres) sous /uploads/**
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:uploads/")
				.setCachePeriod(3600)
				.setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
				.resourceChain(true)
				.addResolver(new PathResourceResolver());
	}
}
