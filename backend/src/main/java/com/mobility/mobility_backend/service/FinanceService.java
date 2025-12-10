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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.BookingStatsDTO;
import com.mobility.mobility_backend.dto.finance.FinanceOverviewDTO;
import com.mobility.mobility_backend.dto.finance.MonthlyRevenuePointDTO;
import com.mobility.mobility_backend.dto.finance.OutstandingPointDTO;
import com.mobility.mobility_backend.dto.finance.PaymentAlertDTO;
import com.mobility.mobility_backend.dto.finance.PaymentEventDTO;
import com.mobility.mobility_backend.dto.finance.PaymentStatusBreakdownDTO;
import com.mobility.mobility_backend.dto.finance.PeriodAmountProjection;
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
		FinanceOverviewDTO overview = new FinanceOverviewDTO();
		YearMonth currentMonth = YearMonth.now();
		LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();

		BigDecimal totalRevenue = reservationRepository.sumPaidAmount();
		BigDecimal monthRevenue = reservationRepository.sumPaidAmountSince(monthStart);
		BigDecimal outstandingRevenue = reservationRepository.sumOutstandingAmount();

		overview.setTotalRevenue(round(totalRevenue.doubleValue()));
		overview.setMonthToDateRevenue(round(monthRevenue.doubleValue()));
		overview.setOutstandingRevenue(round(outstandingRevenue.doubleValue()));

		long confirmed = reservationRepository.countByStatus(Reservation.ReservationStatus.CONFIRMED)
				+ reservationRepository.countByStatus(Reservation.ReservationStatus.COMPLETED);
		long total = reservationRepository.count();
		double confirmationRate = total == 0 ? 0 : (double) confirmed / total;
		overview.setConfirmationRate(confirmationRate);

		List<Reservation> reservations = reservationRepository.findAll();
		overview.setPaymentsByStatus(buildStatusBreakdown(reservations));
		overview.setRevenueHistory(buildHistory(reservations, Math.max(1, Math.min(months, 12))));
		overview.setAlerts(getAlerts(null, null, null, null, null, false, 10));

		LocalDateTime eightWeeksAgo = LocalDateTime.now().minusWeeks(8);
		LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
		overview.setOutstandingByWeek(mapOutstandingPoints(reservationRepository.findOutstandingByWeek(eightWeeksAgo)));
		overview.setOutstandingByMonth(mapOutstandingPoints(reservationRepository.findOutstandingByMonth(sixMonthsAgo)));

		return overview;
	}

	public BookingStatsDTO getBookingStatsSummary() {
		BookingStatsDTO stats = new BookingStatsDTO();
		long totalBookings = reservationRepository.count();
		long pending = reservationRepository.countByStatus(Reservation.ReservationStatus.PENDING);
		long confirmed = reservationRepository.countByStatus(Reservation.ReservationStatus.CONFIRMED);
		long cancelled = reservationRepository.countByStatus(Reservation.ReservationStatus.CANCELLED);
		long completed = reservationRepository.countByStatus(Reservation.ReservationStatus.COMPLETED);

		stats.setTotalBookings(totalBookings);
		stats.setPendingBookings(pending);
		stats.setConfirmedBookings(confirmed);
		stats.setCancelledBookings(cancelled);
		stats.setCompletedBookings(completed);
		stats.setTotalRevenue(round(reservationRepository.sumPaidAmount().doubleValue()));
		stats.setOutstandingRevenue(round(reservationRepository.sumOutstandingAmount().doubleValue()));

		YearMonth currentMonth = YearMonth.now();
		LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
		stats.setMonthToDateRevenue(round(reservationRepository.sumPaidAmountSince(monthStart).doubleValue()));

		long confirmedOrCompleted = confirmed + completed;
		double confirmationRate = totalBookings == 0 ? 0 : (double) confirmedOrCompleted / totalBookings;
		stats.setConfirmationRate(confirmationRate);

		return stats;
	}

	public List<PaymentAlertDTO> getCurrentAlerts() {
		return getAlerts(null, null, null, null, null, false, 10);
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

	public List<PaymentEventDTO> getReservationPaymentEvents(Integer reservationId) {
		if (reservationId == null) {
			return List.of();
		}
		List<PaymentEventLog> logs = paymentEventLogRepository
				.findTop20ByReservationReferenceOrderByReceivedAtDesc(String.valueOf(reservationId));
		return logs.stream()
				.map(log -> new PaymentEventDTO(log.getId(), log.getEventId(), log.getReservationReference(),
						log.getType(), log.getStatus(), log.getErrorMessage(), log.getReceivedAt()))
				.collect(Collectors.toList());
	}
	
	
	
	/*gère désormais les alertes via une méthode paramétrable (getAlerts) capable de filtrer par sévérité, 
	 * statut, texte, période et exigence d’action, avec limite configurable et CSV basé sur les résultats filtrés. 
	 * L’overview tire aussi parti de cette méthode afin de présenter les mêmes données que l’API d’alertes.*/

	public List<PaymentAlertDTO> getAlerts(String severity,
			List<String> statuses,
			String search,
			LocalDate startDate,
			LocalDate endDate,
			boolean actionRequiredOnly,
			int limit) {

		List<Reservation.PaymentStatus> statusFilter = resolveStatusFilter(statuses);
		List<Reservation> candidates = reservationRepository.findByPaymentStatusIn(statusFilter);
		LocalDateTime from = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime to = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
		String severityFilter = severity != null && !severity.isBlank() ? severity.trim().toUpperCase(Locale.ROOT)
				: null;
		String needle = search != null ? search.trim().toLowerCase(Locale.ROOT) : null;
		int effectiveLimit = limit > 0 ? Math.min(limit, 200) : 20;

		return candidates.stream()
				.filter(res -> from == null || (res.getReservationDate() != null
						&& !res.getReservationDate().isBefore(from)))
				.filter(res -> to == null || (res.getReservationDate() != null
						&& res.getReservationDate().isBefore(to)))
				.sorted(Comparator.comparing(Reservation::getReservationDate,
						Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
				.map(this::toAlert)
				.filter(alert -> severityFilter == null
						|| severityFilter.equalsIgnoreCase(alert.getSeverity()))
				.filter(alert -> !actionRequiredOnly || isActionRequired(alert.getPaymentStatus()))
				.filter(alert -> {
					if (needle == null || needle.isBlank()) {
						return true;
					}
					return (alert.getCustomer() != null && alert.getCustomer().toLowerCase(Locale.ROOT).contains(needle))
							|| (alert.getMessage() != null
									&& alert.getMessage().toLowerCase(Locale.ROOT).contains(needle))
							|| (alert.getPaymentStatus() != null
									&& alert.getPaymentStatus().toLowerCase(Locale.ROOT).contains(needle))
							|| (alert.getReservationId() != null
									&& String.valueOf(alert.getReservationId()).contains(needle));
				})
				.limit(effectiveLimit)
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

	public String buildAlertsCsv(List<PaymentAlertDTO> alerts) {
		StringBuilder builder = new StringBuilder();
		builder.append("Reservation,Client,Montant,Statut,Gravite,Message,Date\n");
		alerts.forEach(alert -> builder
				.append(alert.getReservationId()).append(',')
				.append(escapeCsv(alert.getCustomer())).append(',')
				.append(alert.getAmount()).append(',')
				.append(alert.getPaymentStatus()).append(',')
				.append(alert.getSeverity()).append(',')
				.append(escapeCsv(alert.getMessage())).append(',')
				.append(alert.getReservationDate()).append('\n'));
		return builder.toString();
	}

	private String escapeCsv(String value) {
		if (value == null) {
			return "";
		}
		String sanitized = value.replace("\"", "\"\"");
		if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n")) {
			return "\"" + sanitized + "\"";
		}
		return sanitized;
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

	private List<Reservation.PaymentStatus> resolveStatusFilter(List<String> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return List.of(Reservation.PaymentStatus.PENDING, Reservation.PaymentStatus.REQUIRES_ACTION,
					Reservation.PaymentStatus.FAILED);
		}
		List<Reservation.PaymentStatus> parsed = statuses.stream()
				.map(this::parsePaymentStatus)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		if (parsed.isEmpty()) {
			return List.of(Reservation.PaymentStatus.PENDING, Reservation.PaymentStatus.REQUIRES_ACTION,
					Reservation.PaymentStatus.FAILED);
		}
		return parsed;
	}

	private Reservation.PaymentStatus parsePaymentStatus(String raw) {
		if (raw == null) {
			return null;
		}
		try {
			return Reservation.PaymentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private PaymentAlertDTO toAlert(Reservation reservation) {
		String severity = reservation.getPaymentStatus() == Reservation.PaymentStatus.FAILED ? "CRITIQUE" : "ALERTE";
		String message = buildAlertMessage(reservation);
		return new PaymentAlertDTO(reservation.getReservationId(),
				reservation.getUser() != null ? reservation.getUser().getUsername() : "Client",
				round(resolveReservationAmount(reservation)),
				reservation.getPaymentStatus() != null ? reservation.getPaymentStatus().name() : "PENDING", severity,
				message, reservation.getReservationDate());
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

	private List<OutstandingPointDTO> mapOutstandingPoints(List<PeriodAmountProjection> projections) {
		if (projections == null || projections.isEmpty()) {
			return List.of();
		}
		return projections.stream()
				.map(point -> new OutstandingPointDTO(point.getPeriod(),
						point.getCount() != null ? point.getCount() : 0L,
						point.getAmount() != null ? point.getAmount() : BigDecimal.ZERO))
				.collect(Collectors.toList());
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

	private boolean isActionRequired(String paymentStatus) {
		if (paymentStatus == null) {
			return false;
		}
		String normalized = paymentStatus.toUpperCase(Locale.ROOT);
		return normalized.equals("FAILED") || normalized.equals("REQUIRES_ACTION") || normalized.equals("PENDING")
				|| normalized.equals("EXPIRED");
	}
}
