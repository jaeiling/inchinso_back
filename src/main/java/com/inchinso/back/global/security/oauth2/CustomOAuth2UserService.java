package com.inchinso.back.global.security.oauth2;

import com.inchinso.back.domain.user.entity.Role;
import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String profileImageUrl = (String) attributes.get("picture");

        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        boolean isNewUser = false;

        if (existing.isPresent()) {
            user = existing.get();
        } else {
            user = User.builder()
                    .email(email)
                    .name("")
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.USER)
                    .profileImageUrl(profileImageUrl)
                    .build();
            userRepository.save(user);
            isNewUser = true;
        }

        return new CustomOAuth2User(user, attributes, isNewUser);
    }
}
