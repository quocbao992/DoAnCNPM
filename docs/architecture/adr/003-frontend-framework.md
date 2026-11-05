# ADR 003: Lựa chọn Next.js và kiến trúc PWA cho Frontend

## Trạng thái
Đã chấp thuận (Accepted)

## Bối cảnh (Context)
Hệ thống đặt đồ uống BrewLite phục vụ hai nhóm đối tượng chính: khách hàng đặt món trực tiếp tại quầy thông qua quét mã QR trên thiết bị di động, và nhân viên/quản lý vận hành hệ thống đơn hàng trên máy tính/máy tính bảng. Đề bài Task 3.1 yêu cầu sử dụng Next.js kết hợp PWA (Progressive Web App). Nhóm cần một giải pháp frontend đảm bảo tốc độ tải trang tức thì, hỗ trợ trải nghiệm di động mượt mà như ứng dụng gốc (native app) mà không yêu cầu khách hàng cài đặt từ kho ứng dụng.

## Quyết định (Decision)
Nhóm quyết định sử dụng **Next.js (React Framework)** kết hợp mô hình **Progressive Web App (PWA)** làm nền tảng phát triển toàn bộ giao diện người dùng.

## Hệ quả (Consequences / Trade-offs)
* **Ưu điểm:**
  * **Trải nghiệm PWA tối ưu:** Cho phép thêm ứng dụng vào màn hình chính (Add to Home Screen), tận dụng Service Worker để cache tài nguyên tĩnh, giúp khách hàng mở menu và đặt món nhanh chóng ngay cả khi mạng chập chờn.
  * **Hiệu năng & Tối ưu tải trang:** Cơ chế kết hợp Server-Side Rendering (SSR) / Static Site Generation (SSG) của Next.js giúp hiển thị thực đơn đồ uống với thời gian First Contentful Paint (FCP) dưới 1.5 giây.
  * **Tái sử dụng linh hoạt:** Dễ dàng xây dựng giao diện Responsive chuẩn Mobile-first cho khách hàng và bố cục bảng điều khiển (Dashboard) cho nhân viên Barista/Admin trên cùng một codebase.
* **Nhược điểm (Trade-off):**
  * Quá trình cấu hình Service Worker và chiến lược bộ nhớ đệm (Cache Strategy) đòi hỏi kiểm thử kỹ lưỡng để tránh hiện tượng khách hàng nhìn thấy menu cũ chưa cập nhật giá.
  * Độ phức tạp xây dựng và cấu hình bundle cao hơn so với một ứng dụng Single Page Application (SPA) truyền thống bằng React thuần (Vite/CRA).