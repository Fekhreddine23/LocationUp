package com.mobility.mobility_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.entity.ReservationAdminAction;
import com.mobility.mobility_backend.repository.ReservationAdminActionRepository;

@Service
public class ReservationAdminActionService {

	private final ReservationAdminActionRepository repository;

	public ReservationAdminActionService(ReservationAdminActionRepository repository) {
		this.repository = repository;
	}

	public void recordAction(Integer reservationId, String adminUsername, String actionType, String details) {
		ReservationAdminAction action = new ReservationAdminAction();
		action.setReservationId(reservationId);
		action.setAdminUsername(adminUsername != null ? adminUsername : "unknown");
		action.setActionType(actionType);
		action.setDetails(details);
		repository.save(action);
	}

	public List<ReservationAdminAction> getActions(Integer reservationId) {
		return repository.findTop50ByReservationIdOrderByCreatedAtDesc(reservationId);
	}
}
