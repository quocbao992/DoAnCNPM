# TASK 1.3: ĐẶC TẢ MỘT USE-CASE TRỌNG TÂM (BREWLITE)

## 1. Vẽ use-case diagram
```mermaid
flowchart LR
    Customer([" Khách hàng"])

    subgraph DatHang ["Đặt hàng"]
        direction LR
        UC_Order(["Đặt đơn hàng"])
        UC_AddCart(["Thêm sản phẩm vào giỏ hàng"])
        UC_CheckCart(["Kiểm tra giỏ hàng"])
        UC_Topping(["Chọn topping"])
        UC_Size(["Chọn size"])
        UC_Qty(["Cập nhật số lượng"])
        UC_Note(["Thêm ghi chú (note)"])

        UC_Order -. "&lt;&lt;include&gt;&gt;" .-> UC_AddCart
        UC_Order -. "&lt;&lt;include&gt;&gt;" .-> UC_CheckCart

        UC_Topping -. "&lt;&lt;extend&gt;&gt;" .-> UC_AddCart
        UC_Size -. "&lt;&lt;extend&gt;&gt;" .-> UC_AddCart
        UC_Qty -. "&lt;&lt;extend&gt;&gt;" .-> UC_AddCart
        UC_Note -. "&lt;&lt;extend&gt;&gt;" .-> UC_AddCart
    end

    Customer --- UC_Order
```

```mermaid
flowchart LR
    Customer([" Khách hàng"])
    Gateway([" Cổng thanh toán"])

    subgraph ThanhToan ["Thanh Toán"]
        direction LR
        UC_Pay(["Thanh toán đơn hàng"])
        UC_PayMethod(["Chọn phương thức thanh toán"])
        UC_Voucher(["Áp dụng mã giảm giá"])

        UC_Pay -. "&lt;&lt;include&gt;&gt;" .-> UC_PayMethod
        UC_Voucher -. "&lt;&lt;extend&gt;&gt;" .-> UC_Pay
    end

    Customer --- UC_Pay
    UC_Pay --- Gateway
```

## 2. Đặc tả use-case Đặt đơn hàng (Sơ đồ đặt hàng)
|  **Thuộc tính** | **Nội dung mô tả chi tiết** |
|---|---|
| **Use-case ID** | UC-01 |
| **Use-case name** | Đặt đơn hàng |
| **Actor** | Khách hàng (Primary Actor). |
| **Requirement IDs** | FR-ORD-01, FR-ORD-02, FR-ORD-03, FR-ORD-04, FR-ORD-05, FR-ORD-06, FR-ORD-07 |
| **Description** | Khách hàng có thể sử dụng chức năng này để chọn sản phẩm từ thực đơn của cửa hàng thêm vào giỏ hàng, cùng với các lựa chọn tùy chỉnh khác (chọn size, chọn topping, cập nhật số lượng, thêm ghi chú), kiểm tra lại giỏ hàng và xác nhận đặt đơn hàng. |
| **Trigger** | Khách hàng truy cập vào màn hình thực đơn (Menu), chọn sản phẩm thêm vào giỏ hàng và nhấn nút "Đặt đơn hàng". |
| **Preconditions** | 1. Hệ thống đang hoạt động ổn định và kết nối cơ sở dữ liệu thành công.<br>2. Danh mục thực đơn (sản phẩm, size, topping, đơn giá, tồn kho) đã được cấu hình và đang ở trạng thái mở bán.<br>3. Khách hàng đã truy cập vào màn hình đặt món của hệ thống. |
| **Postconditions** | **Thành công:** Giỏ hàng được kiểm tra hợp lệ; đơn hàng mới được khởi tạo ở trạng thái `PENDING` (Chờ thanh toán), lưu đầy đủ danh sách món, size, topping, số lượng, ghi chú và tổng tiền tạm tính; hệ thống chuyển khách hàng sang màn hình Thanh toán đơn hàng.<br><br>**Thất bại:** Đơn hàng không được khởi tạo; trạng thái giỏ hàng hiện tại của khách hàng vẫn được giữ nguyên để khách hàng chỉnh sửa. |
| **Normal flow** | **1.** Hệ thống hiển thị màn hình đặt món và danh sách thực đơn (Menu) cho khách hàng.<br><br>**2.** (`<<include>>` Thêm sản phẩm vào giỏ hàng): Khách hàng lựa chọn sản phẩm từ thực đơn.<br><br>**3.** Hệ thống hiển thị chi tiết sản phẩm (size mặc định, số lượng mặc định là 1, danh sách topping đi kèm, ô nhập ghi chú).<br><br>**4.** Khách hàng tùy chọn size, topping, cập nhật số lượng và nhập ghi chú nếu cần, sau đó xác nhận "Thêm sản phẩm vào giỏ hàng".<br><br>**5.** Hệ thống lưu sản phẩm cùng các thông tin tùy chỉnh vào giỏ hàng và cập nhật tổng số lượng món trong giỏ.<br><br>**6.** (`<<include>>` Kiểm tra giỏ hàng): Khách hàng mở giỏ hàng để kiểm tra lại danh sách sản phẩm đã chọn, các tùy chỉnh đi kèm và tổng tiền tạm tính.<br><br>**7.** Khách hàng xác nhận đặt hàng sau khi kiểm tra thông tin giỏ hàng.<br><br>**8.** Hệ thống kiểm tra xem kho có đủ nguyên liệu để hoàn thành đơn hàng hay không.<br><br>**9.** Hệ thống tạo đơn hàng ở trạng thái PENDING, lưu thông tin đơn hàng và tổng tiền tạm tính.<br><br>**10.** Hệ thống chuyển khách hàng sang màn hình thanh toán đơn hàng để tiếp tục thực tiện chức năng thanh toán. |
| **Exceptions** | **EX-01 (Đơn hàng chứa món không đủ nguyên liệu):** Tại bước 8, hệ thống phát hiệu kho nguyên liệu để đáp ứng số lượng món trong đơn hàng. Hệ thống thông báo tên món không đủ nguyên liệu, không tạo đơn hàng và yêu cầu khách điều chỉnh giỏ hàng.<br><br>**EX-02 (Giỏ hàng trống):** Tại bước 6, khách hàng mở giỏ hàng nhưng chưa có sản phẩm nào, hoặc tại bước 7 khách hàng xác nhận đặt hàng khi thấy giỏ hàng trống. Hệ thống thông báo "Giỏ hàng đang trống, vui lòng thêm sản phẩm" và không cho phép tiếp tục đặt hàng.<br><br>**EX-03 (Số lượng không hợp lệ):** Tại bước 4 hoặc bước 6, khách hàng nhập số lượng nhỏ hơn hoặc bằng 0 hoặc vượt quá giới hạn cho phép. Hệ thống thông báo lỗi, yêu cầu khách hàng nhập lại số lượng hợp lệ và không tiếp tục xác nhận đặt hàng cho đến khi dữ liệu được điều chỉnh.<br><br>**EX-04 (Mất kết nối khi đặt hàng):** Tại bước 5, bước 6, bước 8 hoặc bước 9, hệ thống gặp lỗi kết nối mạng hoặc cơ sở dữ liệu trong quá trình cập nhật giỏ hàng, kiểm tra nguyên liệu hoặc tạo đơn hàng. Hệ thống thông báo lỗi, giữ lại dữ liệu giỏ hàng nếu có thể và bảo đảm không tạo trùng đơn hàng khi khách hàng thử lại.<br><br>**EX-05 (Mất kết nối khi chuyển sang thanh toán):** Tại bước 10, kết nối mạng bị gián đoạn khi hệ thống chuyển khách hàng sang màn hình thanh toán. Hệ thống thông báo lỗi kết nối và cho phép khách hàng thử lại. Đơn hàng đã được tạo vẫn được giữ nguyên; hệ thống không tạo thêm đơn hàng mới khi khách hàng thực hiện thao tác. |
| **Alternative flows** | **AF-01 (`<<extend>>` Chọn size - tại bước 3):** Khách hàng điều chỉnh kích cỡ sản phẩm (Size S, M, L). Hệ thống cập nhật lại đơn giá theo size rồi tiếp tục bước 4.<br><br>**AF-02 (`<<extend>>` Chọn topping - tại bước 3):** Khách hàng chọn thêm một hoặc nhiều loại topping ăn kèm. Hệ thống cộng thêm tiền topping vào giá món rồi tiếp tục bước 4.<br><br>**AF-03 (`<<extend>>` Cập nhật số lượng - tại bước 4 hoặc bước 6):** Khách hàng thay đổi số lượng sản phẩm khi tùy chỉnh món hoặc kiểm tra giỏ hàng. Hệ thống cập nhật số lượng, tính lại thành tiền của món và tổng tiền giỏ hàng. Nếu khách hàng xóa sản phẩm tại bước 6, hệ thống cập nhật giỏ hàng trước khi tiếp tục bước 7<br><br>**AF-04 (`<<extend>>` Thêm ghi chú (note) - tại bước 3):** Khách hàng nhập ghi chú riêng cho sản phẩm, chẳng hạn “Ít đá, 50% đường”. Hệ thống lưu ghi chú kèm theo sản phẩm trong giỏ hàng và tiếp tục bước 4 của luồng chính.<br><br>**AF-05 (Chọn món thay thế khi hết nguyên liệu - tại bước 8):** Hệ thống thông báo món không đủ nguyên liệu. Khách hàng chọn món thay thế hoặc xóa món không khả dụng khỏi giỏ hàng. Hệ thống cập nhật giỏ hàng và tính lại tổng tiền, sau đó quay lại bước 7 để khách hàng xác nhận đặt hàng. Hệ thống thực hiện lại bước 8 để kiểm tra nguyên liệu trước khi tạo đơn hàng. |


## 3. Đặc tả use-case Thanh toán đơn hàng (Sơ đồ thanh toán)
| **Thuộc tính** | **Nội dung mô tả chi tiết** |
|---|---|
| **Use-case ID** | UC-02 |
| **Use-case name** | Thanh toán đơn hàng |
| **Actor** | **Primary Actor (Tác nhân chính):** Khách hàng.<br><br>**Secondary Actor (Tác nhân phụ):** Cổng thanh toán. |
| **Requirement IDs** | FR-PAY-01, FR-PAY-02, FR-PAY-03, FR-PAY-04, NFR-SEC-01 |
| **Description** | Khách hàng sử dụng chức năng này để thực hiện thanh toán cho đơn hàng đã đặt thông qua việc chọn phương thức thanh toán, tùy chọn áp dụng mã giảm giá (nếu có) và giao dịch qua Cổng thanh toán để hoàn tất đơn hàng. |
| **Trigger** | Khách hàng xác nhận đặt đơn hàng thành công ở UC-01 và được chuyển sang màn hình Thanh toán đơn hàng. |
| **Preconditions** | **1.** Đơn hàng đã được khởi tạo hợp lệ từ chức năng Đặt đơn hàng (UC-01) ở trạng thái `PENDING` với tổng tiền `> 0`.<br><br>**2.** Hệ thống kết nối thông suốt với API của Cổng thanh toán. |
| **Postconditions** | **Thành công:** Đơn hàng chuyển sang trạng thái `PAID` (Đã thanh toán); lưu lịch sử giao dịch; trừ lượt dùng mã giảm giá (nếu có); trừ tồn kho nguyên liệu; làm trống giỏ hàng và gửi đơn hàng cho phía nhà bếp xem.<br><br>**Thất bại:** Đơn hàng vẫn giữ trạng thái PENDING (Chờ thanh toán). Hệ thống thông báo thanh toán thất bại cho khách hàng và cho phép khách hàng thực hiện thanh toán lại.; không mất lượt dùng mã giảm giá; không trừ kho và cho phép khách hàng thực hiện thanh toán lại. |
| **Normal flow** | **1.** Hệ thống hiển thị màn hình Thanh toán đơn hàng gồm: thông tin đơn hàng, tổng tiền tạm tính, mục nhập mã giảm giá và danh sách phương thức thanh toán.<br><br>**2.** (`<<include>>` Chọn phương thức thanh toán): Khách hàng lựa chọn một phương thức thanh toán trực tuyến hợp lệ.<br><br>**3.** Hệ thống ghi nhận phương thức thanh toán và hiển thị tổng số tiền cần thanh toán cuối cùng.<br><br>**4.** Khách hàng nhấn nút "Thanh toán đơn hàng".<br><br>**5.** Hệ thống tạo phiên giao dịch và gửi thông tin thanh toán (mã đơn, số tiền, chữ ký bảo mật) sang Cổng thanh toán.<br><br>**6.** Cổng thanh toán hiển thị giao diện thanh toán (mã QR hoặc biểu mẫu thẻ) cho Khách hàng.<br><br>**7.** Khách hàng thực hiện xác nhận thanh toán trên giao diện của Cổng thanh toán.<br><br>**8.** Cổng thanh toán xử lý trừ tiền thành công và trả kết quả phản hồi (Callback/Webhook) về cho Hệ thống.<br><br>**9.** Hệ thống cập nhật trạng thái đơn hàng thành "Đã thanh toán", thông báo thành công cho Khách hàng và chuyển đơn hàng cho phía nhà bếp xem để chế biến. |
| **Exceptions** | **EX-01 (Mã giảm giá không hợp lệ):** Tại bước 1 hoặc bước 3, khách hàng nhập hoặc áp dụng mã giảm giá không tồn tại, đã hết hạn, hết lượt sử dụng hoặc đơn hàng chưa đạt giá trị tối thiểu. Hệ thống thông báo “Mã giảm giá không hợp lệ hoặc không đủ điều kiện”, không áp dụng mã giảm giá và giữ nguyên tổng tiền thanh toán trước khi giảm giá.<br><br>**EX-02 (Giao dịch thất bại tại Cổng thanh toán):** Tại bước 8, Cổng thanh toán trả về kết quả giao dịch thất bại do khách hàng không đủ số dư, nhập sai OTP hoặc thẻ bị khóa. Hệ thống thông báo “Thanh toán không thành công” kèm lý do nếu có, giữ đơn hàng ở trạng thái chưa thanh toán và cho phép khách hàng thực hiện thanh toán lại.<br><br>**EX-03 (Chưa chọn phương thức thanh toán):** Tại bước 2 hoặc bước 4, khách hàng chưa chọn phương thức thanh toán nhưng nhấn nút “Thanh toán đơn hàng”. Hệ thống thông báo yêu cầu chọn phương thức thanh toán và không tạo phiên giao dịch.<br><br>**EX-04 (Hết thời gian chờ thanh toán - Timeout):** Tại bước 6 hoặc bước 7, phiên giao dịch tại Cổng thanh toán hết thời gian cho phép (15 phút theo quy định của hệ thống). Hệ thống thông báo phiên thanh toán đã hết hạn và yêu cầu khách hàng kiểm tra trạng thái giao dịch trước khi thực hiện thanh toán lại.<br><br>**EX-05 (Mất kết nối thanh toán):** Tại bước 5, bước 6, bước 7 hoặc bước 8, kết nối mạng bị gián đoạn khiến hệ thống không nhận được kết quả giao dịch đúng thời điểm. Hệ thống thông báo chưa thể xác nhận kết quả thanh toán và kiểm tra lại trạng thái giao dịch với Cổng thanh toán khi kết nối được khôi phục. Nếu giao dịch đã thành công, hệ thống cập nhật đơn hàng thành PAID; nếu giao dịch thất bại hoặc chưa được thực hiện, hệ thống xử lý theo trạng thái thực tế và không ghi nhận thanh toán trùng.<br><br>**EX-06 (Lỗi xử lý kết quả thanh toán):** Tại bước 8 hoặc bước 9, hệ thống không thể xác minh chữ ký phản hồi, lưu kết quả giao dịch hoặc cập nhật trạng thái đơn hàng. Hệ thống không tự động xác nhận thanh toán thành công khi chưa xác minh được giao dịch; đồng thời lưu thông tin cần thiết để kiểm tra và xử lý lại, tránh ghi nhận thanh toán trùng. |
| **Alternative flows** | **AF-01 (`<<extend>>` Áp dụng mã giảm giá - tại bước 1, trước bước 4):** Khách hàng nhập mã giảm giá vào màn hình thanh toán. Hệ thống kiểm tra tính hợp lệ của mã. Nếu mã hợp lệ, hệ thống áp dụng mức giảm giá và cập nhật tổng tiền thanh toán tại bước 3. Nếu khách hàng không áp dụng mã giảm giá, luồng chính tiếp tục từ bước 2.<br><br>**AF-02 (Thanh toán thất bại và thử lại - tại bước 8):** Khi Cổng thanh toán trả về kết quả thất bại theo EX-02, hệ thống thông báo lý do giao dịch thất bại nếu có. Khách hàng có thể chọn lại phương thức thanh toán tại bước 2 và thực hiện lại từ bước 3. Đơn hàng chỉ được chuyển sang trạng thái PAID khi hệ thống xác minh giao dịch thành công.<br><br>**AF-03 (Khách hàng chủ động hủy trên Cổng thanh toán - tại bước 6 hoặc bước 7):** Khách hàng nhấn “Hủy” hoặc “Quay lại” trên giao diện Cổng thanh toán trước khi giao dịch hoàn tất. Cổng thanh toán kết thúc hoặc hủy phiên giao dịch nếu có thể. Hệ thống kiểm tra trạng thái giao dịch, giữ đơn hàng ở trạng thái chưa thanh toán nếu giao dịch chưa thành công và đưa khách hàng về màn hình thanh toán để lựa chọn phương thức khác hoặc thử lại.<br><br>**EX-04 (Thanh toán thành công nhưng chưa nhận được phản hồi - tại bước 8):** Cổng thanh toán đã xử lý giao dịch thành công nhưng hệ thống chưa nhận được Callback/Webhook do lỗi kết nối. Hệ thống kiểm tra lại trạng thái giao dịch với Cổng thanh toán. Sau khi xác minh thành công, hệ thống tiếp tục bước 9 của luồng chính và chỉ ghi nhận giao dịch một lần. |

## 4. Liên kết use-case với mã yêu cầu (REQUIREMENT TRACEABILITY MATRIX)
Bảng dưới đây liên kết trực tiếp từng Use-case con trong 2 sơ đồ với các Mã yêu cầu chức năng (Functional Requirements - FR) và phi chức năng (Non-Functional Requirements - NFR).

| **Mã yêu cầu** | **Tên yêu cầu** | **Mô tả chi tiết yêu cầu** | **Use-case liên kết** | **Sơ đồ** |
|---|---|---|---|---|
| FR-ORD-01 | Khởi tạo đơn hàng |Hệ thống cho phép khách hàng xác nhận đặt hàng từ các sản phẩm trong giỏ hàng. Trước khi tạo đơn, hệ thống kiểm tra tính hợp lệ của giỏ hàng và khả năng đáp ứng nguyên liệu. | Đặt đơn hàng | Đặt hàng |
| FR-ORD-02 | Thêm sản phẩm vào giỏ | Hệ thống cho phép khách hàng chọn sản phẩm từ thực đơn và thêm vào giỏ hàng trước khi xác nhận đặt hàng. | Thêm sản phẩm vào giỏ hàng (`<<include>>`) | Đặt hàng |
| FR-ORD-03 | Kiểm tra giỏ hàng | Hệ thống cho phép khách hàng xem danh sách sản phẩm, size, topping, số lượng, ghi chú và tổng tiền tạm tính trước khi xác nhận đặt hàng. | Kiểm tra giỏ hàng (`<<include>>`) | Đặt hàng |
| FR-ORD-04 | Tùy chọn Topping | Hệ thống cho phép khách hàng chọn một hoặc nhiều topping đi kèm sản phẩm và cập nhật giá món tương ứng. | Chọn topping (`<<extend>>`) | Đặt hàng |
| FR-ORD-05 | Tùy chọn kích cỡ (Size) | Hệ thống cho phép khách hàng chọn kích cỡ sản phẩm và cập nhật đơn giá theo size đã chọn. | Chọn size (`<<extend>>`) | Đặt hàng |
| FR-ORD-06 | Cập nhật số lượng món | Hệ thống cho phép khách hàng tăng, giảm số lượng sản phẩm khi tùy chỉnh món hoặc kiểm tra giỏ hàng; đồng thời cập nhật thành tiền và tổng tiền giỏ hàng. | Cập nhật số lượng (`<<extend>>`) | Đặt hàng |
| FR-ORD-07 | Ghi chú món ăn/đồ uống | Hệ thống cho phép khách hàng đính kèm ghi chú tùy chọn cho từng món để yêu cầu điều chỉnh cách chế biến. | Thêm ghi chú (note) (`<<extend>>`) | Đặt hàng |
| FR-PAY-01 | Xử lý thanh toán đơn hàng | Hệ thống cho phép khách hàng thanh toán đơn hàng đã được tạo và cập nhật trạng thái đơn hàng thành PAID sau khi giao dịch được xác minh thành công. | Thanh toán đơn hàng | Thanh toán |
| FR-PAY-02 | Chọn phương thức thanh toán | Hệ thống yêu cầu khách hàng chọn một phương thức thanh toán hợp lệ trước khi khởi tạo giao dịch. | Chọn phương thức thanh toán (`<<include>>`) | Thanh toán |
| FR-PAY-03 | Áp dụng mã khuyến mãi | Hệ thống cho phép khách hàng nhập mã giảm giá, kiểm tra điều kiện áp dụng và cập nhật tổng tiền thanh toán nếu mã hợp lệ. | Áp dụng mã giảm giá (`<<extend>>`) | Thanh toán |
| FR-PAY-04 | Tích hợp Cổng thanh toán | Hệ thống gửi yêu cầu thanh toán đến Cổng thanh toán và tiếp nhận, xác minh kết quả giao dịch để cập nhật trạng thái đơn hàng. | Thanh toán đơn hàng | Thanh toán |
| NFR-SEC-01 | Bảo mật & Toàn vẹn giao dịch | Hệ thống bảo vệ dữ liệu trao đổi với Cổng thanh toán bằng cơ chế bảo mật phù hợp, xác minh tính hợp lệ của phản hồi và ngăn việc ghi nhận trùng giao dịch khi xử lý yêu cầu lặp lại. | Thanh toán đơn hàng | Thanh toán |

