package vn.doancnpm.service.implement;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;
import java.util.Locale;

import vn.doancnpm.domain.Customer;
import vn.doancnpm.domain.User;
import vn.doancnpm.dto.Request.LoginRequest;
import vn.doancnpm.dto.Request.RegisterRequest;
import vn.doancnpm.dto.Response.LoginResponse;
import vn.doancnpm.dto.Response.RegisterResponse;
import vn.doancnpm.dto.Response.UserResponse;
import vn.doancnpm.repository.UserRepository;
import vn.doancnpm.service.AuthService;
import vn.doancnpm.repository.CustomerRepository;


@Service
@RequiredArgsConstructor
@Transactional
public class IAuthService implements AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private UserResponse covertToResponse(User user) {
        return new UserResponse(user.getId(),user.getUsername(),user.getFullName(),user.getRole());
    }

    private RegisterResponse convertToRegisterResponse(User user) {
        return new RegisterResponse(user.getId(),user.getUsername(),user.getFullName(),user.getRole());
    }

    private String normalizePhone(String phone) {
        String normalizedPhone = phone.trim();

        if (normalizedPhone.startsWith("+84")) {
            normalizedPhone =
                    "0" + normalizedPhone.substring(3);
        }

        return normalizedPhone;
    }

    

    @Override 
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if(!user.isStatus()) {
            throw new  DisabledException("Tài khoản đã bị khóa");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Tên đăng nhặp hoặc mật khẩu không chính xác");
        }

        UserResponse userResponse = covertToResponse(user);

        return new LoginResponse(null, userResponse);

    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        String fullName = request.getFullName().trim();
        String phone = normalizePhone(request.getPhone());

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập đã được sử dụng"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email đã được sử dụng"
            );
        }

        if (customerRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "Số điện thoại đã được sử dụng"
            );
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        user.setStatus(true);

        User savedUser = userRepository.save(user);

        Customer customer = Customer.builder().user(savedUser).phone(phone).date(request.getDateOfBirth()).gender(request.getGender()).loyaltyP(0).build();

        customerRepository.save(customer);

        return convertToRegisterResponse(savedUser);
    }
}
