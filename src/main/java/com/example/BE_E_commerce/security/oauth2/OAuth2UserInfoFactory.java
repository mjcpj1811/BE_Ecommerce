package com.example.BE_E_commerce.security.oauth2;

import com.example.BE_E_commerce.enums.AuthProvider;
import com.example.BE_E_commerce.exception.BadRequestException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.FACEBOOK.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        } else {
            throw new BadRequestException("Sorry!  Login with " + registrationId + " is not supported yet.");
        }
    }
}