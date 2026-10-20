package vn.doancnpm.service;

import vn.doancnpm.domain.Topping;
import java.util.List;

public interface ToppingService {
    // Lấy danh sách các topping đang được bán
    List<Topping> getActiveToppings();

    // Thêm topping mới
    Topping createTopping(Topping topping);

    // Cập nhật thông tin topping
    Topping updateTopping(Long id, Topping newDetails);

    // Ngừng kinh doanh topping (Xóa mềm)
    void hideTopping(Long id);
}