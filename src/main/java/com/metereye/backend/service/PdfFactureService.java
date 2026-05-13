// ✅ CRÉÉ — PdfFactureService.java
package com.metereye.backend.service;

import com.metereye.backend.entity.FactureLocataire;
import com.metereye.backend.entity.User;

public interface PdfFactureService {

    // Génère le PDF et le retourne en bytes (génère aussi le PDF si absent)
    byte[] telechargerPdf(Long factureId, User currentUser);

    // Génère le PDF d'une facture sans contrôle d'accès (usage interne)
    byte[] genererPdf(FactureLocataire facture);
}
