package com.restaurant.waitlist.backend.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for generating QR codes for offer redemptions.
 * QR codes encode the redemption code and are used for quick validation at POS.
 */
public class QRCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(QRCodeGenerator.class);
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    /**
     * Generate a QR code from a redemption code.
     * 
     * @param redemptionCode The 6-digit or text code to encode
     * @return Byte array of PNG QR code image
     */
    public static byte[] generateQRCode(String redemptionCode) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    redemptionCode,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_WIDTH,
                    QR_CODE_HEIGHT
            );

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream);
            byte[] qrCodeImage = byteArrayOutputStream.toByteArray();

            log.debug("QR code generated for redemption code: {}", redemptionCode);
            return qrCodeImage;
        } catch (Exception e) {
            log.error("Failed to generate QR code for code: {}", redemptionCode, e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }

    /**
     * Generate a Base64-encoded QR code (for easier transport over HTTP).
     * 
     * @param redemptionCode The code to encode
     * @return Base64 encoded PNG image
     */
    public static String generateQRCodeBase64(String redemptionCode) {
        byte[] qrCodeImage = generateQRCode(redemptionCode);
        return Base64.getEncoder().encodeToString(qrCodeImage);
    }

    /**
     * Generate a data URI for embedding QR code directly in HTML/JSON.
     * 
     * @param redemptionCode The code to encode
     * @return data:image/png;base64,... URI
     */
    public static String generateQRCodeDataURI(String redemptionCode) {
        String base64 = generateQRCodeBase64(redemptionCode);
        return "data:image/png;base64," + base64;
    }

    /**
     * Validate that a code matches the expected format (for manual entry).
     * Currently checks for 6-digit numeric codes.
     * 
     * @param code The code to validate
     * @return true if valid format
     */
    public static boolean isValidCodeFormat(String code) {
        return code != null && code.matches("^[0-9]{6}$");
    }

    /**
     * Decode a QR code image from bytes.
     * Accepts PNG, JPG, and other image formats.
     * 
     * @param imageBytes The image file bytes (PNG, JPG, etc.)
     * @return The decoded text from QR code
     * @throws Exception if QR code cannot be decoded
     */
    public static String decodeQRCode(byte[] imageBytes) {
        try {
            // Read image from bytes
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            if (bufferedImage == null) {
                throw new RuntimeException("Could not read image file. Ensure it's a valid PNG/JPG/GIF image.");
            }

            // Create BinaryBitmap
            BinaryBitmap binaryBitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))
            );

            // Decode QR code
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(binaryBitmap, hints);

            String decodedText = result.getText();
            log.info("QR code decoded successfully: {}", decodedText.substring(0, Math.min(3, decodedText.length())) + "***");
            
            return decodedText;
            
        } catch (NotFoundException e) {
            log.error("No QR code found in image", e);
            throw new RuntimeException("No QR code found in the image. Please scan a valid offer QR code.");
        } catch (IOException e) {
            log.error("Error reading image file", e);
            throw new RuntimeException("Error reading image file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error decoding QR code", e);
            throw new RuntimeException("Error decoding QR code: " + e.getMessage());
        }
    }

    /**
     * Decode a QR code from a Base64-encoded image string.
     * 
     * @param base64Image Base64 encoded image string
     * @return The decoded text from QR code
     */
    public static String decodeQRCodeFromBase64(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            return decodeQRCode(imageBytes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 string", e);
            throw new RuntimeException("Invalid Base64 image format");
        }
    }
}
