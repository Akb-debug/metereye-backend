// ✅ CRÉÉ — PdfController.java
package com.metereye.backend.controller;

import com.metereye.backend.entity.FactureLocataire;
import com.metereye.backend.entity.User;
import com.metereye.backend.repository.FactureLocataireRepository;
import com.metereye.backend.service.PdfFactureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PdfController extends BaseController {

    private final PdfFactureService pdfFactureService;
    private final FactureLocataireRepository factureLocataireRepository;

    /**
     * GET /api/pdf/factures/{factureId}
     * Télécharge le PDF d'une facture — génère d'abord si absent
     * Rôle : PROPRIETAIRE ou LOCATAIRE
     */
    @GetMapping("/factures/{factureId}")
    public ResponseEntity<byte[]> telechargerFacturePdf(@PathVariable Long factureId) {
        try {
            User currentUser = getCurrentUser();
            byte[] pdfBytes = pdfFactureService.telechargerPdf(factureId, currentUser);

            // Nom du fichier : facture_{id}_{mois}_{annee}.pdf
            String filename = buildFilename(factureId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            log.warn("PDF non accessible: factureId={}, raison={}", factureId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Erreur génération PDF: factureId={}", factureId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String buildFilename(Long factureId) {
        try {
            FactureLocataire facture = factureLocataireRepository.findById(factureId).orElse(null);
            if (facture != null) {
                return String.format("facture_%d_%02d_%d.pdf",
                        factureId, facture.getMois(), facture.getAnnee());
            }
        } catch (Exception ignored) {}
        return "facture_" + factureId + ".pdf";
    }
}
