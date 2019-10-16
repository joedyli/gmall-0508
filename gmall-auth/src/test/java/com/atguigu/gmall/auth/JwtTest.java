package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
	private static final String pubKeyPath = "D:\\project-0508\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\project-0508\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "@12321dlsjdSFFW234324#w5j");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzEwMzY1NTd9.bdQnqWue-bu4DiS3zUXD47DVuFUNjIMvRagw7HoY_0Z0-89XV7Zk7VfRQk92Sek6udvr0k-e2ATggPjbVYeuGKDr6JjMtUQLqUtdsh-j2hdPrr07gGo-B6AYorLtvpzSZmGC7B51UivnlV4YTFXjbGWTAeQgijw8Y2nNh_y2sgZUKr7CPzyF7jR6Ru-g6hXpviTQkpXTmsnd9ke_Soi1aaqVp9y6fUetdaQGYLakFNR5vSnl3KE6zhrdKAxRieBin9m1MBH-WVOCs376g5U4yHwaZWQi7VLJYuuZtmWQdmwOzHVUbX7245lV0ERGAHWnqfOfZuy_IOU4o8FjBVwwIQ\n";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}