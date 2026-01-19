package com.example.BE_E_commerce.service;

import com.example.BE_E_commerce.entity.User;
import com.example.BE_E_commerce.enums.AuthProvider;
import com.example.BE_E_commerce.enums.UserRole;
import com.example.BE_E_commerce.exception.BadRequestException;
import com.example.BE_E_commerce.repository.UserRepository;
import com.example.BE_E_commerce.security.jwt.UserDetailsImpl;
import com.example.BE_E_commerce.security.oauth2.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oAuth2User.getAttributes()
        );

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new BadRequestException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            // Check if user is trying to login with different provider
            if (!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                throw new BadRequestException(
                        "Looks like you're signed up with " + user.getProvider() +
                                " account. Please use your " + user.getProvider() + " account to login."
                );
            }

            // Update existing user
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            // Register new user
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserDetailsImpl.build(user);
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        // Generate unique username from email
        String username = generateUniqueUsername(oAuth2UserInfo.getEmail());

        User user = User.builder()
                .username(username)
                .email(oAuth2UserInfo.getEmail())
                .fullName(oAuth2UserInfo.getName())
                .avatarUrl(oAuth2UserInfo.getImageUrl())
                .provider(provider)
                .providerId(oAuth2UserInfo.getId())
                .emailVerified(true) // OAuth2 users are automatically verified
                .role(UserRole.BUYER)
                .build();

        user = userRepository.save(user);

        log.info("New user registered via {}:  {} (ID: {})", provider, user.getEmail(), user.getId());

        return user;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // Update user info from OAuth2 provider
        if (oAuth2UserInfo.getName() != null) {
            existingUser.setFullName(oAuth2UserInfo.getName());
        }
        if (oAuth2UserInfo.getImageUrl() != null) {
            existingUser.setAvatarUrl(oAuth2UserInfo.getImageUrl());
        }

        existingUser = userRepository.save(existingUser);

        log.info("User updated via {}: {} (ID: {})", existingUser.getProvider(), existingUser.getEmail(), existingUser.getId());

        return existingUser;
    }

    private String generateUniqueUsername(String email) {
        // Extract username from email
        String baseUsername = email.split("@")[0].toLowerCase();

        // Remove special characters
        baseUsername = baseUsername.replaceAll("[^a-z0-9_]", "_");

        String username = baseUsername;
        int counter = 1;

        // Keep trying until we find a unique username
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}