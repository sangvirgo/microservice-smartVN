package com.smartvn.product_service.client;

import com.smartvn.product_service.dto.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceFallback implements UserServiceClient {

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        log.warn("Fallback: Cannot fetch user info for userId {}. Returning anonymous user.", userId);

        UserInfoDTO fallbackUser = new UserInfoDTO();
        fallbackUser.setId(userId);
        fallbackUser.setFirstName("Anonymous");
        fallbackUser.setLastName("User");
        fallbackUser.setAvatar(null);

        return fallbackUser;
    }
}