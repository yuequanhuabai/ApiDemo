package com.example.demo.controller;

import com.example.demo.utils.PkceUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class OAuth2Controller {

// 配置： OAuth2 客戶端凭据和端點（需替換爲實際值）

    private static final String CLIENT_ID = "clientId";
    private static final String ClIENT_SECRET = "clientSecret";
    private static final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/9/oauth2/v2/auth";
    private static final String TOKEN_ENDPIONT = "https://oauth2.googleapis.com/token";
    private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";
    private static final String SCOPE = "openid email profile";
    private static final String USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";


    @GetMapping("oauth2/authorize")
    public String startAuthorization(HttpSession session) {
        // 1. 生成PKCE的code_verifier 和 code_challenge (見下節 PKCE 工具類)

        String codeVerifier = PkceUtil.generateCodeVerifier();
        String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

        // 將code_verifier 保存以後售後交換令牌使用

        String state = PkceUtil.generateState();

        session.setAttribute("oauthState", state);

        String authUrl = UriComponentsBuilder.fromUriString(AUTHORIZATION_ENDPOINT)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_modeond", "S256")
                .toUriString();

        return "redirect:" + authUrl;
    }


    

}
