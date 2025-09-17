package com.webanhang.team_project.service.user;

import com.webanhang.team_project.dto.address.AddAddressRequest;
import com.webanhang.team_project.dto.address.AddressDTO;
import com.webanhang.team_project.dto.user.BasicUserDTO;
import com.webanhang.team_project.dto.user.UserDTO;
import com.webanhang.team_project.enums.UserRole;
import com.webanhang.team_project.model.Address;
import com.webanhang.team_project.model.Role;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.repository.AddressRepository;
import com.webanhang.team_project.repository.RoleRepository;
import com.webanhang.team_project.repository.UserRepository;
import com.webanhang.team_project.dto.user.CreateUserRequest;
import com.webanhang.team_project.dto.auth.OtpVerificationRequest;
import com.webanhang.team_project.dto.auth.RegisterRequest;
import com.webanhang.team_project.dto.user.UpdateUserRequest;
import com.webanhang.team_project.security.jwt.JwtUtils;
import com.webanhang.team_project.security.otp.OtpService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AddressRepository addressRepository;
    private final JwtUtils jwtUtils;

    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setFirstName(req.getFirstName());
                    user.setLastName(req.getLastName());
                    user.setEmail(req.getEmail());
                    user.setPassword(passwordEncoder.encode(req.getPassword()));

                    // role mặc định là CUSTOMER
                    Role customerRole = roleRepository.findByName(UserRole.CUSTOMER)
                            .orElseThrow(() -> new RuntimeException("Role CUSTOMER không tìm thấy"));
                    user.setRole(customerRole);

                    return userRepository.save(user);
                }).orElseThrow(() -> new EntityExistsException("Email " + request.getEmail() + " already be used"));
    }

    @Override
    @Transactional // Annotation này quan trọng để đảm bảo tính nhất quán của giao dịch
    public UserDTO updateUser(UpdateUserRequest request, Long userId) {
        User existingUser = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPhone(request.getPhoneNumber());

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresentOrElse(userRepository::delete, () -> {
            throw new EntityNotFoundException("User not found");
        });
    }

    @Transactional
    @Override
    public UserDTO convertUserToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole() != null && user.getRole().getName() != null ? user.getRole().getName().name() : "UNKNOWN");
        userDTO.setMobile(user.getPhone());
        userDTO.setActive(user.isActive());
        userDTO.setAddresses(user.getAddress());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setImageUrl(user.getImageUrl());
        userDTO.setOauthProvider(user.getOauthProvider());
        return userDTO;
    }

    @Transactional
    @Override
    public void registerUser(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);

// Gán vai trò CUSTOMER
        Role role = roleRepository.findByName(UserRole.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.CUSTOMER)));

        user.setRole(role);
        userRepository.save(user);

        // Tạo và gửi OTP
        String otp = otpService.generateOtp(request.getEmail());
        otpService.sendOtpEmail(request.getEmail(), otp);
    }

    @Override
    public boolean verifyOtp(OtpVerificationRequest request) {
        // Kiểm tra OTP và kích hoạt tài khoản nếu hợp lệ
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        return isValid;
    }

    @Override
    public UserDTO findUserProfileByJwt(String jwt) {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // Remove "Bearer " prefix
        }
        String email = jwtUtils.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new EntityNotFoundException("User not found " + email);
        }
        return convertUserToDto(user);
    }

    @Override
    public User findUserByJwt(String jwt)  {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // Remove "Bearer " prefix
        }
        String email = jwtUtils.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new EntityNotFoundException("User not found " + email);
        }
        return user;
    }

    @Override
    public AddressDTO addUserAddress(User user, AddAddressRequest request) {
        List<Address> address=user.getAddress();
        if(address==null){
            address=new ArrayList<>();
        }
        Address newAddress=new Address();
        newAddress.setFullName(request.getFullName());
        newAddress.setProvince(request.getProvince());
        newAddress.setDistrict(request.getDistrict());
        newAddress.setWard(request.getWard());
        newAddress.setStreet(request.getStreet());
        newAddress.setNote(request.getNote());
        newAddress.setPhoneNumber(request.getPhoneNumber());
        newAddress.setUser(user);
        address.add(newAddress);
        addressRepository.save(newAddress);
        return new AddressDTO(newAddress);
    }

    @Override
    @Transactional
    public BasicUserDTO changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role not valid: " + roleName);
        }

        if (userRole == UserRole.ADMIN) {
            throw new IllegalArgumentException("Can't change to role ADMIN");
        }

        Role role = roleRepository.findByName(userRole)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));
        user.setRole(role);

        User res = userRepository.save(user);
        BasicUserDTO userDTO = convertToBasicDto(res);
        return userDTO;
    }

    public void forgotPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public BasicUserDTO convertToBasicDto(User user) {
        BasicUserDTO dto = new BasicUserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMobile(user.getPhone());
        dto.setActive(user.isActive());
        dto.setRole(user.getRole() != null && user.getRole().getName() != null ?
                user.getRole().getName().name() : "UNKNOWN");
        dto.setCreatedAt(user.getCreatedAt());
        dto.setImageUrl(user.getImageUrl());
        dto.setOauthProvider(user.getOauthProvider());
        return dto;
    }
}
