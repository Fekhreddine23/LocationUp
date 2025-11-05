package com.mobility.mobility_backend.dto.socket;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "notifications")
public class NotificationMessage {

    @Id
    private String id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private NotificationCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private NotificationSeverity severity;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", length = 1000)
    private String message;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "recipient", nullable = false)
    private String recipient;
    
    @Column(name = "sender")
    private String sender;
    
    @ElementCollection
    @CollectionTable(name = "notification_metadata", joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", length = 1000)
    private Map<String, String> metadata = new HashMap<>(); // ✅ Changé en Map<String, String>
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "action_url")
    private String actionUrl;
    
    @Column(name = "action_label")
    private String actionLabel;
    
    @Column(name = "dismissible")
    private boolean dismissible = true;
    
    @Column(name = "is_read")
    private boolean read = false;

    // === CONSTRUCTEURS ===
    public NotificationMessage() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    // === FACTORY METHODS CORRIGÉES ===
    
    public static NotificationMessage reservationConfirmed(String reservationId, String userId) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.USER_ACTION);
        msg.setSeverity(NotificationSeverity.SUCCESS);
        msg.setTitle("Réservation confirmée");
        msg.setMessage("Votre réservation #" + reservationId + " a été confirmée");
        msg.setRecipient(userId);
        msg.setActionUrl("/reservations/" + reservationId);
        msg.setActionLabel("Voir la réservation");
        msg.getMetadata().put("reservationId", reservationId);
        msg.getMetadata().put("entityType", "reservation");
        return msg;
    }
    
    public static NotificationMessage reservationCancelled(String reservationId, String userId, String reason) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.USER_ACTION);
        msg.setSeverity(NotificationSeverity.WARNING);
        msg.setTitle("Réservation annulée");
        msg.setMessage("Votre réservation #" + reservationId + " a été annulée");
        msg.setDescription("Raison : " + reason);
        msg.setRecipient(userId);
        msg.getMetadata().put("reservationId", reservationId);
        msg.getMetadata().put("cancellationReason", reason);
        return msg;
    }
    
    public static NotificationMessage paymentSuccess(String reservationId, String userId, double amount) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.USER_ACTION);
        msg.setSeverity(NotificationSeverity.SUCCESS);
        msg.setTitle("Paiement réussi");
        msg.setMessage("Paiement de " + amount + "€ confirmé pour la réservation #" + reservationId);
        msg.setRecipient(userId);
        msg.getMetadata().put("reservationId", reservationId);
        msg.getMetadata().put("amount", String.valueOf(amount)); // ✅ Convertir en String
        msg.getMetadata().put("paymentStatus", "completed");
        return msg;
    }
    
    public static NotificationMessage paymentFailed(String reservationId, String userId, String error) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.USER_ACTION);
        msg.setSeverity(NotificationSeverity.ERROR);
        msg.setTitle("Échec du paiement");
        msg.setMessage("Le paiement pour la réservation #" + reservationId + " a échoué");
        msg.setDescription("Erreur : " + error);
        msg.setRecipient(userId);
        msg.setActionUrl("/payment/retry/" + reservationId);
        msg.setActionLabel("Réessayer");
        msg.getMetadata().put("reservationId", reservationId);
        msg.getMetadata().put("error", error);
        return msg;
    }
    
    public static NotificationMessage newOffer(String offerId, String title, String targetAudience) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.BUSINESS_EVENT);
        msg.setSeverity(NotificationSeverity.INFO);
        msg.setTitle("Nouvelle offre disponible");
        msg.setMessage(title);
        msg.setRecipient(targetAudience);
        msg.setActionUrl("/offers/" + offerId);
        msg.setActionLabel("Découvrir l'offre");
        msg.getMetadata().put("offerId", offerId);
        msg.getMetadata().put("promotionType", "new_offer");
        return msg;
    }
    
    public static NotificationMessage systemMaintenance(LocalDateTime start, LocalDateTime end) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.SYSTEM_ALERT);
        msg.setSeverity(NotificationSeverity.WARNING);
        msg.setTitle("Maintenance planifiée");
        msg.setMessage("Une maintenance est prévue le " + start.toLocalDate());
        msg.setDescription("Le système sera indisponible de " + start.toLocalTime() + " à " + end.toLocalTime());
        msg.setRecipient("all");
        msg.setDismissible(false);
        msg.getMetadata().put("maintenanceStart", start.toString()); // ✅ Convertir en String
        msg.getMetadata().put("maintenanceEnd", end.toString());     // ✅ Convertir en String
        return msg;
    }
    
    public static NotificationMessage adminBroadcast(String title, String message, String severity) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.ADMIN_MESSAGE);
        msg.setSeverity(NotificationSeverity.valueOf(severity));
        msg.setTitle(title);
        msg.setMessage(message);
        msg.setRecipient("all");
        msg.setSender("admin");
        return msg;
    }
    
    public static NotificationMessage securityAlert(String userId, String event) {
        NotificationMessage msg = new NotificationMessage();
        msg.setCategory(NotificationCategory.SECURITY_ALERT);
        msg.setSeverity(NotificationSeverity.URGENT);
        msg.setTitle("Alerte de sécurité");
        msg.setMessage("Activité suspecte détectée sur votre compte");
        msg.setRecipient(userId);
        msg.setActionUrl("/security/check");
        msg.setActionLabel("Vérifier la sécurité");
        msg.getMetadata().put("securityEvent", event);
        return msg;
    }

    // === GETTERS/SETTERS ===
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public NotificationCategory getCategory() { return category; }
    public void setCategory(NotificationCategory category) { this.category = category; }
    
    public NotificationSeverity getSeverity() { return severity; }
    public void setSeverity(NotificationSeverity severity) { this.severity = severity; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public Map<String, String> getMetadata() { return metadata; } // ✅ Changé en String
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; } // ✅ Changé en String
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    
    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }
    
    public boolean isDismissible() { return dismissible; }
    public void setDismissible(boolean dismissible) { this.dismissible = dismissible; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}