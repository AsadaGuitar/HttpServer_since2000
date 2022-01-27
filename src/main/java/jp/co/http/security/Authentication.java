package main.java.jp.co.http.security;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import main.java.jp.co.db.DatabaseAccessory;
import main.java.jp.co.domain.Account;

public class Authentication {
	
	private String key = "secret";
	
	private String jwt(String username) {
		
		Date expireTime = new Date();
	    expireTime.setTime(expireTime.getTime() + 600000l);

	    Algorithm algorithm = Algorithm.HMAC256(key);
	    String token = JWT.create()
	            .withIssuer("auth0")
	            .withExpiresAt(expireTime)
	            .withClaim("username", username)
	            .sign(algorithm);
		return token;
	}
	
	public DecodedJWT decodeJwt(String token) throws JWTVerificationException {
		Algorithm algorithm = Algorithm.HMAC256(key);
	    JWTVerifier verifier = JWT.require(algorithm)
	            .withIssuer("auth0")
	            .build(); //Reusable verifier instance
	    DecodedJWT jwt = verifier.verify(token);
	    return jwt;
	}
	
	public String verifyToAuthData(String authData) {
		try {
			DatabaseAccessory dao = new DatabaseAccessory();
			String[] decodedAuthData = new String(Base64.getDecoder().decode(authData), StandardCharsets.UTF_8).split(":");
			String name = decodedAuthData[0];
			String password = decodedAuthData[1];
			Account account = dao.findByName(name);
			if (account == null) {
				return null;
			}
			String accountPassword = account.getPassword();
			//verify
			if (accountPassword.equals(password)) {
				String jwt = this.jwt(name);
				return jwt;
			}
			else {
				System.out.println("illegal password");
				return null;
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
	