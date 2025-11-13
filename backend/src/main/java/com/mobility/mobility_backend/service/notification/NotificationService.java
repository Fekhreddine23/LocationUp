package com.mobility.mobility_backend.service.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mobility.mobility_backend.dto.socket.NotificationCategory;
import com.mobility.mobility_backend.dto.socket.NotificationMessage;
import com.mobility.mobility_backend.dto.socket.NotificationSeverity;
import com.mobility.mobility_backend.repository.notification.NotificationRepository;

@Service
public class NotificationService {

	private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

	@Autowired
	private NotificationRepository notificationRepository;

	// === MÉTHODES SSE ===

	public SseEmitter connect(String userId, String userRole) {
		System.out.println("🔔 [SERVICE] Nouvelle connexion SSE pour userId: " + userId);

		SseEmitter emitter = new SseEmitter(120_000L); // 2 minutes timeout
		String connectionId = userId + "_" + System.currentTimeMillis();

		userEmitters.put(connectionId, emitter);
		System.out.println("📊 [SERVICE] Connexions actives: " + userEmitters.size());

		// Configuration des callbacks
		emitter.onCompletion(() -> {
			System.out.println("🔌 [SERVICE] Connexion SSE terminée: " + connectionId);
			userEmitters.remove(connectionId);
		});

		emitter.onTimeout(() -> {
			System.out.println("⏰ [SERVICE] Timeout SSE: " + connectionId);
			userEmitters.remove(connectionId);
		});

		// Envoyer un message de bienvenue
		try {
			NotificationMessage welcomeMsg = new NotificationMessage();
			welcomeMsg.setCategory(NotificationCategory.SYSTEM_ALERT);
			welcomeMsg.setSeverity(NotificationSeverity.INFO);
			welcomeMsg.setTitle("Connexion établie");
			welcomeMsg.setMessage("Vous êtes connecté aux notifications temps réel");
			welcomeMsg.setRecipient(userId);

			emitter.send(SseEmitter.event().name("notification").data(welcomeMsg));

			System.out.println("✅ [SERVICE] Message de bienvenue envoyé à: " + userId);
		} catch (IOException e) {
			System.err.println("❌ [SERVICE] Erreur envoi message bienvenue: " + e.getMessage());
		}

		return emitter;
	}

	public void sendNotification(NotificationMessage notification) {
		System.out.println(
				"📤 [SERVICE] Envoi notification: " + notification.getTitle() + " à: " + notification.getRecipient());

		// Sauvegarder en base
		notificationRepository.save(notification);
		System.out.println("💾 [SERVICE] Notification sauvegardée en base: " + notification.getId());

		// Déterminer les destinataires
		List<String> recipients = resolveRecipients(notification.getRecipient());
		System.out.println("🎯 [SERVICE] Destinataires trouvés: " + recipients);

		// Envoyer en temps réel via SSE
		int sentCount = 0;
		for (String recipient : recipients) {
			for (Map.Entry<String, SseEmitter> entry : userEmitters.entrySet()) {
				if (entry.getKey().startsWith(recipient + "_")) {
					try {
						entry.getValue().send(SseEmitter.event().name("notification").data(notification));
						sentCount++;
						System.out.println("📨 [SERVICE] Notification envoyée via SSE à: " + recipient);
					} catch (IOException e) {
						System.err.println("❌ [SERVICE] Erreur envoi SSE à " + recipient + ": " + e.getMessage());
						entry.getValue().completeWithError(e);
						userEmitters.remove(entry.getKey());
					}
				}
			}
		}

		System.out.println("📊 [SERVICE] Total notifications envoyées via SSE: " + sentCount);
		if (sentCount == 0) {
			System.out.println("⚠️ [SERVICE] Aucun client connecté pour recevoir la notification");
		}
	}

	private List<String> resolveRecipients(String recipientPattern) {
		List<String> recipients = new ArrayList<>();

		if ("all".equals(recipientPattern)) {
			// Tous les utilisateurs connectés
			recipients.addAll(userEmitters.keySet().stream().map(key -> key.split("_")[0]).distinct()
					.collect(Collectors.toList()));
		} else {
			// UserId spécifique
			recipients.add(recipientPattern);
		}

		return recipients;
	}

	// ✅ MÉTHODE SPÉCIALE POUR LES TESTS
	public void sendTestNotification(String userId, String message, String severity) {
		System.out.println("🧪 [SERVICE] Envoi notification de test à: " + userId);

		NotificationMessage testNotification = new NotificationMessage();
		testNotification.setCategory(NotificationCategory.SYSTEM_ALERT);
		testNotification.setSeverity(NotificationSeverity.valueOf(severity));
		testNotification.setTitle("Notification de Test");
		testNotification.setMessage(message);
		testNotification.setRecipient(userId);

		sendNotification(testNotification);
	}

	// === MÉTHODES UTILITAIRES POUR ÉVÉNEMENTS ===

	public void notifyReservationEvent(String reservationId, String userId, String eventType,
			Map<String, Object> data) {
		NotificationMessage notification = createNotificationFromEvent("reservation", eventType, userId, data);
		sendNotification(notification);
	}

	public void notifyPaymentEvent(String paymentId, String userId, String eventType, Map<String, Object> data) {
		NotificationMessage notification = createNotificationFromEvent("payment", eventType, userId, data);
		sendNotification(notification);
	}

	public void notifySystemEvent(String eventType, String message, NotificationSeverity severity) {
		NotificationMessage notification = new NotificationMessage();
		notification.setCategory(NotificationCategory.SYSTEM_ALERT);
		notification.setSeverity(severity);
		notification.setTitle("Événement système");
		notification.setMessage(message);
		notification.setRecipient("all");
		notification.getMetadata().put("systemEvent", eventType);

		sendNotification(notification);
	}

	private NotificationMessage createNotificationFromEvent(String entityType, String eventType, String userId,
			Map<String, Object> data) {
		NotificationMessage notification = new NotificationMessage();

		switch (entityType + "_" + eventType) {
		case "reservation_created":
			notification.setCategory(NotificationCategory.USER_ACTION);
			notification.setSeverity(NotificationSeverity.SUCCESS);
			notification.setTitle("Réservation créée");
			notification.setMessage("Votre réservation a été créée avec succès");
			break;
		case "payment_failed":
			notification.setCategory(NotificationCategory.USER_ACTION);
			notification.setSeverity(NotificationSeverity.ERROR);
			notification.setTitle("Paiement échoué");
			notification.setMessage("Le traitement de votre paiement a échoué");
			break;
		default:
			notification.setCategory(NotificationCategory.USER_ACTION);
			notification.setSeverity(NotificationSeverity.INFO);
			notification.setTitle("Événement " + entityType);
			notification.setMessage("Événement " + eventType + " pour " + entityType);
		}

		notification.setRecipient(userId);
		if (data != null) {
			// Convertir chaque valeur en String
			for (Map.Entry<String, Object> entry : data.entrySet()) {
				notification.getMetadata().put(entry.getKey(),
						entry.getValue() != null ? entry.getValue().toString() : null);
			}
		}
		notification.getMetadata().put("entityType", entityType);
		notification.getMetadata().put("eventType", eventType);

		return notification;
	}

	// === MÉTHODES POUR LE CONTROLLER (CELLES QUI MANQUAIENT) ===

	/**
	 * Récupérer les notifications d'un utilisateur avec filtres
	 */
	public List<NotificationMessage> getUserNotifications(String userId, String category, String severity) {
		System.out.println("📋 [SERVICE] Récupération notifications pour userId: " + userId + ", category: " + category
				+ ", severity: " + severity);

		if (category != null && severity != null) {
			try {
				NotificationCategory catEnum = NotificationCategory.valueOf(category.toUpperCase());
				NotificationSeverity sevEnum = NotificationSeverity.valueOf(severity.toUpperCase());
				return notificationRepository.findByUserIdAndCategoryAndSeverity(userId, catEnum, sevEnum);
			} catch (IllegalArgumentException e) {
				System.err.println("❌ [SERVICE] Catégorie ou sévérité invalide: " + e.getMessage());
				return new ArrayList<>();
			}
		} else if (category != null) {
			try {
				NotificationCategory catEnum = NotificationCategory.valueOf(category.toUpperCase());
				return notificationRepository.findByUserIdAndCategory(userId, catEnum);
			} catch (IllegalArgumentException e) {
				System.err.println("❌ [SERVICE] Catégorie invalide: " + e.getMessage());
				return new ArrayList<>();
			}
		} else if (severity != null) {
			try {
				NotificationSeverity sevEnum = NotificationSeverity.valueOf(severity.toUpperCase());
				return notificationRepository.findByUserIdAndSeverity(userId, sevEnum);
			} catch (IllegalArgumentException e) {
				System.err.println("❌ [SERVICE] Sévérité invalide: " + e.getMessage());
				return new ArrayList<>();
			}
		} else {
			return notificationRepository.findByUserId(userId);
		}
	}

	/**
	 * Compter les notifications non lues
	 */
	public long getUnreadCount(String userId) {
		System.out.println("🔢 [SERVICE] Comptage notifications non lues pour userId: " + userId);
		try {
			long count = notificationRepository.countUnreadByUserId(userId);
			System.out.println("📊 [SERVICE] Nombre de notifications non lues: " + count);
			return count;
		} catch (Exception e) {
			System.err.println("❌ [SERVICE] Erreur comptage non lues: " + e.getMessage());
			return 0L;
		}
	}

	/**
	 * Marquer une notification comme lue
	 */
	public void markAsRead(String notificationId, String userId) {
		System.out
				.println("📖 [SERVICE] Marquer comme lue - notificationId: " + notificationId + ", userId: " + userId);
		try {
			notificationRepository.markAsRead(notificationId, userId);
			System.out.println("✅ [SERVICE] Notification marquée comme lue");
		} catch (Exception e) {
			System.err.println("❌ [SERVICE] Erreur marquer comme lue: " + e.getMessage());
			throw new RuntimeException("Erreur lors du marquage comme lu: " + e.getMessage());
		}
	}

	/**
	 * Marquer toutes les notifications comme lues
	 */
	public void markAllAsRead(String userId) {
		System.out.println("📚 [SERVICE] Marquer toutes comme lues - userId: " + userId);
		try {
			notificationRepository.markAllAsRead(userId);
			System.out.println("✅ [SERVICE] Toutes les notifications marquées comme lues");
		} catch (Exception e) {
			System.err.println("❌ [SERVICE] Erreur marquer toutes comme lues: " + e.getMessage());
			throw new RuntimeException("Erreur lors du marquage de toutes les notifications: " + e.getMessage());
		}
	}

	/**
	 * Supprimer une notification
	 */
	public void deleteNotification(String notificationId, String userId) {
		System.out.println(
				"🗑️ [SERVICE] Suppression notification - notificationId: " + notificationId + ", userId: " + userId);
		try {
			// Vérifier que l'utilisateur est propriétaire de la notification
			NotificationMessage notification = notificationRepository.findById(notificationId)
					.orElseThrow(() -> new RuntimeException("Notification non trouvée"));

			if (!notification.getRecipient().equals(userId)) {
				throw new RuntimeException("Non autorisé à supprimer cette notification");
			}

			notificationRepository.deleteById(notificationId);
			System.out.println("✅ [SERVICE] Notification supprimée");
		} catch (Exception e) {
			System.err.println("❌ [SERVICE] Erreur suppression: " + e.getMessage());
			throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
		}
	}

	/**
	 * Nettoyer les notifications expirées
	 */
	@Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
	public void cleanExpiredNotifications() {
		System.out.println("🧹 [SERVICE] Nettoyage des notifications expirées");
		try {
			notificationRepository.deleteExpiredNotifications();
			System.out.println("✅ [SERVICE] Nettoyage terminé");
		} catch (Exception e) {
			System.err.println("❌ [SERVICE] Erreur nettoyage: " + e.getMessage());
		}
	}

	// ✅ MÉTHODE POUR DÉBOGUAGE
	public Map<String, SseEmitter> getUserEmitters() {
		return userEmitters;
	}
}