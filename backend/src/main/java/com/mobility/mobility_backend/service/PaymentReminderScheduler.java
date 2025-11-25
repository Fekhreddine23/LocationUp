package com.mobility.mobility_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.repository.ReservationRepository;

@Component
public class PaymentReminderScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentReminderScheduler.class);

	private final ReservationRepository reservationRepository;
	private final PaymentNotificationService notificationService;
	private final long paymentActionTimeoutMinutes;

	public PaymentReminderScheduler(ReservationRepository reservationRepository,
			PaymentNotificationService notificationService,
			@Value("${app.payment.action-timeout-minutes:120}") long paymentActionTimeoutMinutes) {
		this.reservationRepository = reservationRepository;
		this.notificationService = notificationService;
		this.paymentActionTimeoutMinutes = paymentActionTimeoutMinutes;
	}

	@Scheduled(cron = "0 0 * * * *")
	public void sendReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime in24h = now.plusHours(24);
		List<Reservation> upcoming = reservationRepository
				.findByPaymentStatusAndReservationDateBetween(Reservation.PaymentStatus.PENDING, now, in24h);
		upcoming.forEach(notificationService::notifyUpcomingPayment);
	}

	@Scheduled(cron = "0 */15 * * * *")
	public void expireStalePayments() {
		if (paymentActionTimeoutMinutes <= 0) {
			return;
		}
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(paymentActionTimeoutMinutes);
		List<Reservation> staleReservations = reservationRepository
				.findByPaymentStatusAndUpdatedAtBefore(Reservation.PaymentStatus.REQUIRES_ACTION, threshold);

		if (staleReservations.isEmpty()) {
			return;
		}

		LOGGER.info("Expiring {} reservations stuck in REQUIRES_ACTION since before {}", staleReservations.size(),
				threshold);
		LocalDateTime now = LocalDateTime.now();
		staleReservations.forEach(reservation -> {
			reservation.setPaymentStatus(Reservation.PaymentStatus.FAILED);
			reservation.setUpdatedAt(now);
			reservationRepository.save(reservation);
			notificationService.notifyPaymentFailure(reservation,
					"Paiement expiré : la session Stripe n'a pas été finalisée à temps.");
		});
	}
}
