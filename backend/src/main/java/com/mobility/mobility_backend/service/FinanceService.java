package com.mobility.mobility_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.finance.FinanceOverviewDTO;
import com.mobility.mobility_backend.dto.finance.MonthlyRevenuePointDTO;
import com.mobility.mobility_backend.dto.finance.PaymentAlertDTO;
import com.mobility.mobility_backend.dto.finance.PaymentEventDTO;
import com.mobility.mobility_backend.dto.finance.PaymentStatusBreakdownDTO;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.PaymentEventLog;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.repository.PaymentEventLogRepository;

@Service
public class FinanceService {

    private final ReservationRepository reservationRepository;
    private final PaymentEventLogRepository paymentEventLogRepository;

    public FinanceService(ReservationRepository reservationRepository,
		PaymentEventLogRepository paymentEventLogRepository) {
	this.reservationRepository = reservationRepository;
	this.paymentEventLogRepository = paymentEventLogRepository;
    }

	public FinanceOverviewDTO getFinanceOverview(int months) {
		List<Reservation> reservations = reservationRepository.findAll();
		FinanceOverviewDTO overview = new FinanceOverviewDTO();

		double totalRevenue = reservations.stream().filter(this::isPaid)
				.mapToDouble(this::resolveReservationAmount).sum();
		double mtdRevenue = reservations.stream().filter(this::isPaid)
				.filter(r -> isInMonth(r.getPaymentDate(), YearMonth.now()))
				.mapToDouble(this::resolveReservationAmount).sum();
		double outstanding = reservations.stream().filter(this::isOutstanding)
				.mapToDouble(this::resolveReservationAmount).sum();

		overview.setTotalRevenue(round(totalRevenue));
		overview.setMonthToDateRevenue(round(mtdRevenue));
		overview.setOutstandingRevenue(round(outstanding));

		overview.setPaymentsByStatus(buildStatusBreakdown(reservations));
		overview.setRevenueHistory(buildHistory(reservations, Math.max(1, Math.min(months, 12))));
		overview.setAlerts(buildAlerts());

		return overview;
	}

    public List<PaymentAlertDTO> getCurrentAlerts() {
	return buildAlerts();
    }

    public List<PaymentEventDTO> getRecentPaymentEvents(int size) {
	int limit = size > 0 ? Math.min(size, 50) : 20;
	List<PaymentEventLog> logs;
	if (limit == 20) {
		logs = paymentEventLogRepository.findTop20ByOrderByReceivedAtDesc();
	} else {
		logs = paymentEventLogRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit,
				org.springframework.data.domain.Sort.by("receivedAt").descending())).getContent();
	}
	return logs.stream().map(log -> new PaymentEventDTO(log.getId(), log.getEventId(), log.getReservationReference(),
			log.getType(), log.getStatus(), log.getErrorMessage(), log.getReceivedAt()))
		.collect(Collectors.toList());
    }

	public String buildFinanceCsv(int months) {
		FinanceOverviewDTO overview = getFinanceOverview(months);
		StringBuilder builder = new StringBuilder();
		builder.append("Statut,Nombre,Montant\n");
		overview.getPaymentsByStatus()
				.forEach(item -> builder.append(item.getStatus()).append(',')
						.append(item.getCount()).append(',')
						.append(item.getAmount()).append('\n'));
		builder.append("\nRevenus mensuels\nMois,Paiements,Montant\n");
		overview.getRevenueHistory().forEach(point -> builder.append(point.getLabel()).append(',')
				.append(point.getPayments()).append(',')
				.append(point.getRevenue()).append('\n'));
		return builder.toString();
	}

	private List<PaymentStatusBreakdownDTO> buildStatusBreakdown(List<Reservation> reservations) {
		Map<Reservation.PaymentStatus, PaymentStatusBreakdownDTO> map = new EnumMap<>(Reservation.PaymentStatus.class);
		for (Reservation.PaymentStatus status : Reservation.PaymentStatus.values()) {
			map.put(status, new PaymentStatusBreakdownDTO(status.name(), 0, 0));
		}

		reservations.forEach(res -> {
			Reservation.PaymentStatus status = res.getPaymentStatus() != null ? res.getPaymentStatus()
					: Reservation.PaymentStatus.PENDING;
			PaymentStatusBreakdownDTO dto = map.get(status);
			dto.setCount(dto.getCount() + 1);
			dto.setAmount(round(dto.getAmount() + resolveReservationAmount(res)));
		});

		return new ArrayList<>(map.values());
	}

	private List<MonthlyRevenuePointDTO> buildHistory(List<Reservation> reservations, int months) {
		YearMonth current = YearMonth.now();
		Map<YearMonth, MonthlyRevenuePointDTO> points = new java.util.LinkedHashMap<>();
		for (int i = months - 1; i >= 0; i--) {
			YearMonth ym = current.minusMonths(i);
			String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + ym.getYear();
			points.put(ym, new MonthlyRevenuePointDTO(label, 0, 0));
		}

		reservations.stream().filter(this::isPaid).forEach(res -> {
			if (res.getPaymentDate() == null) {
				return;
			}
			YearMonth month = YearMonth.from(res.getPaymentDate());
			if (!points.containsKey(month)) {
				return;
			}
			MonthlyRevenuePointDTO dto = points.get(month);
			dto.setPayments(dto.getPayments() + 1);
			dto.setRevenue(round(dto.getRevenue() + resolveReservationAmount(res)));
		});

		return new ArrayList<>(points.values());
	}

	private List<PaymentAlertDTO> buildAlerts() {
		List<Reservation.PaymentStatus> statuses = List.of(Reservation.PaymentStatus.PENDING,
				Reservation.PaymentStatus.REQUIRES_ACTION, Reservation.PaymentStatus.FAILED);
		List<Reservation> candidates = reservationRepository.findByPaymentStatusIn(statuses);
		LocalDateTime now = LocalDateTime.now();

		return candidates.stream().sorted((a, b) -> a.getReservationDate().compareTo(b.getReservationDate()))
				.limit(10)
				.map(res -> {
					String severity = res.getPaymentStatus() == Reservation.PaymentStatus.FAILED ? "CRITIQUE"
							: "ALERTE";
					String message = buildAlertMessage(res);
					return new PaymentAlertDTO(res.getReservationId(),
							res.getUser() != null ? res.getUser().getUsername() : "Client",
							round(resolveReservationAmount(res)),
							res.getPaymentStatus() != null ? res.getPaymentStatus().name() : "PENDING", severity,
							message, res.getReservationDate());
				}).collect(Collectors.toList());
	}

	private String buildAlertMessage(Reservation reservation) {
		switch (reservation.getPaymentStatus()) {
		case FAILED:
			return "Paiement refusé - intervention nécessaire";
		case REQUIRES_ACTION:
			return "Action requise du client pour confirmer le paiement";
		default:
			LocalDate date = reservation.getReservationDate() != null ? reservation.getReservationDate().toLocalDate()
					: LocalDate.now();
			return "Paiement en attente pour la réservation du " + date;
		}
	}

	private boolean isPaid(Reservation reservation) {
		if (reservation.getPaymentStatus() == Reservation.PaymentStatus.PAID
				|| reservation.getPaymentStatus() == Reservation.PaymentStatus.REFUNDED) {
			return true;
		}
		return reservation.getStatus() == Reservation.ReservationStatus.COMPLETED
				|| reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED;
	}

	private boolean isOutstanding(Reservation reservation) {
		if (reservation.getPaymentStatus() == Reservation.PaymentStatus.FAILED) {
			return true;
		}
		return reservation.getPaymentStatus() == Reservation.PaymentStatus.PENDING
				|| reservation.getPaymentStatus() == Reservation.PaymentStatus.REQUIRES_ACTION;
	}

	private boolean isInMonth(LocalDateTime paymentDate, YearMonth target) {
		if (paymentDate == null) {
			return false;
		}
		YearMonth month = YearMonth.from(paymentDate);
		return month.equals(target);
	}

	private double resolveReservationAmount(Reservation reservation) {
		BigDecimal amount = reservation.getPaymentAmount();
		if (amount == null && reservation.getOffer() != null) {
			amount = reservation.getOffer().getPrice();
		}
		return amount != null ? amount.doubleValue() : 0;
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
