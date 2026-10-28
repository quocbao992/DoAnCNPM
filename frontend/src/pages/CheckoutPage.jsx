import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { orderApi } from '../api';
import { OrderType, PaymentMethod } from '../models/order.model';
import { formatService } from '../services/formatService';
import InteractiveButton from '../components/common/InteractiveButton';

export const CheckoutPage = ({ onNavigate }) => {
  const { user } = useAuth();
  const { cartItems, subtotal, clearCart } = useCart();

  const [orderType, setOrderType] = useState(OrderType.DINE_IN);
  const [tableNumber, setTableNumber] = useState('');
  const [voucherCode, setVoucherCode] = useState('');
  const [paymentMethod, setPaymentMethod] = useState(PaymentMethod.VNPAY);
  const [note, setNote] = useState('');

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [createdOrder, setCreatedOrder] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    try {
      const payload = {
        customerId: user?.id || 1,
        orderType,
        tableNumber: orderType === OrderType.DINE_IN ? tableNumber : '',
        voucherCode: voucherCode.trim() || undefined,
        loyaltyPointsUsed: 0,
        note: note.trim(),
        paymentMethod,
      };

      const res = await orderApi.checkout(payload);
      setCreatedOrder(res);
      clearCart();
    } catch (err) {
      setErrorMsg(
        err.message ||
          'Không thể kết nối đến máy chủ Spring Boot. Bạn có thể kiểm tra xem backend đã chạy tại port 8081 chưa.'
      );
    } finally {
      setLoading(false);
    }
  };

  const renderQrPreview = () => {
    const isMomo = paymentMethod === PaymentMethod.MOMO;
    const qrData = isMomo
      ? `2|99|0987654321|BREWLITE COFFEE|admin@brewlite.vn|0|0|${subtotal}|BREWLITE`
      : `00020101021238540010A0000007270124000697042201121028749281920208QRIBFTTA5303704540${subtotal}5802VN62180814BREWLITE6304`;

    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(
      qrData
    )}`;

    return (
      <div
        style={{
          marginTop: '16px',
          padding: '16px',
          borderRadius: 'var(--radius-md)',
          background: isMomo ? '#fdf2f8' : '#eff6ff',
          border: `1.5px solid ${isMomo ? '#f472b6' : '#93c5fd'}`,
          display: 'flex',
          gap: '16px',
          alignItems: 'center',
        }}
      >
        <img
          src={qrUrl}
          alt="QR Thanh toán"
          style={{
            width: '120px',
            height: '120px',
            borderRadius: '8px',
            background: '#ffffff',
            padding: '4px',
            boxShadow: 'var(--shadow-sm)',
          }}
        />
        <div style={{ fontSize: '0.88rem' }}>
          <div
            style={{
              fontWeight: 800,
              fontSize: '1rem',
              color: isMomo ? '#be185d' : '#1d4ed8',
              marginBottom: '4px',
            }}
          >
            {isMomo ? 'Quét mã Ví MoMo' : 'Quét mã QR Banking (VietQR)'}
          </div>
          <div>
            <strong>Đơn vị nhận:</strong> Cà Phê BrewLite
          </div>
          <div>
            <strong>{isMomo ? 'Số MoMo:' : 'Số TK:'}</strong>{' '}
            {isMomo ? '0987654321' : '102874928192 (VietinBank)'}
          </div>
          <div>
            <strong>Số tiền:</strong>{' '}
            <span style={{ color: 'var(--color-accent)', fontWeight: 700 }}>
              {formatService.formatMoney(subtotal)}
            </span>
          </div>
          <div style={{ color: 'var(--color-text-muted)', fontSize: '0.78rem', marginTop: '4px' }}>
            * Quét mã bằng App ngân hàng hoặc MoMo để thanh toán không tiền mặt
          </div>
        </div>
      </div>
    );
  };

  if (createdOrder) {
    const isMomo = paymentMethod === PaymentMethod.MOMO;
    return (
      <div className="container" style={{ padding: '60px 24px', textAlign: 'center' }}>
        <h1 style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--color-success)' }}>
          Đặt Hàng Thành Công!
        </h1>
        <p style={{ fontSize: '1.1rem', marginTop: '8px', color: 'var(--color-text-secondary)' }}>
          Mã đơn hàng của bạn:{' '}
          <strong style={{ color: 'var(--color-primary-dark)' }}>
            {createdOrder.orderCode || `#ORD-${Date.now().toString().slice(-6)}`}
          </strong>
        </p>

        <div
          style={{
            maxWidth: '520px',
            margin: '24px auto',
            background: 'var(--color-surface)',
            padding: '24px',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--color-border)',
            textAlign: 'left',
          }}
        >
          <div className="summary-row">
            <span>Trạng thái đơn:</span>
            <span style={{ fontWeight: 700, color: 'var(--color-warning)' }}>
              Đang chờ xác nhận / pha chế
            </span>
          </div>
          <div className="summary-row">
            <span>Hình thức thanh toán:</span>
            <span style={{ fontWeight: 700, color: isMomo ? '#be185d' : '#1d4ed8' }}>
              {isMomo ? 'Ví MoMo (Không tiền mặt)' : 'QR Banking (Không tiền mặt)'}
            </span>
          </div>
          <div className="summary-row total">
            <span>Tổng thanh toán:</span>
            <span>{formatService.formatMoney(createdOrder.totalAmount || subtotal)}</span>
          </div>

          <div
            style={{
              marginTop: '16px',
              padding: '14px',
              background: 'var(--color-surface-muted)',
              borderRadius: 'var(--radius-md)',
              textAlign: 'center',
            }}
          >
            <div style={{ fontWeight: 700, marginBottom: '6px' }}>
              Quét mã để hoàn tất chuyển khoản
            </div>
            <img
              src={`https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${encodeURIComponent(
                createdOrder.orderCode || 'BREWLITE'
              )}`}
              alt="QR Đơn hàng"
              style={{ width: '130px', height: '130px', margin: '0 auto', borderRadius: '8px' }}
            />
            <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', marginTop: '6px' }}>
              Nội dung CK: <strong>{createdOrder.orderCode || 'BREWLITE'}</strong>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center', gap: '14px' }}>
          <button className="btn btn-primary" onClick={() => onNavigate('orders')}>
            Xem lịch sử đơn hàng
          </button>
          <button className="btn btn-secondary" onClick={() => onNavigate('menu')}>
            Tiếp tục mua hàng
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '40px 24px 60px' }}>
      <h1 style={{ fontSize: '2.2rem', fontWeight: 800, marginBottom: '28px' }}>
        Thanh Toán & Xác Nhận Đơn Hàng
      </h1>

      {errorMsg && (
        <div
          style={{
            padding: '14px 18px',
            background: '#fee2e2',
            color: '#b91c1c',
            borderRadius: 'var(--radius-md)',
            marginBottom: '24px',
            fontWeight: 600,
          }}
        >
          {errorMsg}
        </div>
      )}

      <form onSubmit={handleSubmit} className="cart-page-grid">
        <div
          style={{
            background: 'var(--color-surface)',
            padding: '28px',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--color-border)',
          }}
        >
          <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '20px' }}>
            Thông tin nhận đồ uống
          </h3>

          <div className="form-group">
            <label className="form-label">Hình thức nhận món</label>
            <div style={{ display: 'flex', gap: '12px' }}>
              {[
                { type: OrderType.DINE_IN, label: 'Tại bàn' },
                { type: OrderType.TAKE_AWAY, label: 'Mang đi' },
                { type: OrderType.DELIVERY, label: 'Giao hàng' },
              ].map((opt) => (
                <button
                  type="button"
                  key={opt.type}
                  onClick={() => setOrderType(opt.type)}
                  style={{
                    flex: 1,
                    padding: '12px',
                    borderRadius: 'var(--radius-md)',
                    border: '1.5px solid',
                    borderColor:
                      orderType === opt.type
                        ? 'var(--color-accent)'
                        : 'var(--color-border)',
                    background:
                      orderType === opt.type
                        ? 'rgba(193, 120, 56, 0.08)'
                        : 'var(--color-surface)',
                    fontWeight: 700,
                    cursor: 'pointer',
                  }}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {orderType === OrderType.DINE_IN && (
            <div className="form-group">
              <label className="form-label">Số bàn (Nếu ngồi tại quán)</label>
              <input
                type="text"
                className="form-input"
                placeholder="VD: Bàn 04"
                value={tableNumber}
                onChange={(e) => setTableNumber(e.target.value)}
              />
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Phương thức thanh toán (Không nhận tiền mặt)</label>
            <div style={{ display: 'flex', gap: '12px', marginBottom: '12px' }}>
              <button
                type="button"
                onClick={() => setPaymentMethod(PaymentMethod.VNPAY)}
                style={{
                  flex: 1,
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  border: '1.5px solid',
                  borderColor:
                    paymentMethod === PaymentMethod.VNPAY
                      ? '#2563eb'
                      : 'var(--color-border)',
                  background:
                    paymentMethod === PaymentMethod.VNPAY
                      ? '#eff6ff'
                      : 'var(--color-surface)',
                  fontWeight: 700,
                  cursor: 'pointer',
                  textAlign: 'left',
                }}
              >
                🏦 Chuyển khoản QR Banking (VietQR)
              </button>

              <button
                type="button"
                onClick={() => setPaymentMethod(PaymentMethod.MOMO)}
                style={{
                  flex: 1,
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  border: '1.5px solid',
                  borderColor:
                    paymentMethod === PaymentMethod.MOMO
                      ? '#db2777'
                      : 'var(--color-border)',
                  background:
                    paymentMethod === PaymentMethod.MOMO
                      ? '#fdf2f8'
                      : 'var(--color-surface)',
                  fontWeight: 700,
                  cursor: 'pointer',
                  textAlign: 'left',
                }}
              >
                Ví điện tử MoMo (Quét QR MoMo)
              </button>
            </div>

            {renderQrPreview()}
          </div>

          <div className="form-group" style={{ marginTop: '20px' }}>
            <label className="form-label">Mã ưu đãi / Voucher</label>
            <input
              type="text"
              className="form-input"
              placeholder="VD: GIAM20K, CHAOMUNG"
              value={voucherCode}
              onChange={(e) => setVoucherCode(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Ghi chú cho quầy pha chế</label>
            <textarea
              className="form-input"
              rows={3}
              placeholder="VD: Ít ngọt, nhiều đá, để riêng đá..."
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          </div>
        </div>

        <div>
          <div className="order-summary-card">
            <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '20px' }}>
              Món trong đơn ({cartItems.length})
            </h3>

            <div style={{ maxHeight: '240px', overflowY: 'auto', marginBottom: '20px' }}>
              {cartItems.map((item) => (
                <div
                  key={item.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 0',
                    borderBottom: '1px solid var(--color-border)',
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '0.92rem' }}>
                      {item.name} x {item.quantity}
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--color-text-muted)' }}>
                      Size {item.size} {item.note ? `• ${item.note}` : ''}
                    </div>
                  </div>
                  <div style={{ fontWeight: 700, fontSize: '0.92rem' }}>
                    {formatService.formatMoney(item.itemTotalPrice || item.unitPrice * item.quantity)}
                  </div>
                </div>
              ))}
            </div>

            <div className="summary-row">
              <span style={{ color: 'var(--color-text-secondary)' }}>Tạm tính</span>
              <span style={{ fontWeight: 700 }}>{formatService.formatMoney(subtotal)}</span>
            </div>

            <div className="summary-row total">
              <span>Tổng thanh toán</span>
              <span>{formatService.formatMoney(subtotal)}</span>
            </div>

            <InteractiveButton
              text="Xác nhận đặt hàng & thanh toán"
              loadingText="Đang gửi đơn hàng..."
              successText="Đặt hàng thành công!"
              loading={loading}
              success={Boolean(createdOrder)}
              type="submit"
              style={{ marginTop: '24px', padding: '14px' }}
            />
          </div>
        </div>
      </form>
    </div>
  );
};

export default CheckoutPage;
