package com.mobility.mobility_backend.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.repository.ReservationRepository;

@Service
public class ReservationDocumentService {

	private final ReservationRepository reservationRepository;

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

	public ReservationDocumentService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	public byte[] buildReceipt(Integer reservationId) {
		Optional<Reservation> optional = reservationRepository.findById(reservationId);
		if (optional.isEmpty()) {
			throw new IllegalArgumentException("Réservation introuvable");
		}
		Reservation reservation = optional.get();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4);
		try {
			PdfWriter.getInstance(document, baos);
			document.open();

			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
			Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

			document.add(new Paragraph("Reçu de réservation", titleFont));
			document.add(new Paragraph("Réservation #" + reservation.getReservationId(), textFont));
			document.add(new Paragraph(" "));

			document.add(new Paragraph("Informations client", sectionFont));
			PdfPTable clientTable = new PdfPTable(2);
			clientTable.setWidthPercentage(100);
			clientTable.addCell(cell("Client"));
			String username = reservation.getUser() != null ? reservation.getUser().getUsername() : "N/A";
			clientTable.addCell(cell(username));
			clientTable.addCell(cell("Nom complet"));
			clientTable.addCell(cell(resolveClientName(reservation)));
			clientTable.addCell(cell("Email"));
			String email = reservation.getUser() != null ? reservation.getUser().getEmail() : "N/A";
			clientTable.addCell(cell(email));
			document.add(clientTable);
			document.add(new Paragraph(" "));

			document.add(new Paragraph("Informations réservation", sectionFont));
			PdfPTable bookingTable = new PdfPTable(2);
			bookingTable.setWidthPercentage(100);
			bookingTable.addCell(cell("Date de réservation"));
			bookingTable.addCell(
					cell(reservation.getReservationDate() != null ? DATE_FORMAT.format(reservation.getReservationDate())
							: "N/A"));
			bookingTable.addCell(cell("Statut"));
			bookingTable.addCell(cell(formatReservationStatus(reservation.getStatus())));
			bookingTable.addCell(cell("Statut paiement"));
			bookingTable.addCell(cell(formatPaymentStatus(reservation.getPaymentStatus())));
			bookingTable.addCell(cell("Référence paiement"));
			bookingTable.addCell(cell(reservation.getPaymentReference() != null ? reservation.getPaymentReference()
					: "Non disponible"));
			bookingTable.addCell(cell("Montant"));
			String amount = reservation.getPaymentAmount() != null ? reservation.getPaymentAmount().toPlainString()
					: (reservation.getOffer() != null && reservation.getOffer().getPrice() != null
							? reservation.getOffer().getPrice().toPlainString()
							: "0");
			bookingTable.addCell(cell(amount + " €"));
			document.add(bookingTable);
			document.add(new Paragraph(" "));

			document.add(new Paragraph("Offre", sectionFont));
			PdfPTable offerTable = new PdfPTable(2);
			offerTable.setWidthPercentage(100);
			offerTable.addCell(cell("Description"));
			offerTable.addCell(
					cell(reservation.getOffer() != null ? reservation.getOffer().getDescription() : "Non disponible"));
			offerTable.addCell(cell("Service"));
			offerTable.addCell(cell(reservation.getOffer() != null && reservation.getOffer().getMobilityService() != null
					? reservation.getOffer().getMobilityService().getName()
					: "Non disponible"));
			document.add(offerTable);

			document.close();
			return baos.toByteArray();
		} catch (Exception ex) {
			throw new IllegalStateException("Impossible de générer le reçu", ex);
		}
	}

	private PdfPCell cell(String value) {
		PdfPCell cell = new PdfPCell(new Phrase(value));
		cell.setPadding(6f);
		return cell;
	}

	private String resolveClientName(Reservation reservation) {
		if (reservation.getUser() == null) {
			return "N/A";
		}
		String first = reservation.getUser().getFirstName();
		String last = reservation.getUser().getLastName();
		StringBuilder fullName = new StringBuilder();
		if (first != null && !first.isBlank()) {
			fullName.append(first.trim());
		}
		if (last != null && !last.isBlank()) {
			if (fullName.length() > 0) {
				fullName.append(" ");
			}
			fullName.append(last.trim());
		}
		return fullName.length() == 0 ? "N/A" : fullName.toString();
	}

	private String formatReservationStatus(Reservation.ReservationStatus status) {
		if (status == null) {
			return "N/A";
		}
		return switch (status) {
		case PENDING -> "En attente";
		case CONFIRMED -> "Confirmée";
		case CANCELLED -> "Annulée";
		case COMPLETED -> "Terminée";
		};
	}

	private String formatPaymentStatus(Reservation.PaymentStatus status) {
		if (status == null) {
			return "N/A";
		}
		return switch (status) {
		case PENDING -> "En attente";
		case REQUIRES_ACTION -> "Action requise";
		case PAID -> "Payé";
		case FAILED -> "Echoué";
		case REFUNDED -> "Remboursé";
		};
	}
}
