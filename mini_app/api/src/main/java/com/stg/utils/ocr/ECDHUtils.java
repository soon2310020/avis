package com.stg.utils.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stg.service3rd.ocr.dto.resp.OCRDecryptedResp;
import com.stg.service3rd.ocr.dto.resp.ProcessedOcrResp;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ECDHUtils {
    public static final List<String> ENCRYPTED_ARRAYS = List.of("business", "file_checksums", "system", "user");

    public static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String ALGORITHM = "AES";
    public static final String KEYPAIR_TYPE = "EC";
    public static final String ECDH = "ECDH";
    public static final String SECP_384_R_1 = "secp384r1";
    public static final String HASH_ALGORITHM = "SHA-256";

    public static KeyPair generateECKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEYPAIR_TYPE);
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(SECP_384_R_1);
        keyPairGenerator.initialize(ecGenParameterSpec);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] generateSharedSecret(PrivateKey privateKey, String ocrPublicKey) throws Exception {
        PublicKey publicKey = getPublicKeyFromString(ocrPublicKey);
        KeyAgreement keyAgreement = KeyAgreement.getInstance(ECDH);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        MessageDigest sha256 = MessageDigest.getInstance(HASH_ALGORITHM);
        return sha256.digest(keyAgreement.generateSecret());
    }

    public static String encrypt(String plaintext, byte[] sharedSecret) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM); //NOSONAR
        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, ALGORITHM);
        byte[] ivBytes = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedData = cipher.doFinal(plaintextBytes);
        byte[] combinedData = new byte[ivBytes.length + encryptedData.length];
        System.arraycopy(ivBytes, 0, combinedData, 0, ivBytes.length);
        System.arraycopy(encryptedData, 0, combinedData, ivBytes.length, encryptedData.length);
        return Base64.getEncoder().encodeToString(combinedData);
    }

    public static String decrypt(String encryptedDataString, byte[] sharedSecret) throws Exception {
        if (encryptedDataString == null) return null;

        byte[] combinedData = Base64.getDecoder().decode(encryptedDataString);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM); //NOSONAR
        byte[] ivBytes = new byte[16];
        byte[] encryptedData = new byte[combinedData.length - ivBytes.length];
        System.arraycopy(combinedData, 0, ivBytes, 0, ivBytes.length);
        System.arraycopy(combinedData, ivBytes.length, encryptedData, 0, encryptedData.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

        byte[] decryptedData = cipher.doFinal(encryptedData);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static List<String> calculateMD5Checksums(MultipartFile[] files) throws IOException {
        List<String> checksums = new ArrayList<>();

        for (MultipartFile file : files) {
            byte[] fileBytes = file.getBytes();
            String checksum = DigestUtils.md5DigestAsHex(fileBytes); //NOSONAR
            checksums.add(checksum);
        }
        return checksums;
    }

    private static PublicKey getPublicKeyFromString(String publicKeyString) throws Exception {
        // Convert byte array to ECPublicKey object
        byte[] bytes = Base64.getDecoder().decode(publicKeyString);
        if (bytes[0] != 0x04) {
            throw new InvalidKeyException("Key is not uncompressed");
        }

        // Lấy tọa độ x và y của điểm EC.
        BigInteger x = new BigInteger(1, bytes, 1, 48);
        BigInteger y = new BigInteger(1, bytes, 49, 48);


        // Sử dụng Bouncy Castle Provider để lấy thông số của đường cong P-384.
        ECNamedCurveParameterSpec namedCurveParams = ECNamedCurveTable.getParameterSpec("P-384");

        // Sử dụng ECNamedCurveParameterSpec để tạo đối tượng ECPublicKeySpec mới với điểm EC làm giá trị.
        org.bouncycastle.math.ec.ECPoint ecPoint = namedCurveParams.getCurve().createPoint(x, y);
        org.bouncycastle.jce.spec.ECPublicKeySpec spec = new org.bouncycastle.jce.spec.ECPublicKeySpec(ecPoint, namedCurveParams);

        // Sử dụng KeyFactory.generatePublic() để tạo đối tượng ECPublicKey mới từ ECPublicKeySpec.
        KeyFactory factory = KeyFactory.getInstance("EC", "BC");
        return factory.generatePublic(spec);
    }

    public static <T> void encryptFieldsInObject(T object, byte[] sharedKey, List<String> fieldsToDecrypt) throws Exception {
        for (String fieldName : fieldsToDecrypt) {
            Field field;
            field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true); //NOSONAR
            Object fieldValue;
            fieldValue = field.get(object);
            if (fieldValue instanceof String) {
                String value = (String) fieldValue;
                String encryptedValue = encrypt(value, sharedKey);
                field.set(object, encryptedValue); //NOSONAR

            }
            if (fieldValue instanceof Collection<?>) {
                List<?> list = (List<?>) fieldValue;
                List<String> enOrDecryptedList = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof String) enOrDecryptedList.add(encrypt((String) item, sharedKey));
                }
                field.set(object, enOrDecryptedList); //NOSONAR
            }
        }

    }

    public static OCRDecryptedResp decryptOcrData(ProcessedOcrResp.Doc result, byte[] sharedKey) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode data = objectMapper.createObjectNode();
        for (ProcessedOcrResp.SingleResult s : result.getSingleResult()) {
            data.put(s.getName(), decrypt(s.getValue(), sharedKey));
        }
        for (String templateCode : result.getTemplateCodes()){
            data.put("template_codes",decrypt(templateCode,sharedKey));
        }
        return OCRDecryptedResp.getObjectFromJSonNode(data);
    }
}






