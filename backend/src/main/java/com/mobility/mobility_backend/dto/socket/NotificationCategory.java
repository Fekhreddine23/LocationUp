package com.mobility.mobility_backend.dto.socket;

public enum NotificationCategory {

	 USER_ACTION,    // Actions utilisateur (réservations, paiements, etc.)
     SYSTEM_ALERT,   // Alertes système (maintenance, erreurs)
     BUSINESS_EVENT, // Événements métier (nouvelles offres, promotions)
     ADMIN_MESSAGE,  // Messages administrateurs
     SECURITY_ALERT  // Alertes de sécurité

}
