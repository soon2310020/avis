package unused.util;

//@Deprecated
//public class AesEncoder {
//
//	public static String encode(String rawData, String salt) throws Exception {
//		LogicUtils.assertNotEmpty(rawData, "rawData");
//		LogicUtils.assertNotEmpty(salt, "salt");
//		if (salt.length() < 16) {
//			throw new IllegalArgumentException("salt should be longer than 15");
//		}
//
//		byte[] key = salt.getBytes("UTF-8");
//		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//
//		byte[] iv = new byte[12];
//		new SecureRandom().nextBytes(iv);
//		GCMParameterSpec paramSpec = new GCMParameterSpec(128, iv);
//
//		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//		cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
//
//		byte[] encoded = cipher.doFinal(rawData.getBytes("UTF-8"));
//
//		ByteBuffer buf = ByteBuffer.allocate(4 + iv.length + encoded.length);
//		buf.putInt(iv.length);
//		buf.put(iv);
//		buf.put(encoded);
//		byte[] bytes = buf.array();
//
//		char[] chars = Hex.encode(bytes);
//		String encodedData = new String(chars);
//		return encodedData;
//	}
//
//	public static String decode(String encodedData, String salt) throws Exception {
//		LogicUtils.assertNotEmpty(encodedData, "encodedData");
//		LogicUtils.assertNotEmpty(salt, "salt");
//		if (salt.length() < 16) {
//			throw new IllegalArgumentException("salt should be longer than 15");
//		}
//
//		byte[] bytes = Hex.decode(encodedData);
//
//		byte[] key = salt.getBytes("UTF-8");
//		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//
//		ByteBuffer buf = ByteBuffer.wrap(bytes);
//		int ivLength = buf.getInt();
//		if (ivLength < 12 || ivLength >= 16) {
//			throw new IllegalArgumentException("Invalid IV");
//		}
//
//		byte[] iv = new byte[ivLength];
//		buf.get(iv);
//		GCMParameterSpec paramSpec = new GCMParameterSpec(128, iv);
//
//		byte[] encoded = new byte[buf.remaining()];
//		buf.get(encoded);
//
//		final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//		cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
//		byte[] rawBytes = cipher.doFinal(encoded);
//
//		String rawData = new String(rawBytes, "UTF-8");
//		return rawData;
//	}
//
//}
