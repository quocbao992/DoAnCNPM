package vn.doancnpm.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class UserRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(
        min = 3,
        max = 100,
        message = "Tên đăng nhập phải có từ 3 đến 100 ký tự"
    )
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới"
    )
    private String username;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(
        min = 2,
        max = 100,
        message = "Họ tên phải có từ 2 đến 100 ký tự"
    )
    private String fullName;

    

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,32}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;

    @NotBlank(message = "Vai trò không được để trống")
    @Pattern(
        regexp = "^(?i)(CUSTOMER|CASHIER|ADMIN|BARISTA)$",
        message = "Vai trò phải là CUSTOMER, CASHIER, BARISTA hoặc ADMIN"
    )
    private String role;

    private String phone;

    public UserRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
