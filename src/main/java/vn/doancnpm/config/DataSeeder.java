package vn.doancnpm.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doancnpm.domain.Category;
import vn.doancnpm.domain.Customer;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.OrderItem;
import vn.doancnpm.domain.Product;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.domain.User;
import vn.doancnpm.repository.CategoryRepository;
import vn.doancnpm.repository.CustomerRepository;
import vn.doancnpm.repository.OrderRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.repository.ToppingRepository;
import vn.doancnpm.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Kiem tra va khoi tao du lieu mau he thong (DataSeeder)...");
        seedUsersAndCustomers();
        seedCategories();
        seedToppings();
        seedProducts();
        seedOrders();
        log.info("DataSeeder hoan tat thanh cong.");
    }

    private void seedUsersAndCustomers() {
        seedUser("admin", "Admin BrewLite", "admin@brewlite.vn", "Admin@123", "ADMIN", null);
        seedUser("barista01", "Nguyen Van An", "barista01@brewlite.vn", "Barista@123", "BARISTA", "0901111001");
        seedUser("cashier01", "Tran Thi Binh", "cashier01@brewlite.vn", "Cashier@123", "CASHIER", "0901111002");
        seedUser("customer01", "Pham Quoc Bao", "customer01@brewlite.vn", "Customer@123", "CUSTOMER", "0901111003");
        seedUser("customer02", "Le Thi Mai", "customer02@brewlite.vn", "Customer@123", "CUSTOMER", "0901111004");
        seedUser("customer03", "Nguyen Hoang Nam", "customer03@brewlite.vn", "Customer@123", "CUSTOMER", "0901111005");
    }

    private User seedUser(String username, String fullName, String email,
                          String rawPassword, String role, String phone) {
        return userRepository.findByUsername(username).orElseGet(() -> {
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
                        .loyaltyP(120)
                        .build();
                customerRepository.save(customer);
            }
            log.info("Da tao tai khoan [{}] username='{}'", role, username);
            return saved;
        });
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("Ca Phe Truyen Thong").displayOrder(1L).status(true).build());
            categoryRepository.save(Category.builder().name("Espresso & Ca Phe Y").displayOrder(2L).status(true).build());
            categoryRepository.save(Category.builder().name("Cold Brew Dac Biet").displayOrder(3L).status(true).build());
            categoryRepository.save(Category.builder().name("Tra Trai Cay").displayOrder(4L).status(true).build());
            categoryRepository.save(Category.builder().name("Banh Ngot An Kem").displayOrder(5L).status(true).build());
            log.info("Da khoi tao danh muc.");
        }

        // Đảm bảo danh mục Topping luôn tồn tại
        if (!categoryRepository.existsByNameIgnoreCase("Topping")) {
            Category catTopping = categoryRepository.save(Category.builder().name("Topping").displayOrder(6L).status(true).build());
            log.info("Da them danh muc Topping vao he thong.");

            // Thêm các món Topping vào danh mục để hiển thị trên thực đơn
            if (!productRepository.existsByNameIgnoreCase("Tran Chau Trang")) {
                productRepository.save(Product.builder().name("Tran Chau Trang").category(catTopping).unitPrice(BigDecimal.valueOf(10000)).stock(200).status(true).build());
            }
            if (!productRepository.existsByNameIgnoreCase("Thach Ca Phe")) {
                productRepository.save(Product.builder().name("Thach Ca Phe").category(catTopping).unitPrice(BigDecimal.valueOf(10000)).stock(200).status(true).build());
            }
            if (!productRepository.existsByNameIgnoreCase("Kem Cheese Beo")) {
                productRepository.save(Product.builder().name("Kem Cheese Beo").category(catTopping).unitPrice(BigDecimal.valueOf(15000)).stock(200).status(true).build());
            }
            if (!productRepository.existsByNameIgnoreCase("Sot Caramel")) {
                productRepository.save(Product.builder().name("Sot Caramel").category(catTopping).unitPrice(BigDecimal.valueOf(8000)).stock(200).status(true).build());
            }
        }
    }

    private void seedToppings() {
        if (toppingRepository.count() == 0) {
            toppingRepository.save(Topping.builder().name("Tran Chau Trang").unitPrice(BigDecimal.valueOf(10000)).status(true).build());
            toppingRepository.save(Topping.builder().name("Thach Ca Phe").unitPrice(BigDecimal.valueOf(10000)).status(true).build());
            toppingRepository.save(Topping.builder().name("Kem Cheese Beo").unitPrice(BigDecimal.valueOf(15000)).status(true).build());
            toppingRepository.save(Topping.builder().name("Sot Caramel").unitPrice(BigDecimal.valueOf(8000)).status(true).build());
            log.info("Da khoi tao toppings.");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();
            if (categories.isEmpty()) return;

            Category catTrad = categories.get(0);
            Category catMachine = categories.size() > 1 ? categories.get(1) : catTrad;
            Category catColdBrew = categories.size() > 2 ? categories.get(2) : catTrad;
            Category catTea = categories.size() > 3 ? categories.get(3) : catTrad;
            Category catPastry = categories.size() > 4 ? categories.get(4) : catTrad;

            productRepository.saveAll(List.of(
                Product.builder().name("Ca Phe Sua Da Sai Gon").category(catTrad).unitPrice(BigDecimal.valueOf(29000)).stock(100).status(true).imgURL("cf-sua-da.jpg").build(),
                Product.builder().name("Ca Phe Den Da Dam Vi").category(catTrad).unitPrice(BigDecimal.valueOf(25000)).stock(100).status(true).imgURL("cf-den-da.jpg").build(),
                Product.builder().name("Bac Xiu Kem Sua 3 Tang").category(catTrad).unitPrice(BigDecimal.valueOf(35000)).stock(100).status(true).imgURL("bac-xiu.jpg").build(),
                Product.builder().name("Caffe Latte Thom Beo").category(catMachine).unitPrice(BigDecimal.valueOf(45000)).stock(80).status(true).imgURL("caffe-latte.jpg").build(),
                Product.builder().name("Caramel Macchiato").category(catMachine).unitPrice(BigDecimal.valueOf(49000)).stock(80).status(true).imgURL("caramel-macchiato.jpg").build(),
                Product.builder().name("Cold Brew Cam Sa Mat Lanh").category(catColdBrew).unitPrice(BigDecimal.valueOf(48000)).stock(60).status(true).imgURL("cold-brew.jpg").build(),
                Product.builder().name("Tra Dao Cam Sa Tuoi Mat").category(catTea).unitPrice(BigDecimal.valueOf(39000)).stock(90).status(true).imgURL("tra-dao.jpg").build(),
                Product.builder().name("Banh Sung Bo Croissant Bo Phap").category(catPastry).unitPrice(BigDecimal.valueOf(32000)).stock(50).status(true).imgURL("croissant.jpg").build()
            ));
            log.info("Da khoi tao san pham.");
        }
    }

    private void seedOrders() {
        if (orderRepository.count() == 0) {
            List<Customer> customers = customerRepository.findAll();
            List<Product> products = productRepository.findAll();
            if (customers.isEmpty() || products.isEmpty()) return;

            Customer c1 = customers.get(0);
            Customer c2 = customers.size() > 1 ? customers.get(1) : c1;
            Customer c3 = customers.size() > 2 ? customers.get(2) : c1;

            Product p1 = products.get(0);
            Product p2 = products.size() > 1 ? products.get(1) : p1;
            Product p3 = products.size() > 2 ? products.get(2) : p1;
            Product p4 = products.size() > 3 ? products.get(3) : p1;

            // Don 1: PAID (cho pha che)
            createOrder(c1, "ORD-20261028-001", Order.Status.PAID, Order.OrderType.DINE_IN, "Ban 02", Order.PaymentMethod.MOMO,
                    List.of(createItem(p1, "M", 2, p1.getUnitPrice()), createItem(p3, "L", 1, p3.getUnitPrice())));

            // Don 2: PREPARING (dang pha che)
            createOrder(c2, "ORD-20261028-002", Order.Status.PREPARING, Order.OrderType.TAKE_AWAY, null, Order.PaymentMethod.VNPAY,
                    List.of(createItem(p4, "L", 2, p4.getUnitPrice())));

            // Don 3: READY (san sang giao)
            createOrder(c3, "ORD-20261028-003", Order.Status.READY, Order.OrderType.DINE_IN, "Ban 05", Order.PaymentMethod.CASH,
                    List.of(createItem(p2, "M", 1, p2.getUnitPrice()), createItem(p1, "L", 1, p1.getUnitPrice())));

            // Don 4: COMPLETED (da hoan tat)
            createOrder(c1, "ORD-20261028-004", Order.Status.COMPLETED, Order.OrderType.TAKE_AWAY, null, Order.PaymentMethod.MOMO,
                    List.of(createItem(p3, "M", 1, p3.getUnitPrice())));

            // Don 5: CANCELLED (da huy)
            createOrder(c2, "ORD-20261028-005", Order.Status.CANCELLED, Order.OrderType.DINE_IN, "Ban 01", Order.PaymentMethod.CASH,
                    List.of(createItem(p4, "M", 1, p4.getUnitPrice())));

            log.info("Da khoi tao don hang mau.");
        }
    }

    private void createOrder(Customer customer, String orderCode, Order.Status status,
                             Order.OrderType orderType, String tableNumber, Order.PaymentMethod paymentMethod,
                             List<OrderItem> items) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderCode(orderCode);
        order.setStatus(status);
        order.setOrderType(orderType);
        order.setTableNumber(tableNumber);
        order.setPaymentMethod(paymentMethod);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setLoyaltyPointsUsed(0);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            item.setOrder(order);
            subtotal = subtotal.add(item.getLineTotal());
        }
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal);
        order.setItems(new ArrayList<>(items));

        orderRepository.save(order);
    }

    private OrderItem createItem(Product product, String size, int qty, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductImage(product.getImgURL());
        item.setSize(size);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(qty)));
        return item;
    }
}
