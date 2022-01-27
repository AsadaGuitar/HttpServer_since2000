package main.java.jp.co.http.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptManager {

	public static String sha256(String plainText) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.reset();
		md.update(plainText.getBytes("utf8"));
		return String.format("%064x", new BigInteger(1, md.digest()));
	}
}
