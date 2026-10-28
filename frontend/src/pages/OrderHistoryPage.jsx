import React, { useEffect, useState } from 'react';
import { orderApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { formatService } from '../services/formatService';
import LoadingSpinner from '../components/common/LoadingSpinner';

export const OrderHistoryPage = ({ onNavigate }) => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrders();
  }, [user]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const customerId = user?.customerId || user?.id || 1;
      const res = await orderApi.getHistory({ customerId, page: 0, size: 20 });
      const list = res?.content || (Array.isArray(res) ? res : []);
      setOrders(list);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (orderId) => {
    if (!window.confirm('Bạn có chắc muốn hủy đơn hàng này?')) return;
    try {
      await orderApi.cancel(orderId, 'Khách hàng yêu cầu hủy qua ứng dụng');
      loadOrders();
    } catch (error) {
      alert(error.message || 'Hủy đơn thất bại. Vui lòng liên hệ nhân viên.');
    }
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải lịch sử đơn hàng..." />;
  }

  return (
    <div className="container" style={{ padding: '40px 24px 80px' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '28px',
        }}
      >
        <div>
          <h1 style={{ fontSize: '2.2rem', fontWeight: 800 }}>Lịch Sử Đơn Hàng</h1>
          <p style={{ color: 'var(--color-text-secondary)' }}>
            Theo dõi tiến độ và các món bạn đã thưởng thức tại BrewLite
          </p>
        </div>

        <button className="btn btn-secondary btn-sm" onClick={loadOrders}>
          Làm mới
        </button>
      </div>

      {orders.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: '80px 20px',
            background: 'var(--color-surface)',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--color-border)',
          }}
        >
          <h3 style={{ fontSize: '1.3rem', marginBottom: '8px' }}>Chưa có đơn hàng nào</h3>
          <p style={{ color: 'var(--color-text-secondary)', marginBottom: '24px' }}>
            Hãy chọn một ly đồ uống mát lạnh từ thực đơn của chúng tôi ngay hôm nay!
          </p>
          <button className="btn btn-primary" onClick={() => onNavigate('menu')}>
            Khám phá thực đơn
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {orders.map((order) => {
            const statusInfo = formatService.getOrderStatusInfo(order.status);
            return (
              <div
                key={order.id}
                style={{
                  background: 'var(--color-surface)',
                  padding: '24px',
                  borderRadius: 'var(--radius-lg)',
                  border: '1px solid var(--color-border)',
                  boxShadow: 'var(--shadow-sm)',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderBottom: '1px solid var(--color-surface-muted)',
                    paddingBottom: '14px',
                    marginBottom: '16px',
                  }}
                >
                  <div>
                    <span style={{ fontWeight: 800, fontSize: '1.15rem' }}>
                      {order.orderCode || `#ORD-${order.id}`}
                    </span>
                    <span
                      style={{
                        marginLeft: '12px',
                        fontSize: '0.85rem',
                        color: 'var(--color-text-muted)',
                      }}
                    >
                      {formatService.formatDateTime(order.createdAt)}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span
                      className="status-badge"
                      style={{ background: statusInfo.bg, color: statusInfo.color }}
                    >
                      {statusInfo.label}
                    </span>

                    {order.status === 'PENDING' && (
                      <button
                        className="btn btn-secondary btn-sm"
                        style={{ color: 'var(--color-danger)' }}
                        onClick={() => handleCancel(order.id)}
                      >
                        Hủy đơn
                      </button>
                    )}
                  </div>
                </div>

                <div style={{ marginBottom: '14px' }}>
                  {order.items &&
                    order.items.map((item, idx) => (
                      <div
                        key={idx}
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          padding: '8px 0',
                          borderBottom:
                            idx < order.items.length - 1
                              ? '1px solid var(--color-surface-muted)'
                              : 'none',
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 600 }}>
                            {item.quantity}x {item.productName || item.name}
                          </div>
                          <div style={{ fontSize: '0.85rem', color: 'var(--color-text-secondary)' }}>
                            Size {item.size}
                            {item.ice ? ` - Đá: ${item.ice}` : ''}
                            {item.sugar ? ` - Đường: ${item.sugar}` : ''}
                            {item.toppings && item.toppings.length > 0
                              ? ` - Topping: ${item.toppings.join(', ')}`
                              : ''}
                          </div>
                        </div>
                        <div style={{ fontWeight: 700 }}>
                          {formatService.formatMoney(item.lineTotal || item.unitPrice * item.quantity)}
                        </div>
                      </div>
                    ))}
                </div>

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    paddingTop: '14px',
                    borderTop: '1px dashed var(--color-border)',
                  }}
                >
                  <span style={{ fontSize: '0.88rem', color: 'var(--color-text-secondary)' }}>
                    Hình thức:{' '}
                    <strong>
                      {order.orderType === 'DINE_IN'
                        ? `Tại bàn (${order.tableNumber || 'Bàn chưa số'})`
                        : order.orderType === 'TAKE_AWAY'
                        ? 'Mang đi'
                        : 'Giao hàng'}
                    </strong>
                  </span>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--color-accent)' }}>
                    Tổng: {formatService.formatMoney(order.totalAmount || order.subtotal)}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default OrderHistoryPage;
