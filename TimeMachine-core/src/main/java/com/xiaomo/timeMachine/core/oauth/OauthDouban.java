package com.xiaomo.timeMachine.core.oauth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaomo.timeMachine.core.util.OathConfig;
import com.xiaomo.timeMachine.core.util.TokenUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

/**
 * douban 登录 BAE
 *
 * @author L.cm
 *         email: 596392912@qq.com
 *         site:  http://www.dreamlu.net
 * @date Jun 24, 2013 10:18:23 PM
 */
public class OauthDouban extends Oauth {

    private static final Logger LOGGER = Logger.getLogger(OauthDouban.class);

    private static final String AUTH_URL = "https://www.douban.com/service/auth2/auth";
    private static final String TOKEN_URL = "https://www.douban.com/service/auth2/token";
    private static final String USER_INFO_URL = "https://api.douban.com/v2/user/~me";

    private static OauthDouban oauthDouban = new OauthDouban();

    public OauthDouban() {
        setClientId(OathConfig.getValue("openid_douban"));
        setClientSecret(OathConfig.getValue("openkey_douban"));
        setRedirectUri(OathConfig.getValue("redirect_douban"));
    }

    /**
     * 用于链式操作
     */
    public static OauthDouban me() {
        return oauthDouban;
    }

    /**
     * @return String    返回类型
     * @throws UnsupportedEncodingException 获取授权url
     */
    public String getAuthorizeUrl(String state) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectUri());
        if (StringUtils.isNotBlank(state)) {
            params.put("state", state); //OAuth2.0标准协议建议，利用state参数来防止CSRF攻击。可存储于session或其他cache中
        }
        return super.getAuthorizeUrl(AUTH_URL, params);
    }

    /**
     * @return String    返回类型
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException   获取token
     */
    public String getTokenByCode(String code) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", getClientId());
        params.put("client_secret", getClientSecret());
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", getRedirectUri());
        String token = TokenUtil.getAccessToken(super.doPost(TOKEN_URL, params));
        LOGGER.debug(token);
        return token;
    }

    /**
     * 获取用户信息
     *
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public JSONObject getUserInfo(String accessToken) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        Map<String, String> params = new HashMap<>();
        params.put("Authorization", "Bearer " + accessToken);
        String userInfo = super.doGetWithHeaders(USER_INFO_URL, params);
        JSONObject dataMap = JSON.parseObject(userInfo);
        LOGGER.debug(dataMap.toJSONString());
        return dataMap;
    }

    /**
     * @return void    返回类型
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException   根据code一步获取用户信息
     */
    public JSONObject getUserInfoByCode(String code) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        String accessToken = getTokenByCode(code);
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        JSONObject dataMap = getUserInfo(accessToken);
        dataMap.put("access_token", accessToken);
        LOGGER.debug(dataMap);
        return dataMap;
    }
}