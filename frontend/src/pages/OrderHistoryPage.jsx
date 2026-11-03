import React, { useEffect, useState, useRef } from 'react';
import { orderApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { formatService } from '../services/formatService';
import LoadingSpinner from '../components/common/LoadingSpinner';

export const OrderHistoryPage = ({ onNavigate }) => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const pollingRef = useRef(null);

  const loadOrders = async (isBackground = false) => {
    try {
      if (!isBackground) setLoading(true);
      const customerId = user?.customerId || user?.id || 1;
      const res = await orderApi.getHistory({ customerId, page: 0, size: 20 });
      const list = res?.content || (Array.isArray(res) ? res : []);
      setOrders(list);
    } catch {
      if (!isBackground) setOrders([]);
    } finally {
      if (!isBackground) setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();

    // Tự động làm mới mỗi 5 giây để khách hàng nhận trạng thái mới ngay khi Barista xác nhận
    pollingRef.current = setInterval(() => {
      loadOrders(true);
    }, 5000);

    return () => {
      if (pollingRef.current) clearInterval(pollingRef.current);
    };
  }, [user]);

  const handleCancel = async (orderId) => {
    if (!window.confirm('Bạn có chắc muốn hủy đơn hàng này?')) return;
    try {
      await orderApi.cancel(orderId, 'Khách hàng yêu cầu hủy qua ứng dụng');
      loadOrders();
    } catch (error) {
      alert(error.message || 'Hủy đơn thất bại. Vui lòng liên hệ nhân viên.');
    }
  };

  // Xác định bước tiến trình của đơn
  const getStepProgress = (status) => {
    switch (status) {
      case 'PENDING':
        return 1;
      case 'PAID':
        return 2;
      case 'PREPARING':
        return 3;
      case 'READY':
        return 4;
      case 'COMPLETED':
        return 5;
      default:
        return 0;
    }
  };

  if (loading) {
    return <LoadingSpinner text="Đang tải lịch sử đơn hàng..." />;
  }

  return (
    <div className="container" style={{ padding: '36px 24px 80px', maxWidth: '850px', margin: '0 auto' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '24px',
          flexWrap: 'wrap',
          gap: '12px',
        }}
      >
        <div>
          <h1 style={{ fontSize: '1.8rem', fontWeight: 800 }}>Đơn Hàng Của Tôi</h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9rem' }}>
            Theo dõi trạng thái pha chế và nhận đồ uống theo thời gian thực (tự động cập nhật)
          </p>
        </div>

        <button className="btn btn-secondary btn-sm" onClick={() => loadOrders(false)}>
          Làm mới
        </button>
      </div>

      {orders.length === 0 ? (
        <div
          style={{
            textAlign: 'center',
            padding: '60px 20px',
            background: 'var(--color-surface)',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--color-border)',
          }}
        >
          <h3 style={{ fontSize: '1.25rem', marginBottom: '8px' }}>Chưa có đơn hàng nào</h3>
          <p style={{ color: 'var(--color-text-secondary)', marginBottom: '20px', fontSize: '0.92rem' }}>
            Hãy chọn một ly đồ uống mát lạnh từ thực đơn của BrewLite ngay hôm nay!
          </p>
          <button className="btn btn-primary" onClick={() => onNavigate('menu')}>
            Khám phá thực đơn
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {orders.map((order) => {
            const statusInfo = formatService.getOrderStatusInfo(order.status);
            const currentStep = getStepProgress(order.status);
            const isCancelled = order.status === 'CANCELLED' || order.status === 'PAYMENT_FAILED';

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
                {/* Header đơn hàng */}
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderBottom: '1px solid var(--color-surface-muted)',
                    paddingBottom: '14px',
                    marginBottom: '16px',
                    flexWrap: 'wrap',
                    gap: '10px',
                  }}
                >
                  <div>
                    <span style={{ fontWeight: 800, fontSize: '1.15rem', color: 'var(--color-accent)' }}>
                      {order.orderCode || `#BL${order.id}`}
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
                      style={{
                        background: statusInfo.bg,
                        color: statusInfo.color,
                        padding: '4px 10px',
                        borderRadius: '6px',
                        fontWeight: 700,
                        fontSize: '0.85rem',
                      }}
                    >
                      {statusInfo.label}
                    </span>

                    {order.status === 'PENDING' && (
                      <button
                        className="btn btn-secondary btn-sm"
                        style={{ color: '#ef4444', padding: '4px 8px' }}
                        onClick={() => handleCancel(order.id)}
                      >
                        Hủy đơn
                      </button>
                    )}
                  </div>
                </div>

                {/* Thanh tiến trình thời gian thực (Timeline) */}
                {!isCancelled && (
                  <div
                    style={{
                      background: 'var(--color-surface-muted)',
                      borderRadius: '8px',
                      padding: '14px 12px',
                      marginBottom: '16px',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', position: 'relative' }}>
                      {[
                        { step: 1, label: 'Đã nhận đơn' },
                        { step: 2, label: 'Đã thanh toán' },
                        { step: 3, label: 'Barista đang pha' },
                        { step: 4, label: 'Mời nhận món' },
                        { step: 5, label: 'Hoàn tất' },
                      ].map((s) => {
                        const isActive = currentStep >= s.step;
                        const isCurrent = currentStep === s.step;
                        return (
                          <div
                            key={s.step}
                            style={{
                              flex: 1,
                              textAlign: 'center',
                              position: 'relative',
                            }}
                          >
                            <div
                              style={{
                                width: '24px',
                                height: '24px',
                                borderRadius: '50%',
                                background: isActive ? 'var(--color-accent)' : '#cbd5e1',
                                color: '#ffffff',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '0.75rem',
                                fontWeight: 700,
                                margin: '0 auto 6px',
                                boxShadow: isCurrent ? '0 0 0 3px rgba(193, 120, 56, 0.25)' : 'none',
                              }}
                            >
                              {s.step}
                            </div>
                            <div
                              style={{
                                fontSize: '0.75rem',
                                fontWeight: isCurrent ? 700 : 500,
                                color: isActive ? 'var(--color-text-main)' : 'var(--color-text-muted)',
                              }}
                            >
                              {s.label}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Danh sách món */}
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
                          <div style={{ fontWeight: 600, fontSize: '0.92rem' }}>
                            {item.quantity}x {item.productName || item.name}
                          </div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', marginTop: '2px' }}>
                            Size {item.size || 'M'}
                            {item.toppings && item.toppings.length > 0 && (
                              <span> • Topping: {item.toppings.map((t) => t.toppingName || t.name || t).join(', ')}</span>
                            )}
                          </div>
                        </div>
                        <div style={{ fontWeight: 700, fontSize: '0.92rem' }}>
                          {formatService.formatMoney(item.lineTotal || item.unitPrice * item.quantity)}
                        </div>
                      </div>
                    ))}
                </div>

                {/* Footer đơn hàng */}
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
                        ? `Tại bàn (${order.tableNumber || 'Chưa rõ'})`
                        : 'Mang đi'}
                    </strong>
                  </span>
                  <div style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--color-accent)' }}>
                    Tổng cộng: {formatService.formatMoney(order.totalAmount || order.subtotal)}
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
