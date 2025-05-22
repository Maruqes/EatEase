package com.eatease.eatease.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QRService {

    private final Map<String, QRData> qrDataMap = new ConcurrentHashMap<>();
    @Value("${app.base-url}/qr/qrPage?key=")
    private String PREFIX_URL;

    public String generateKey(Long funcionarioId, Long mesaId) {
        String key = UUID.randomUUID().toString();
        qrDataMap.put(key, new QRData(funcionarioId, mesaId, false, key));
        return key;
    }

    public byte[] generateQRCodeImage(String key, int width, int height) throws WriterException, IOException {
        String url = PREFIX_URL + key;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        System.out.println("URL QR Code: " + url);
        return pngOutputStream.toByteArray();
    }

    public QRData getQRData(String key) {
        return qrDataMap.get(key);
    }

    public boolean markAsUsed(String key) {
        QRData data = qrDataMap.get(key);
        if (data != null && !data.isUsed()) {
            data.setUsed(true);
            return true;
        }
        return false;
    }

    public List<QRData> getAllQRData() {
        return List.copyOf(qrDataMap.values());
    }

    public Boolean isKeyValidAndUnused(String key) {
        QRData data = qrDataMap.get(key);
        return data != null && !data.isUsed();
    }

    public static class QRData {
        private Long funcionarioId;
        private Long mesaId;
        private boolean used;
        private String key;

        public QRData(Long funcionarioId, Long mesaId, boolean used, String key) {
            this.funcionarioId = funcionarioId;
            this.mesaId = mesaId;
            this.used = used;
            this.key = key;
        }

        public Long getFuncionarioId() {
            return funcionarioId;
        }

        public Long getMesaId() {
            return mesaId;
        }

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
