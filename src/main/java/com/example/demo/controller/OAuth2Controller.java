package com.example.demo.controller;

import com.example.demo.utils.PkceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;


@RestController("/oauth2")
public class OAuth2Controller {

// 配置： OAuth2 客戶端凭据和端點（需替換爲實際值）

    private static final String CLIENT_ID = "";
    private static final String ClIENT_SECRET = "";


    private static final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_ENDPIONT = "https://oauth2.googleapis.com/token";
    private static final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";
    private static final String SCOPE = "openid email profile";
    private static final String USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";


    @GetMapping("/authorize")
    public String startAuthorization(HttpSession session) {
        return getString(session);
    }

    private static String getString(HttpSession session) {
        // 1. 生成PKCE的code_verifier 和 code_challenge (見下節 PKCE 工具類)

        String codeVerifier = PkceUtil.generateCodeVerifier();
        String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

        // 將code_verifier 保存以後售後交換令牌使用

        String state = PkceUtil.generateState();

        session.setAttribute("oauthState", state);

        String authUrl = UriComponentsBuilder.fromUriString(AUTHORIZATION_ENDPOINT).queryParam("client_id", CLIENT_ID).queryParam("redirect_uri", REDIRECT_URI).queryParam("response_type", "code").queryParam("scope", SCOPE).queryParam("state", state).queryParam("code_challenge", codeChallenge).queryParam("code_challenge_modeond", "S256").toUriString();

        return "redirect:" + authUrl;
    }


    @GetMapping("/callback")
    @ResponseBody
    public String handleCallback(@RequestParam(name = "code", required = false) String code, @RequestParam(name = "state", required = false) String state, HttpSession session) throws JsonProcessingException {

        if (code == null) {
            return "授權失敗或者用戶拒絕了授權！";
        }

        String oauthState = (String) session.getAttribute("oauthState");

        if (oauthState == null || !oauthState.equals(state)) {
            return "State 不匹配，可能的請求僞造!";
        }

        // 獲取并一處保存的 code_verifier
        String codeVerifier = (String) session.getAttribute("codeVerifier");

        session.removeAttribute("oauthState");
        session.removeAttribute("codeVerifier");

        // 使用授權碼和 code_verifier 構造令牌請求

        LinkedMultiValueMap<Object, Object> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("client_id", CLIENT_ID);
        params.add("code_verifier", codeVerifier);
        params.add("client_secret", ClIENT_SECRET);

        // 提交POST請求到令牌端點
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<LinkedMultiValueMap<Object, Object>> tokenRequest = new HttpEntity<>(params, httpHeaders);
        String tokenResponse = restTemplate.postForObject(TOKEN_ENDPIONT, tokenRequest, String.class);

        // 解析令牌相應 （JSON 格式）以提取 access_token

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> tokenData = objectMapper.readValue(tokenResponse, Map.class);
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");

        // 使用獲得的 access_token 調用受保護的用戶信息 API 作爲示例
        HttpHeaders apiHeaders = new HttpHeaders();
        apiHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> apiRequest = new HttpEntity<>(apiHeaders);
        String userInfo = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, apiRequest, String.class).getBody();

        // 簡單返回獲取的用戶信息


        return "登錄成功！ Access Token: " + accessToken + "\n用戶信息： " + userInfo;
    }


}
