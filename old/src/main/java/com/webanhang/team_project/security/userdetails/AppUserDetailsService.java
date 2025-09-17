package com.webanhang.team_project.security.userdetails;

import com.webanhang.team_project.model.User;
import com.webanhang.team_project.repository.UserRepository;
// import jakarta.persistence.EntityNotFoundException; // Không thấy dùng
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, DisabledException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        if (user.isBanned()) {
            throw new DisabledException("your account has been banned");
        }

        if (!user.isActive()) {
            throw new DisabledException("Account is not activated for email: " + email);
        }

        return AppUserDetails.buildUserDetails(user);
    }
}