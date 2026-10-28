package vn.doancnpm.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doancnpm.domain.Category;
import vn.doancnpm.domain.Product;
import vn.doancnpm.repository.CategoryRepository;
import vn.doancnpm.repository.ProductRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Khởi tạo danh mục và sản phẩm mẫu vào cơ sở dữ liệu...");

            Category catTrad = categoryRepository.save(Category.builder().name("Cà Phê Truyền Thống").displayOrder(1L).status(true).build());
            Category catMachine = categoryRepository.save(Category.builder().name("Cà Phê Pha Máy").displayOrder(2L).status(true).build());
            Category catColdBrew = categoryRepository.save(Category.builder().name("Cold Brew Đặc Biệt").displayOrder(3L).status(true).build());
            Category catTea = categoryRepository.save(Category.builder().name("Trà Trái Cây").displayOrder(4L).status(true).build());
            Category catPastry = categoryRepository.save(Category.builder().name("Bánh Ngọt Ăn Kèm").displayOrder(5L).status(true).build());

            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                    Product.builder().name("Cà Phê Sữa Đá Sài Gòn").category(catTrad).unitPrice(BigDecimal.valueOf(29000)).stock(100).status(true).imgURL("cf-sua-da.jpg").build(),
                    Product.builder().name("Cà Phê Đen Đá Đậm Vị").category(catTrad).unitPrice(BigDecimal.valueOf(25000)).stock(100).status(true).imgURL("cf-den-da.jpg").build(),
                    Product.builder().name("Bạc Xỉu Sữa Tươi 3 Tầng").category(catTrad).unitPrice(BigDecimal.valueOf(35000)).stock(100).status(true).imgURL("bac-xiu.jpg").build(),
                    Product.builder().name("Cà Phê Cốt Dừa Latte").category(catMachine).unitPrice(BigDecimal.valueOf(45000)).stock(80).status(true).imgURL("caffe-latte.jpg").build(),
                    Product.builder().name("Caramel Macchiato Thơm Béo").category(catMachine).unitPrice(BigDecimal.valueOf(49000)).stock(80).status(true).imgURL("caramel-macchiato.jpg").build(),
                    Product.builder().name("Cold Brew Cam Sả Mát Lạnh").category(catColdBrew).unitPrice(BigDecimal.valueOf(48000)).stock(60).status(true).imgURL("cold-brew.jpg").build(),
                    Product.builder().name("Trà Đào Cam Sả Tươi Mát").category(catTea).unitPrice(BigDecimal.valueOf(39000)).stock(90).status(true).imgURL("tra-dao.jpg").build(),
                    Product.builder().name("Bánh Sừng Bò Croissant Bơ Pháp").category(catPastry).unitPrice(BigDecimal.valueOf(32000)).stock(50).status(true).imgURL("croissant.jpg").build()
                ));
            }
            log.info("Hoàn tất khởi tạo dữ liệu.");
        }
    }
}
