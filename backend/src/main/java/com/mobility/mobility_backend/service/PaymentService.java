package com.mobility.mobility_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.payment.PaymentSessionRequest;
import com.mobility.mobility_backend.dto.payment.PaymentSessionResponse;
import com.mobility.mobility_backend.dto.payment.PaymentStatusResponse;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
@Transactional
public class PaymentService {

	private final ReservationRepository reservationRepository;
	private final PaymentNotificationService paymentNotificationService;

	@Value("${stripe.secret.key:}")
	private String stripeSecretKey;

	@Value("${app.payment.success-url:http://localhost:4200/payments/success}")
	private String defaultSuccessUrl;

	@Value("${app.payment.cancel-url:http://localhost:4200/payments/cancel}")
	private String defaultCancelUrl;

	public PaymentService(ReservationRepository reservationRepository,
			PaymentNotificationService paymentNotificationService) {
		this.reservationRepository = reservationRepository;
		this.paymentNotificationService = paymentNotificationService;
	}

	public PaymentSessionResponse createCheckoutSession(PaymentSessionRequest request) throws StripeException {
		if (request.getReservationId() == null) {
			throw new IllegalArgumentException("reservationId obligatoire");
		}
		Reservation reservation = reservationRepository.findById(request.getReservationId())
				.orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));
		Reservation.PaymentStatus previousStatus = reservation.getPaymentStatus();

		if (reservation.getOffer() == null || reservation.getOffer().getPrice() == null) {
			throw new IllegalStateException("Impossible de créer un paiement pour une offre sans prix");
		}

		if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
			throw new IllegalStateException("stripe.secret.key non configuré");
		}

		Stripe.apiKey = stripeSecretKey;

		BigDecimal price = reservation.getOffer().getPrice();
		long amount = price.multiply(BigDecimal.valueOf(100)).longValue();

		String successUrl = (request.getSuccessUrl() == null || request.getSuccessUrl().isBlank()) ? defaultSuccessUrl
				: request.getSuccessUrl();
		String cancelUrl = (request.getCancelUrl() == null || request.getCancelUrl().isBlank()) ? defaultCancelUrl
				: request.getCancelUrl();

		SessionCreateParams params = SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(successUrl)
				.setCancelUrl(cancelUrl)
				.setClientReferenceId(reservation.getReservationId().toString())
				.setCustomerEmail(reservation.getUser() != null ? reservation.getUser().getEmail() : null)
				.addLineItem(SessionCreateParams.LineItem.builder()
						.setQuantity(1L)
						.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
								.setCurrency("eur")
								.setUnitAmount(amount)
								.setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
										.setName(reservation.getOffer().getDescription())
										.build())
								.build())
						.build())
				.build();

		Session session = Session.create(params);

		reservation.setPaymentStatus(Reservation.PaymentStatus.REQUIRES_ACTION);
		reservation.setPaymentReference(session.getId());
		reservation.setUpdatedAt(java.time.LocalDateTime.now());
		reservationRepository.save(reservation);

		if (previousStatus == Reservation.PaymentStatus.EXPIRED
				|| previousStatus == Reservation.PaymentStatus.FAILED) {
			paymentNotificationService.notifyPaymentRetryRequested(reservation);
		}

		return new PaymentSessionResponse(session.getId(), session.getUrl());
	}

	public PaymentStatusResponse syncPaymentStatus(Integer reservationId) throws StripeException {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

		if (reservation.getPaymentReference() == null || reservation.getPaymentReference().isBlank()) {
			throw new IllegalStateException("Aucune référence de paiement à synchroniser");
		}

		if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
			throw new IllegalStateException("stripe.secret.key non configuré");
		}

		Stripe.apiKey = stripeSecretKey;

		Session session = Session.retrieve(reservation.getPaymentReference());
		String stripeStatus = session.getPaymentStatus();
		boolean updated = false;

		if ("paid".equalsIgnoreCase(stripeStatus)
				&& reservation.getPaymentStatus() != Reservation.PaymentStatus.PAID) {
			LocalDateTime now = LocalDateTime.now();
			reservation.setPaymentStatus(Reservation.PaymentStatus.PAID);
			reservation.setPaymentDate(now);
			reservation.setUpdatedAt(now);
			if (reservation.getPaymentAmount() == null && reservation.getOffer() != null) {
				reservation.setPaymentAmount(reservation.getOffer().getPrice());
			}
			reservationRepository.save(reservation);
			updated = true;
		}

		String paymentStatus = reservation.getPaymentStatus() != null ? reservation.getPaymentStatus().name() : null;
		return new PaymentStatusResponse(reservationId, paymentStatus, stripeStatus, updated);
	}
}
