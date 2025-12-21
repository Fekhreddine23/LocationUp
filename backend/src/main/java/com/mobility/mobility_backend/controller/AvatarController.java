package com.mobility.mobility_backend.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.service.UserAvatarService;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users/avatar")
public class AvatarController {

	private final UserAvatarService userAvatarService;

	public AvatarController(UserAvatarService userAvatarService) {
		this.userAvatarService = userAvatarService;
	}

	@GetMapping("/{filename:.+}")
	@PermitAll
	public ResponseEntity<Resource> serveAvatar(@PathVariable String filename, HttpServletRequest request)
			throws IOException {
		Resource resource = userAvatarService.loadAsResource(filename);
		String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		if (contentType == null) {
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800").body(resource);
	}
}
