// ✅ CRÉÉ — PdfFactureServiceImpl.java
package com.metereye.backend.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.metereye.backend.entity.FactureLocataire;
import com.metereye.backend.entity.SousCompteur;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.RoleName;
import com.metereye.backend.enums.StatutFacture;
import com.metereye.backend.repository.FactureLocataireRepository;
import com.metereye.backend.service.PdfFactureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PdfFactureServiceImpl implements PdfFactureService {

    private final FactureLocataireRepository factureLocataireRepository;

    @Override
    public byte[] telechargerPdf(Long factureId, User currentUser) {
        FactureLocataire facture = factureLocataireRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        boolean isLocataire = facture.getSousCompteur().getLocataire() != null
                && facture.getSousCompteur().getLocataire().getId().equals(currentUser.getId());
        boolean isProprietaire = facture.getSousCompteur().getMaison().getProprietaire().getId()
                .equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().getName() == RoleName.ADMIN;

        if (!isLocataire && !isProprietaire && !isAdmin) {
            throw new RuntimeException("Accès non autorisé à cette facture");
        }

        byte[] pdfBytes = genererPdf(facture);

        // Passe en TELECHARGEE quand le locataire télécharge
        if (isLocataire && facture.getStatut() == StatutFacture.GENEREE) {
            facture.setStatut(StatutFacture.TELECHARGEE);
            factureLocataireRepository.save(facture);
        }

        log.info("PDF téléchargé: factureId={} par {}", factureId, currentUser.getEmail());
        return pdfBytes;
    }

    @Override
    public byte[] genererPdf(FactureLocataire facture) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SousCompteur sc = facture.getSousCompteur();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
             Document document = new Document(pdfDoc)) {

            // ── EN-TÊTE ──────────────────────────────────────────────────────
            document.add(new Paragraph("FACTURE D'ÉLECTRICITÉ")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(20)
                    .setMarginBottom(4));

            document.add(new Paragraph("MeterEye — Système de gestion partagée")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(16));

            // ── INFOS FACTURE ─────────────────────────────────────────────────
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(16);

            addRow(infoTable, "Période", getNomMois(facture.getMois()) + " " + facture.getAnnee());
            addRow(infoTable, "Maison", sc.getMaison().getNom());
            addRow(infoTable, "Adresse", sc.getMaison().getAdresse());
            addRow(infoTable, "Additionneuse", sc.getReference());

            if (sc.getDescriptionLogement() != null) {
                addRow(infoTable, "Logement", sc.getDescriptionLogement());
            }
            if (sc.getLocataire() != null) {
                addRow(infoTable, "Locataire", sc.getLocataire().getNomComplet());
                addRow(infoTable, "Email", sc.getLocataire().getEmail());
            }
            if (facture.getDateGeneration() != null) {
                addRow(infoTable, "Date de génération",
                        facture.getDateGeneration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            document.add(infoTable);

            // ── DÉTAIL CONSOMMATION ──────────────────────────────────────────
            document.add(new Paragraph("Détail de la répartition")
                    .setBold()
                    .setFontSize(13)
                    .setMarginBottom(6));

            Table consoTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                    .useAllAvailableWidth()
                    .setMarginBottom(16);

            addRow(consoTable, "Consommation du locataire",
                    String.format("%.2f kWh", nullToZero(facture.getConsommationKwh())));
            addRow(consoTable, "Consommation totale de la maison",
                    String.format("%.2f kWh", nullToZero(facture.getConsommationTotaleMaison())));
            addRow(consoTable, "Part proportionnelle",
                    String.format("%.2f %%", nullToZero(facture.getPartPourcentage())));
            addRow(consoTable, "Montant facture principale (FCFA)",
                    String.format("%.0f FCFA", nullToZero(facture.getMontantFacturePrincipale())));
            document.add(consoTable);

            // ── MONTANT À PAYER ──────────────────────────────────────────────
            document.add(new Paragraph(
                    String.format("MONTANT À PAYER : %.0f FCFA", nullToZero(facture.getMontantFcfa())))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginTop(8)
                    .setMarginBottom(4));

            document.add(new Paragraph("Statut : " + facture.getStatut().name())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

        } catch (Exception e) {
            log.error("Erreur génération PDF factureId={}: {}", facture.getId(), e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage());
        }

        return baos.toByteArray();
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10)));
    }

    private double nullToZero(Double value) {
        return value != null ? value : 0.0;
    }

    private String getNomMois(Integer mois) {
        return switch (mois) {
            case 1 -> "Janvier";   case 2 -> "Février";   case 3 -> "Mars";
            case 4 -> "Avril";     case 5 -> "Mai";        case 6 -> "Juin";
            case 7 -> "Juillet";   case 8 -> "Août";       case 9 -> "Septembre";
            case 10 -> "Octobre";  case 11 -> "Novembre";  case 12 -> "Décembre";
            default -> "Mois inconnu";
        };
    }
}
