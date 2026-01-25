package com.rms.service;

//package com.rms.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for generating QR codes for tables
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeService {

    @Value("${app.qr.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.qr.size:300}")
    private int qrCodeSize;

    /**
     * Generate unique QR code identifier
     */
    public String generateQRCodeIdentifier() {
        return "QR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Generate QR code URL for table
     */
    public String generateTableQRUrl(String qrCode) {
        return String.format("%s/api/tables/scan/%s", baseUrl, qrCode);
    }

    /**
     * Generate QR code image as Base64 string
     */
    public String generateQRCodeImageBase64(String qrCode) {
        try {
            String qrUrl = generateTableQRUrl(qrCode);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for: {}", qrCode, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate QR code image file
     */
    public void generateQRCodeImageFile(String qrCode, String filePath) {
        try {
            String qrUrl = generateTableQRUrl(qrCode);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);

            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            log.info("QR code image generated at: {}", filePath);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code file for: {}", qrCode, e);
            throw new RuntimeException("Failed to generate QR code file", e);
        }
    }

    /**
     * Generate session join URL
     */
    public String generateSessionJoinUrl(String sessionCode) {
        return String.format("%s/api/tables/session/join/%s", baseUrl, sessionCode);
    }
}
