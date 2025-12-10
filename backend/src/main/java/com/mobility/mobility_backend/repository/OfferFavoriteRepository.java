package com.mobility.mobility_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mobility.mobility_backend.entity.OfferFavorite;

public interface OfferFavoriteRepository extends JpaRepository<OfferFavorite, Long> {

	boolean existsByUser_IdAndOffer_OfferId(Integer userId, Integer offerId);

	void deleteByUser_IdAndOffer_OfferId(Integer userId, Integer offerId);

	List<OfferFavorite> findByUser_Id(Integer userId);

	@Query("select f.offer.offerId from OfferFavorite f where f.user.id = :userId")
	List<Integer> findOfferIdsByUserId(Integer userId);
}
