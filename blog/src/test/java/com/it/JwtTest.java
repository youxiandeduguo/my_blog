package com.it;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    @Test
    public void testGen(){
        Map<String,Object> claims=new HashMap<>();
        claims.put("id",1);
        claims.put("username","test");
        String token= JWT.create()
                .withClaim("user",claims)
                .withExpiresAt(new Date(System.currentTimeMillis()+1000*60*60*12))
                .sign(Algorithm.HMAC256("it"));
        System.out.println(token);
    }

    /*
    @Test
    public void testParse(){
        String token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJ1c2VyIjp7ImlkIjoxLCJ1c2VybmFtZSI6InRlc3QifSwiZXhwIjoxNzM5NzU1Njg3fQ." +
                "BIokscxR-amhqaGBoH2pwwklKUgykOz2NfQmNmdUsj4";
        JWTVerifier jwtVerifier= JWT.require(Algorithm.HMAC256("it")).build();

        DecodedJWT decodedJWT= jwtVerifier.verify(token);
        Map<String, Claim> claims=decodedJWT.getClaims();
        System.out.println(claims.get("user"));
    }*/


}
