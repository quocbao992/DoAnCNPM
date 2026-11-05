package vn.doancnpm.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
  @NotBlank(message = "Họ và tên không được để trống")
  private String fullName;

  @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
  private String phone;

  private LocalDate dateOfBirth;
  private Boolean gender;
}
