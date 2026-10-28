package vn.doancnpm.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doancnpm.domain.Customer;
import vn.doancnpm.domain.User;
import vn.doancnpm.repository.CustomerRepository;
import vn.doancnpm.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin",      "Admin BrewLite",   "admin@brewlite.vn",      "Admin@123",   "ADMIN",    null);
        seedUser("barista01",  "Nguyễn Văn A",     "barista01@brewlite.vn",  "Barista@123", "BARISTA",  "0901111001");
        seedUser("cashier01",  "Trần Thị B",       "cashier01@brewlite.vn",  "Cashier@123", "CASHIER",  "0901111002");
        seedUser("customer01", "Phạm Quốc Bảo",    "customer01@brewlite.vn", "Customer@123","CUSTOMER", "0901111003");
        log.info("DataSeeder: Seeding hoàn tất.");
    }

    
    private void seedUser(String username, String fullName, String email,
                          String rawPassword, String role, String phone) {
        if (userRepository.existsByUsername(username)) {
            log.info("Bỏ qua '{}' — đã tồn tại.", username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(true);
        User saved = userRepository.save(user);

        if ("CUSTOMER".equals(role)) {
            Customer customer = Customer.builder()
                    .user(saved)
                    .phone(phone)
                    .gender(true)
                    .date(LocalDate.of(2000, 1, 1))
                    .loyaltyP(0)
                    .build();
            customerRepository.save(customer);
        }

        log.info("Đã tạo tài khoản [{}] username='{}'  password='{}'", role, username, rawPassword);
    }
}
