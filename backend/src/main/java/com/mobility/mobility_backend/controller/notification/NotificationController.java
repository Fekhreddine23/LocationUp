package com.mobility.mobility_backend.controller.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import com.mobility.mobility_backend.dto.socket.NotificationMessage;
import com.mobility.mobility_backend.dto.socket.NotificationSeverity;
import com.mobility.mobility_backend.service.UserService;
import com.mobility.mobility_backend.service.notification.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserService userService;

	// === CONNEXION SSE ===

	/**
	 * Connexion au flux de notifications SSE
	 */
	@GetMapping(value = "/stream", produces = "text/event-stream")
	public SseEmitter streamNotifications(@RequestParam String userId,
			@RequestParam(required = false) String userRole) {

		enforceUserAccess(userId);
		String effectiveRole = isAdmin() ? "ADMIN" : "USER";
		System.out.println("🔔 [CONTROLLER] Connexion SSE demandée - userId: " + userId + ", userRole: " + userRole);
		return notificationService.connect(userId, effectiveRole);
	}

	// === ENVOI DE NOTIFICATIONS ===

	/**
	 * Envoyer une notification personnalisée
	 */
	@PostMapping("/send")
	public ResponseEntity<String> sendNotification(@RequestBody NotificationMessage notification) {
		enforceAdmin();
		try {
			System.out.println("📤 [CONTROLLER] Envoi notification personnalisée: " + notification.getTitle() + " à: "
					+ notification.getRecipient());
			notificationService.sendNotification(notification);
			return ResponseEntity.ok("Notification envoyée avec succès");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur envoi notification: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur lors de l'envoi: " + e.getMessage());
		}
	}

	/**
	 * Notification pour événement de réservation
	 */
	@PostMapping("/reservation-event")
	public ResponseEntity<String> sendReservationEvent(@RequestParam String reservationId, @RequestParam String userId,
			@RequestParam String eventType, @RequestBody(required = false) java.util.Map<String, Object> data) {

		enforceAdmin();
		try {
			System.out.println("🏨 [CONTROLLER] Événement réservation - reservationId: " + reservationId + ", userId: "
					+ userId + ", eventType: " + eventType);
			notificationService.notifyReservationEvent(reservationId, userId, eventType, data);
			return ResponseEntity.ok("Notification de réservation envoyée");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur événement réservation: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Notification pour événement de paiement
	 */
	@PostMapping("/payment-event")
	public ResponseEntity<String> sendPaymentEvent(@RequestParam String paymentId, @RequestParam String userId,
			@RequestParam String eventType, @RequestBody(required = false) java.util.Map<String, Object> data) {

		enforceAdmin();
		try {
			System.out.println("💳 [CONTROLLER] Événement paiement - paymentId: " + paymentId + ", userId: " + userId
					+ ", eventType: " + eventType);
			notificationService.notifyPaymentEvent(paymentId, userId, eventType, data);
			return ResponseEntity.ok("Notification de paiement envoyée");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur événement paiement: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Notification système (broadcast à tous)
	 */
	@PostMapping("/system-event")
	public ResponseEntity<String> sendSystemEvent(@RequestParam String eventType, @RequestParam String message,
			@RequestParam String severity) {

		enforceAdmin();
		try {
			System.out.println("🔊 [CONTROLLER] Événement système - eventType: " + eventType + ", message: " + message
					+ ", severity: " + severity);
			NotificationSeverity severityEnum = NotificationSeverity.valueOf(severity.toUpperCase());
			notificationService.notifySystemEvent(eventType, message, severityEnum);
			return ResponseEntity.ok("Notification système envoyée");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur événement système: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	// === GESTION DES NOTIFICATIONS ===

	/**
	 * Récupérer les notifications d'un utilisateur
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<java.util.List<NotificationMessage>> getUserNotifications(@PathVariable String userId,
			@RequestParam(required = false) String category, @RequestParam(required = false) String severity) {

		enforceUserAccess(userId);
		try {
			System.out.println("📋 [CONTROLLER] Récupération notifications - userId: " + userId + ", category: "
					+ category + ", severity: " + severity);
			java.util.List<NotificationMessage> notifications = notificationService.getUserNotifications(userId,
					category, severity);
			return ResponseEntity.ok(notifications);
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur récupération notifications: " + e.getMessage());
			return ResponseEntity.badRequest().body(null);
		}
	}

	/**
	 * Compter les notifications non lues
	 */
	@GetMapping("/user/{userId}/unread-count")
	public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
		enforceUserAccess(userId);
		try {
			System.out.println("🔢 [CONTROLLER] Comptage non lus - userId: " + userId);
			long count = notificationService.getUnreadCount(userId);
			return ResponseEntity.ok(count);
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur comptage non lus: " + e.getMessage());
			return ResponseEntity.badRequest().body(0L);
		}
	}

	/**
	 * Marquer une notification comme lue
	 */
	@PostMapping("/{notificationId}/read")
	public ResponseEntity<String> markAsRead(@PathVariable String notificationId, @RequestParam String userId) {

		enforceUserAccess(userId);
		try {
			System.out.println(
					"📖 [CONTROLLER] Marquer comme lue - notificationId: " + notificationId + ", userId: " + userId);
			notificationService.markAsRead(notificationId, userId);
			return ResponseEntity.ok("Notification marquée comme lue");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur marquer comme lue: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Marquer toutes les notifications comme lues
	 */
	@PostMapping("/user/{userId}/mark-all-read")
	public ResponseEntity<String> markAllAsRead(@PathVariable String userId) {
		enforceUserAccess(userId);
		try {
			System.out.println("📚 [CONTROLLER] Marquer toutes comme lues - userId: " + userId);
			notificationService.markAllAsRead(userId);
			return ResponseEntity.ok("Toutes les notifications marquées comme lues");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur marquer toutes comme lues: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Supprimer une notification
	 */
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<String> deleteNotification(@PathVariable String notificationId, @RequestParam String userId) {

		enforceUserAccess(userId);
		try {
			System.out.println("🗑️ [CONTROLLER] Suppression notification - notificationId: " + notificationId
					+ ", userId: " + userId);
			notificationService.deleteNotification(notificationId, userId);
			return ResponseEntity.ok("Notification supprimée");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur suppression: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	// === ENDPOINTS DE TEST ===

	/**
	 * Envoyer une notification de test
	 */
	@PostMapping("/test")
	public ResponseEntity<String> sendTestNotification(@RequestParam String userId,
			@RequestParam(required = false, defaultValue = "INFO") String severity,
			@RequestParam(required = false, defaultValue = "Test Notification") String message) {

		enforceAdmin();
		try {
			System.out.println("🧪 [CONTROLLER] Test notification - userId: " + userId + ", severity: " + severity
					+ ", message: " + message);

			// ✅ CORRECTION : Utiliser la méthode dédiée du service au lieu de créer
			// manuellement
			notificationService.sendTestNotification(userId, message, severity);
			return ResponseEntity.ok("Notification de test envoyée à " + userId);

		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur test notification: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Broadcast de test à tous les utilisateurs
	 */
	@PostMapping("/test-broadcast")
	public ResponseEntity<String> sendTestBroadcast(@RequestParam String message,
			@RequestParam(required = false, defaultValue = "INFO") String severity) {

		enforceAdmin();
		try {
			System.out.println("📢 [CONTROLLER] Test broadcast - message: " + message + ", severity: " + severity);
			NotificationSeverity severityEnum = NotificationSeverity.valueOf(severity.toUpperCase());
			notificationService.notifySystemEvent("test_broadcast", message, severityEnum);
			return ResponseEntity.ok("Broadcast de test envoyé à tous les utilisateurs");
		} catch (Exception e) {
			System.err.println("❌ [CONTROLLER] Erreur test broadcast: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
		}
	}

	// ✅ NOUVEL ENDPOINT : Statut des connexions SSE (pour debug)
	@GetMapping("/debug/connections")
	public ResponseEntity<java.util.Map<String, Object>> getConnectionStatus() {
		try {
			java.util.Map<String, Object> status = new java.util.HashMap<>();
			status.put("timestamp", new java.util.Date());
			status.put("service", "Notification Service Active");
			return ResponseEntity.ok(status);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		}
	}

	private void enforceUserAccess(String userId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
		}
		if (isAdmin(auth)) {
			return;
		}

		Integer requestedId = parseUserId(userId);
		String username = auth.getName();
		Integer currentId = userService.getUserByUsername(username)
				.map(user -> user.getId())
				.orElse(null);
		if (currentId == null || !currentId.equals(requestedId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
		}
	}

	private void enforceAdmin() {
		if (!isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
		}
	}

	private boolean isAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return isAdmin(auth);
	}

	private boolean isAdmin(Authentication auth) {
		if (auth == null || auth.getAuthorities() == null) {
			return false;
		}
		for (GrantedAuthority authority : auth.getAuthorities()) {
			if ("ROLE_ADMIN".equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	private Integer parseUserId(String userId) {
		try {
			return Integer.valueOf(userId);
		} catch (NumberFormatException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId");
		}
	}
}
