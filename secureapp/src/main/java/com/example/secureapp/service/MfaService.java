package com.example.secureapp.service;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.secret.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import dev.samstevens.totp.time.SystemTimeProvider;

@Service
public class MfaService {
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    public String generateNewSecret() { return secretGenerator.generate(); }
    public boolean verifyCode(String secret, String code) { return codeVerifier.isValidCode(secret, code); }

    public byte[] generateQrCode(String secret, String email) throws Exception {
        String data = "otpauth://totp/SecureApp:" + email + "?secret=" + secret + "&issuer=SecureApp";
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 250, 250);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);
        return bos.toByteArray();
    }
}