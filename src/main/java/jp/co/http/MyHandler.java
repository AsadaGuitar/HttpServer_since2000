package main.java.jp.co.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.jp.co.db.DatabaseAccessory;
import main.java.jp.co.domain.Account;
import main.java.jp.co.http.security.Authentication;

public class MyHandler implements HttpHandler {

	  public void handle(HttpExchange t) throws IOException {

	    // 開始行を取得
	    String startLine =	 t.getRequestMethod() + " " + t.getRequestURI().toString() + " " + t.getProtocol();
	    System.out.println(startLine);

	    // リクエストヘッダを取得
	    Headers reqHeaders = t.getRequestHeaders();
	    for (String name : reqHeaders.keySet()) {
	      System.out.println(name + ": " + reqHeaders.getFirst(name));
	    }
	    
	    // リクエストボディを取得
	    InputStream is = t.getRequestBody();
	    byte[] temp = new byte[256];
	    is.read(temp);
	    is.close();
	    if (temp.length != 0) {
	      System.out.println(); // 空行
	    }
	    
	    // Content-Length 以外のレスポンスヘッダを設定
	    Headers resHeaders = t.getResponseHeaders();
	    resHeaders.set("Content-Type", "application/json");
	    resHeaders.set("Last-Modified",
	      ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));
	    resHeaders.set("Server",
	      "MyServer (" +
	        System.getProperty("java.vm.name") + " " +
	        System.getProperty("java.vm.vendor") + " " +
	        System.getProperty("java.vm.version") + ")");

	    //ステータスコードを初期化
	    int statusCode;

	    // レスポンスボディを構築
	    String resBody = "";
	    switch (t.getRequestURI().toString()) {
		    case "/login":
		    	if (reqHeaders.containsKey("Authorization")) {
		    		String[] headersAuth = reqHeaders.getFirst("Authorization").split(" ");
		    		if (headersAuth.length != 2) {
		    			statusCode = 403;
		    			resBody = "Index Outbound.";
		    		}
		    		String authStyle = headersAuth[0];
		    		String authData = headersAuth[1];
		    		if (authStyle.equals("Basic")) {
		    			System.out.println("Authentication Style \"Basic\" is Success.");
		    			
		    			Authentication auth = new Authentication();
		    			String token = auth.verifyToAuthData(authData);
		    		    if (token != null) {
		    		    	System.out.println("token is exists.");
		    		    	resHeaders.set("Authorization", "Bearer " + token);
		    		    	statusCode = 201;
		    		    	String username = auth.decodeJwt(token).getClaim("username").asString();
		    		    	resBody = "{\"name\":\"" + username + "\"}";
		    		    	break;
		    		    } 
		    		}
		    	} else { 
		    		System.out.println("Not Found Authorizaion Header.");
		    	}
		    	//認証方式を設定
		    	resHeaders.set("WWW-Authenticate", "Basic realm=\"Access to the staging site\"");
		    	statusCode = 401;
		    	break;
		    	
		    case "/drink":
		    	resBody = "{\"name\":\"cola\",\"price\":100}";
		    	statusCode = 200;
		    	break;
		    	
		    case "/hasToken":
		    	if (reqHeaders.containsKey("Authorization")) {
		    		String[] headersAuth = reqHeaders.getFirst("Authorization").split(" ");
		    		if (headersAuth.length != 2) {
		    			statusCode = 403;
		    			resBody = "Index Outbound.";
		    		}
		    		String authStyle = headersAuth[0];
		    		String authData = headersAuth[1];
		    		if (authStyle.equals("Bearer")) {
		    			System.out.println("Authentication Style \"Bearer\" is Success.");
		    			Authentication auth = new Authentication();
		    			try {
		    				DecodedJWT jwt = auth.decodeJwt(authData);
		    				System.out.println("claims = " + jwt.getClaims());
		    				String username = jwt.getClaims().get("username").asString();
		    				System.out.println("username =" + username);
		    				DatabaseAccessory dao = new DatabaseAccessory();
		    				Account account = dao.findByName(username);
		    				if (account != null) {
		    					statusCode = 200;
		    					resHeaders.set("Authorization", "Bearer " + authData);
		    					resBody = "{\"name\":\"" + account.getName() + "\",\"age\":" + account.getAge() + "}";
		    					break;
		    				}
		    			} catch(Exception e) {
		    				System.err.println("Failure Verify: " + e.getMessage());
		    				statusCode = 403;
		    				resBody = "Not Match Token Type";
		    			}
		    		}
		    	}
		    	//認証方式を設定
		    	resHeaders.set("WWW-Authenticate", "Basic realm=\"Access to the staging site\"");
		    	statusCode = 401;
		    	break;
		    	
		    default:
		    	resBody = "";
		    	statusCode = 400;
	    }

	    
	    long contentLength = resBody.getBytes(StandardCharsets.UTF_8).length;
	    t.sendResponseHeaders(statusCode, contentLength);

	    // レスポンスボディを送信
	    OutputStream os = t.getResponseBody();
	    os.write(resBody.getBytes());
	    os.close();
	  }
	}