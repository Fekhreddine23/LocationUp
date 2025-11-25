package com.mobility.mobility_backend.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

	@GetMapping("/alerts")
	public ResponseEntity<List<PaymentAlertDTO>> getAlerts() {
		return ResponseEntity.ok(financeService.getCurrentAlerts());
	}

	@GetMapping("/events")
	public ResponseEntity<List<PaymentEventDTO>> getEvents(@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(financeService.getRecentPaymentEvents(size));
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> exportFinanceData(@RequestParam(defaultValue = "6") int months) {
		String csv = financeService.buildFinanceCsv(months);
		byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=finance-report.csv")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(bytes);
	}
}
