package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.dto.OfferMapper;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.entity.OfferFavorite;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.OfferFavoriteRepository;
import com.mobility.mobility_backend.repository.OfferRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
@Transactional
public class FavoriteService {

	private final OfferFavoriteRepository favoriteRepository;
	private final OfferRepository offerRepository;
	private final UserRepository userRepository;
	private final OfferMapper offerMapper;
	private final PaymentNotificationService notificationService;

	public FavoriteService(OfferFavoriteRepository favoriteRepository, OfferRepository offerRepository,
			UserRepository userRepository, OfferMapper offerMapper, PaymentNotificationService notificationService) {
		this.favoriteRepository = favoriteRepository;
		this.offerRepository = offerRepository;
		this.userRepository = userRepository;
		this.offerMapper = offerMapper;
		this.notificationService = notificationService;
	}

	public void addFavorite(Integer offerId) {
		User user = getCurrentUser();
		if (favoriteRepository.existsByUser_IdAndOffer_OfferId(user.getId(), offerId)) {
			return;
		}

		Offer offer = offerRepository.findById(offerId)
				.orElseThrow(() -> new IllegalArgumentException("Offre introuvable avec l'id " + offerId));

		OfferFavorite favorite = new OfferFavorite();
		favorite.setUser(user);
		favorite.setOffer(offer);
		favoriteRepository.save(favorite);
		notificationService.notifyFavoriteAdded(user, offer);
	}

	public void removeFavorite(Integer offerId) {
		User user = getCurrentUser();
		Offer offer = offerRepository.findById(offerId).orElse(null);
		favoriteRepository.deleteByUser_IdAndOffer_OfferId(user.getId(), offerId);
		notificationService.notifyFavoriteRemoved(user, offer);
	}

	@Transactional(readOnly = true)
	public List<Integer> getFavoriteOfferIdsForCurrentUser() {
		User user = getCurrentUser();
		return favoriteRepository.findOfferIdsByUserId(user.getId());
	}

	@Transactional(readOnly = true)
	public List<OfferDTO> getFavoriteOffersForCurrentUser() {
		User user = getCurrentUser();
		return favoriteRepository.findByUser_Id(user.getId()).stream().map(OfferFavorite::getOffer).map(offer -> {
			OfferDTO dto = offerMapper.toDTO(offer);
			dto.setFavorite(true);
			return dto;
		}).collect(Collectors.toList());
	}

	private User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new AccessDeniedException("Utilisateur non authentifié");
		}

		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable: " + username));
	}
}
