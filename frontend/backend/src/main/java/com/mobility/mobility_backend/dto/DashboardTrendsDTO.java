package com.mobility.mobility_backend.dto;

import java.util.List;

public class DashboardTrendsDTO {
	private List<MonthlyReservationStatDTO> reservationsByMonth;
	private List<CategoryStatDTO> offersByCategory;
	private List<CityStatDTO> topPickupCities;

	public DashboardTrendsDTO() {
	}

	public DashboardTrendsDTO(List<MonthlyReservationStatDTO> reservationsByMonth, List<CategoryStatDTO> offersByCategory,
			List<CityStatDTO> topPickupCities) {
		this.reservationsByMonth = reservationsByMonth;
		this.offersByCategory = offersByCategory;
		this.topPickupCities = topPickupCities;
	}

	public List<MonthlyReservationStatDTO> getReservationsByMonth() {
		return reservationsByMonth;
	}

	public void setReservationsByMonth(List<MonthlyReservationStatDTO> reservationsByMonth) {
		this.reservationsByMonth = reservationsByMonth;
	}

	public List<CategoryStatDTO> getOffersByCategory() {
		return offersByCategory;
	}

	public void setOffersByCategory(List<CategoryStatDTO> offersByCategory) {
		this.offersByCategory = offersByCategory;
	}

	public List<CityStatDTO> getTopPickupCities() {
		return topPickupCities;
	}

	public void setTopPickupCities(List<CityStatDTO> topPickupCities) {
		this.topPickupCities = topPickupCities;
	}
}
