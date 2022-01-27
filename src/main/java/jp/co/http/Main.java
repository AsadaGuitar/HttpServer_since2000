package main.java.jp.co.http;


import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Scanner;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.*;

import main.java.jp.co.http.security.Authentication;
import main.java.jp.co.http.security.CryptManager;

public class Main {

	//John Page's password : HELLO
	public static void main(String[] arg) throws NoSuchAlgorithmException, UnsupportedEncodingException, SQLException, ClassNotFoundException {

		//curl -H 'Authorization: Basic Sm9obiBQYWdlOjM3MzNjZDk3N2ZmOGViMThiOTg3MzU3ZTIyY2VkOTlmNDYwOTdmMzFlY2IyMzllODc4YWU2Mzc2MGU4M2U0ZDU=' --dump-header - http://localhost:9000/login
		//curl -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhdXRoMCIsImV4cCI6MTY0MzMwMjA5NiwidXNlcm5hbWUiOiJKb2huIFBhZ2UifQ.A74Xi4oS8lY-XBCe0JWfo_t8YSi1QzBBRZWe-dXK0nU' --dump-header - http://localhost:9000/hasToken
		
		
	    
	    Authentication auth = new Authentication();
	    
	    //誤ったパスワードを入力した場合
	    String failurePass = CryptManager.sha256("failure");
	    System.out.println("failurePass: " + failurePass);
	    String failureAuthData = Base64.getEncoder().encodeToString(
	    		("John Page:" + failurePass).getBytes(StandardCharsets.UTF_8)
	    );
	    System.out.println("failureAuthData: " + failureAuthData);
	    String failureToken = auth.verifyToAuthData(failureAuthData);
	    if (failureToken == null) {
	    	System.out.println("TEST SUCCESS\n");
	    } else {
	    	System.out.println("failureToken: " + failureToken);
		    DecodedJWT failureResult = auth.decodeJwt(failureToken);
		    System.out.println("failureResult: " + failureResult.getClaims().get("username"));
		    System.out.println("TEST FAILURE\n");
	    }

	    //正しいパスワードを入力した場合
	    String authData = Base64.getEncoder().encodeToString(
	    		"John Page:3733cd977ff8eb18b987357e22ced99f46097f31ecb239e878ae63760e83e4d5".getBytes(StandardCharsets.UTF_8)
	    );
	    System.out.println("authData: " + authData);
	    String token = auth.verifyToAuthData(authData);
	    System.out.println("token: " + token);
	    DecodedJWT result = auth.decodeJwt(token);
	    Claim username = result.getClaims().get("username");
	    System.out.println("result: " + username);
	    if (username.asString().equals("John Page")) {
	    	System.out.println("TEST SUCCESS\n");
	    } else {
	    	System.out.println("TEST FAILURE\n");
	    	return;
	    }
	    
	    
	    
	    System.out.println("StartServer\n");
	    
	    HttpServer server = null;
	    int port = 9000;
	    
	    try {
	    	server = HttpServer.create(new InetSocketAddress(port), 0);
	    	server.createContext("/", new MyHandler());
	    	server.start();
	    	
	    	try (Scanner scanner = new Scanner(System.in)) {
				scanner.nextLine();
			}
	    	server.stop(1000);
	    } catch (Exception e){
	    	System.err.println(e.getMessage());
	    	server.stop(1000);
	    }
	}	
}

