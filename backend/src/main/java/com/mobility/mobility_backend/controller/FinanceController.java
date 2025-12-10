package com.mobility.mobility_backend.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.finance.FinanceOverviewDTO;
import com.mobility.mobility_backend.dto.finance.PaymentAlertDTO;
import com.mobility.mobility_backend.dto.finance.PaymentEventDTO;
import com.mobility.mobility_backend.service.FinanceService;

@RestController
@RequestMapping("/api/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class FinanceController {

	private final FinanceService financeService;

	public FinanceController(FinanceService financeService) {
		this.financeService = financeService;
	}

	@GetMapping("/overview")
	public ResponseEntity<FinanceOverviewDTO> getOverview(@RequestParam(defaultValue = "6") int months) {
		return ResponseEntity.ok(financeService.getFinanceOverview(months));
	}

	
	/* expose ces filtres côté API (/finance/alerts et export CSV) via des query params
	 *  (sévérité, statuts multiples, recherche, dates, actionOnly, limite)  */
	@GetMapping("/alerts")
	public ResponseEntity<List<PaymentAlertDTO>> getAlerts(
			@RequestParam(required = false) String severity,
			@RequestParam(required = false) List<String> statuses,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "false") boolean actionRequiredOnly,
			@RequestParam(defaultValue = "20") int limit) {

		return ResponseEntity.ok(financeService.getAlerts(severity, statuses, search, startDate, endDate,
				actionRequiredOnly, limit));
	}

	@GetMapping("/events")
	public ResponseEntity<List<PaymentEventDTO>> getEvents(@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(financeService.getRecentPaymentEvents(size));
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> exportFinanceData(@RequestParam(defaultValue = "6") int months,
			@RequestParam(defaultValue = "overview") String type,
			@RequestParam(required = false) String severity,
			@RequestParam(required = false) List<String> statuses,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "false") boolean actionRequiredOnly,
			@RequestParam(defaultValue = "100") int limit) {
		String csv;
		String filename = "finance-report.csv";

		if ("alerts".equalsIgnoreCase(type)) {
			List<PaymentAlertDTO> alerts = financeService.getAlerts(severity, statuses, search, startDate, endDate,
					actionRequiredOnly, limit);
			csv = financeService.buildAlertsCsv(alerts);
			filename = "finance-alerts.csv";
		} else {
			csv = financeService.buildFinanceCsv(months);
		}

		byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(bytes);
	}
}
