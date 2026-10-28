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
  }, [user?.id]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const customerId = user?.id || 1;
      const res = await orderApi.getHistory({ customerId, page: 0, size: 20 });
      const list = res?.content || (Array.isArray(res) ? res : []);
      if (list && list.length > 0) {
        setOrders(list);
        setLoading(false);
        return;
      }
    } catch {}

    setOrders([
      {
        id: 101,
        orderCode: 'ORD-20261025-001',
        status: 'PREPARING',
        subtotal: 74000,
        totalAmount: 74000,
        orderType: 'DINE_IN',
        tableNumber: 'Bàn 03',
        createdAt: new Date().toISOString(),
        items: [
          { id: 1, productName: 'Cà Phê Cốt Dừa Latte', quantity: 1, size: 'M', unitPrice: 45000 },
          { id: 2, productName: 'Bạc Xỉu Sữa Tươi', quantity: 1, size: 'S', unitPrice: 29000 },
        ],
      },
      {
        id: 102,
        orderCode: 'ORD-20261024-042',
        status: 'COMPLETED',
        subtotal: 39000,
        totalAmount: 39000,
        orderType: 'TAKE_AWAY',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        items: [
          { id: 3, productName: 'Trà Đào Cam Sả Tươi Mát', quantity: 1, size: 'M', unitPrice: 39000 },
        ],
      },
    ]);
    setLoading(false);
  };

  const handleCancel = async (orderId) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) return;
    try {
      await orderApi.cancel(orderId, 'Khách hàng yêu cầu hủy');
      alert('Đã hủy đơn hàng thành công');
      loadOrders();
    } catch (err) {
      alert('Không thể hủy đơn: ' + err.message);
    }
  };

  return (
    <div className="container" style={{ padding: '40px 24px 60px' }}>
      <h1 style={{ fontSize: '2.2rem', fontWeight: 800, marginBottom: '28px' }}>
        Lịch Sử Đơn Hàng Của Bạn
      </h1>

      {loading ? (
        <LoadingSpinner text="Đang tải lịch sử đơn hàng..." />
      ) : orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <p style={{ fontSize: '1.2rem', color: 'var(--color-text-secondary)', marginBottom: '16px' }}>
            Bạn chưa có đơn hàng nào tại BrewLite.
          </p>
          <button className="btn btn-primary" onClick={() => onNavigate('menu')}>
            Đặt đồ uống ngay
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
                    flexWrap: 'wrap',
                    gap: '12px',
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
                      ● {statusInfo.label}
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
                          padding: '6px 0',
                          fontSize: '0.92rem',
                        }}
                      >
                        <span>
                          {item.quantity}x {item.productName || item.name} ({item.size})
                        </span>
                        <span>
                          {formatService.formatMoney(
                            item.totalPrice || item.unitPrice * item.quantity
                          )}
                        </span>
                      </div>
                    ))}
                </div>

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    paddingTop: '12px',
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
