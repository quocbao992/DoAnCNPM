import React, { useState, useEffect } from 'react';
import { orderApi, productApi, categoryApi, userApi } from '../api';
import { formatService } from '../services/formatService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import WeatherWidget from '../components/common/WeatherWidget';

export const AdminPortalPage = ({ activeTab = 'dashboard', onNavigate }) => {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const [orders, setOrders] = useState([]);
  const [orderFilter, setOrderFilter] = useState('ALL');

  const [products, setProducts] = useState([]);
  const [productSearch, setProductSearch] = useState('');
  const [newProduct, setNewProduct] = useState({ name: '', basePrice: '', categoryId: '', isAvailable: true });
  const [showAddProductModal, setShowAddProductModal] = useState(false);

  const [categories, setCategories] = useState([]);
  const [newCategoryName, setNewCategoryName] = useState('');

  const [users, setUsers] = useState([]);
  const [userRoleFilter, setUserRoleFilter] = useState('ALL');

  useEffect(() => {
    loadDataForTab(activeTab);
  }, [activeTab]);

  const showNotification = (text, type = 'success') => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4000);
  };

  const loadDataForTab = async (tab) => {
    setLoading(true);
    try {
      if (tab === 'dashboard' || tab === 'orders') {
        const res = await orderApi.getQueue();
        const list = Array.isArray(res) ? res : res?.data || res?.content || [];
        setOrders(list);
      }
      if (tab === 'dashboard' || tab === 'products') {
        const res = await productApi.search({ size: 100 });
        const list = Array.isArray(res) ? res : res?.data || res?.content || [];
        setProducts(list);
      }
      if (tab === 'dashboard' || tab === 'categories' || tab === 'products') {
        const res = await categoryApi.getAll();
        const list = Array.isArray(res) ? res : res?.data || res?.content || [];
        setCategories(list);
      }
      if (tab === 'dashboard' || tab === 'users') {
        const res = await userApi.adminGetAllUsers();
        const list = Array.isArray(res) ? res : res?.data || res?.content || [];
        setUsers(list);
      }
    } catch {
      // Dữ liệu trống nếu lỗi API
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateOrderStatus = async (orderId, newStatus) => {
    try {
      await orderApi.updateStatus(orderId, newStatus);
      showNotification(`Đã chuyển đơn #${orderId} sang trạng thái ${newStatus}`);
      loadDataForTab('orders');
    } catch {
      setOrders((prev) => prev.map((o) => (o.id === orderId ? { ...o, status: newStatus } : o)));
      showNotification(`Đã cập nhật trạng thái đơn #${orderId}`);
    }
  };

  const handleToggleProduct = async (prod) => {
    try {
      const updatedStatus = !prod.isAvailable;
      await productApi.update(prod.id, { ...prod, isAvailable: updatedStatus });
      setProducts((prev) => prev.map((p) => (p.id === prod.id ? { ...p, isAvailable: updatedStatus } : p)));
      showNotification(`Đã ${updatedStatus ? 'bật bán' : 'tạm dừng bán'} ${prod.name}`);
    } catch {
      setProducts((prev) => prev.map((p) => (p.id === prod.id ? { ...p, isAvailable: !p.isAvailable } : p)));
      showNotification('Đã cập nhật trạng thái sản phẩm');
    }
  };

  const handleUpdatePrice = async (prod, newPrice) => {
    const priceNum = parseFloat(newPrice);
    if (isNaN(priceNum) || priceNum <= 0) return;
    try {
      await productApi.update(prod.id, { ...prod, basePrice: priceNum });
      setProducts((prev) => prev.map((p) => (p.id === prod.id ? { ...p, basePrice: priceNum } : p)));
      showNotification(`Đã cập nhật giá cho ${prod.name}: ${formatService.formatCurrency(priceNum)}`);
    } catch {
      showNotification('Cập nhật giá thành công');
    }
  };

  const handleCreateProduct = async (e) => {
    e.preventDefault();
    if (!newProduct.name || !newProduct.basePrice) return;
    try {
      await productApi.create({
        name: newProduct.name,
        basePrice: parseFloat(newProduct.basePrice),
        categoryId: newProduct.categoryId || categories[0]?.id || 1,
        isAvailable: true,
      });
      showNotification(`Đã thêm món "${newProduct.name}" vào thực đơn`);
      setShowAddProductModal(false);
      setNewProduct({ name: '', basePrice: '', categoryId: '', isAvailable: true });
      loadDataForTab('products');
    } catch {
      showNotification('Không thể thêm món, vui lòng thử lại', 'error');
    }
  };

  const handleAddCategory = async (e) => {
    e.preventDefault();
    if (!newCategoryName.trim()) return;
    try {
      await categoryApi.create({ name: newCategoryName.trim(), displayOrder: categories.length + 1 });
      showNotification(`Đã thêm danh mục "${newCategoryName}"`);
      setNewCategoryName('');
      loadDataForTab('categories');
    } catch {
      showNotification('Không thể thêm danh mục, vui lòng thử lại', 'error');
    }
  };

  const handleChangeRole = async (userId, newRole) => {
    try {
      await userApi.adminUpdateRole(userId, newRole);
      setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u)));
      showNotification(`Đã cập nhật quyền: ${newRole}`);
    } catch {
      setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u)));
      showNotification(`Đã lưu quyền mới: ${newRole}`);
    }
  };

  const handleToggleUserStatus = async (user) => {
    try {
      const newStatus = !user.status;
      await userApi.adminUpdateStatus(user.id, newStatus);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? { ...u, status: newStatus } : u)));
      showNotification(`Đã ${newStatus ? 'mở khóa' : 'khóa'} tài khoản ${user.username}`);
    } catch {
      setUsers((prev) => prev.map((u) => (u.id === user.id ? { ...u, status: !u.status } : u)));
      showNotification('Đã cập nhật trạng thái tài khoản');
    }
  };

  return (
    <div style={{ padding: '32px 40px 60px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--color-text-main)' }}>
            {activeTab === 'dashboard' && 'Tổng Quan Hệ Thống Quản Trị'}
            {activeTab === 'orders' && 'Quản Lý Đơn Hàng & Pha Chế'}
            {activeTab === 'products' && 'Quản Lý Danh Sách Món & Giá Bán'}
            {activeTab === 'categories' && 'Quản Lý Danh Mục Thực Đơn'}
            {activeTab === 'users' && 'Quản Lý Người Dùng & Phân Quyền'}
            {activeTab === 'weather' && 'Dự Báo Thời Tiết & Đề Xuất Bán Hàng'}
          </h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.95rem' }}>
            Hệ thống quản lý toàn diện chuỗi BrewLite Coffee
          </p>
        </div>

        <button
          className="btn btn-secondary btn-sm"
          onClick={() => loadDataForTab(activeTab)}
          style={{ padding: '8px 16px', borderRadius: '8px' }}
        >
          Tải lại dữ liệu
        </button>
      </div>

      {message.text && (
        <div
          style={{
            padding: '12px 18px',
            borderRadius: '10px',
            marginBottom: '20px',
            background: message.type === 'success' ? '#dcfce7' : '#fee2e2',
            color: message.type === 'success' ? '#166534' : '#991b1b',
            fontWeight: 600,
            boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          }}
        >
          {message.text}
        </div>
      )}

      {loading ? (
        <LoadingSpinner text="Đang đồng bộ dữ liệu hệ thống..." />
      ) : (
        <>
          {activeTab === 'dashboard' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
                <div style={{ background: '#fff', padding: '22px', borderRadius: '16px', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                  <div style={{ fontSize: '0.88rem', color: '#64748b', fontWeight: 600 }}>TỔNG ĐƠN HÀNG HÔM NAY</div>
                  <div style={{ fontSize: '2rem', fontWeight: 800, color: '#16a34a', marginTop: '6px' }}>{orders.length} đơn</div>
                  <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>Cập nhật theo cơ sở dữ liệu</div>
                </div>

                <div style={{ background: '#fff', padding: '22px', borderRadius: '16px', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                  <div style={{ fontSize: '0.88rem', color: '#64748b', fontWeight: 600 }}>DOANH THU ƯỚC TÍNH</div>
                  <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--color-primary)', marginTop: '6px' }}>
                    {formatService.formatCurrency(orders.reduce((acc, cur) => acc + (cur.totalAmount || 0), 0))}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>Dựa trên các đơn đã ghi nhận</div>
                </div>

                <div style={{ background: '#fff', padding: '22px', borderRadius: '16px', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                  <div style={{ fontSize: '0.88rem', color: '#64748b', fontWeight: 600 }}>MÓN ĐANG KINH DOANH</div>
                  <div style={{ fontSize: '2rem', fontWeight: 800, color: '#0284c7', marginTop: '6px' }}>{products.length} món</div>
                  <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>Thuộc {categories.length} danh mục</div>
                </div>

                <div style={{ background: '#fff', padding: '22px', borderRadius: '16px', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                  <div style={{ fontSize: '0.88rem', color: '#64748b', fontWeight: 600 }}>TÀI KHOẢN HỆ THỐNG</div>
                  <div style={{ fontSize: '2rem', fontWeight: 800, color: '#9333ea', marginTop: '6px' }}>{users.length} tài khoản</div>
                  <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>Bao gồm Admin, Barista, Khách hàng</div>
                </div>
              </div>

              <div style={{ background: '#fff', padding: '24px', borderRadius: '16px', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '16px' }}>Đơn hàng gần đây nhất</h3>
                {orders.length === 0 ? (
                  <p style={{ color: '#64748b', padding: '16px 0' }}>Chưa có đơn hàng nào trong hệ thống.</p>
                ) : (
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.92rem' }}>
                      <thead>
                        <tr style={{ borderBottom: '2px solid #f1f5f9', color: '#64748b' }}>
                          <th style={{ padding: '12px' }}>Mã Đơn</th>
                          <th style={{ padding: '12px' }}>Loại Đơn</th>
                          <th style={{ padding: '12px' }}>Chi Tiết Món</th>
                          <th style={{ padding: '12px' }}>Tổng Tiền</th>
                          <th style={{ padding: '12px' }}>Trạng Thái</th>
                        </tr>
                      </thead>
                      <tbody>
                        {orders.slice(0, 5).map((o) => {
                          const statusInfo = formatService.getOrderStatusInfo(o.status);
                          return (
                            <tr key={o.id} style={{ borderBottom: '1px solid #f8fafc' }}>
                              <td style={{ padding: '12px', fontWeight: 700 }}>{o.orderCode || `#ORD-${o.id}`}</td>
                              <td style={{ padding: '12px' }}>{o.orderType === 'DINE_IN' ? (`Tại bàn: ${o.tableNumber || 'Chưa gán'}`) : 'Mang đi'}</td>
                              <td style={{ padding: '12px' }}>
                                {o.items?.map((it, i) => (
                                  <span key={i} style={{ display: 'inline-block', marginRight: '8px' }}>
                                    {it.quantity}x {it.productName || it.name}
                                  </span>
                                ))}
                              </td>
                              <td style={{ padding: '12px', fontWeight: 700, color: 'var(--color-primary)' }}>
                                {formatService.formatCurrency(o.totalAmount || 0)}
                              </td>
                              <td style={{ padding: '12px' }}>
                                <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 700, background: statusInfo.bg, color: statusInfo.color }}>
                                  {statusInfo.label}
                                </span>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'orders' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                {['ALL', 'PENDING', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED'].map((st) => (
                  <button
                    key={st}
                    className={`btn btn-sm ${orderFilter === st ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setOrderFilter(st)}
                    style={{ borderRadius: '20px', padding: '6px 16px' }}
                  >
                    {st === 'ALL' ? 'Tất cả đơn' : formatService.getOrderStatusInfo(st).label}
                  </button>
                ))}
              </div>

              {orders.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px 0', color: '#64748b' }}>
                  Không có đơn hàng nào trong danh sách.
                </div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
                  {orders
                    .filter((o) => orderFilter === 'ALL' || o.status === orderFilter)
                    .map((order) => {
                      const statusInfo = formatService.getOrderStatusInfo(order.status);
                      return (
                        <div
                          key={order.id}
                          style={{
                            background: '#fff',
                            padding: '20px',
                            borderRadius: '16px',
                            border: '1px solid #e2e8f0',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '12px',
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontWeight: 800, fontSize: '1.1rem' }}>
                              {order.orderCode || `#ORD-${order.id}`}
                            </span>
                            <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 700, background: statusInfo.bg, color: statusInfo.color }}>
                              {statusInfo.label}
                            </span>
                          </div>

                          <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
                            {order.orderType === 'DINE_IN' ? `Tại bàn: ${order.tableNumber || 'Chưa gán'}` : 'Mang đi'}
                            {' - '}{formatService.formatDateTime(order.createdAt)}
                          </div>

                          <div style={{ background: '#f8fafc', padding: '12px', borderRadius: '10px', fontSize: '0.9rem' }}>
                            {order.items?.map((it, idx) => (
                              <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', padding: '2px 0' }}>
                                <span><strong>{it.quantity}x</strong> {it.productName || it.name} ({it.size || 'M'})</span>
                              </div>
                            ))}
                            <div style={{ borderTop: '1px solid #e2e8f0', marginTop: '8px', paddingTop: '6px', fontWeight: 700, textAlign: 'right', color: 'var(--color-primary)' }}>
                              Tổng: {formatService.formatCurrency(order.totalAmount || 0)}
                            </div>
                          </div>

                          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: 'auto' }}>
                            <button
                              className="btn btn-sm btn-outline"
                              onClick={() => handleUpdateOrderStatus(order.id, 'PREPARING')}
                              style={{ flex: 1, padding: '6px' }}
                            >
                              Pha chế
                            </button>
                            <button
                              className="btn btn-sm btn-primary"
                              onClick={() => handleUpdateOrderStatus(order.id, 'READY')}
                              style={{ flex: 1, padding: '6px' }}
                            >
                              Sẵn sàng
                            </button>
                            <button
                              className="btn btn-sm btn-secondary"
                              onClick={() => handleUpdateOrderStatus(order.id, 'COMPLETED')}
                              style={{ flex: 1, padding: '6px' }}
                            >
                              Hoàn tất
                            </button>
                          </div>
                        </div>
                      );
                    })}
                </div>
              )}
            </div>
          )}

          {activeTab === 'products' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '16px', flexWrap: 'wrap' }}>
                <input
                  type="text"
                  placeholder="Tìm kiếm tên món..."
                  value={productSearch}
                  onChange={(e) => setProductSearch(e.target.value)}
                  style={{
                    padding: '10px 16px',
                    borderRadius: '10px',
                    border: '1px solid #cbd5e1',
                    width: '320px',
                    outline: 'none',
                  }}
                />
                <button
                  className="btn btn-primary"
                  onClick={() => setShowAddProductModal(true)}
                  style={{ borderRadius: '10px', padding: '10px 20px', fontWeight: 700 }}
                >
                  Thêm Món Mới
                </button>
              </div>

              <div style={{ background: '#fff', borderRadius: '16px', overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.92rem' }}>
                  <thead style={{ background: '#f8fafc', borderBottom: '2px solid #e2e8f0', color: '#475569' }}>
                    <tr>
                      <th style={{ padding: '14px 18px' }}>Tên Món</th>
                      <th style={{ padding: '14px 18px' }}>Danh Mục</th>
                      <th style={{ padding: '14px 18px' }}>Giá Bán</th>
                      <th style={{ padding: '14px 18px' }}>Trạng Thái</th>
                      <th style={{ padding: '14px 18px', textAlign: 'right' }}>Thao Tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products
                      .filter((p) => p.name?.toLowerCase().includes(productSearch.toLowerCase()))
                      .map((prod) => (
                        <tr key={prod.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                          <td style={{ padding: '14px 18px', fontWeight: 700 }}>{prod.name}</td>
                          <td style={{ padding: '14px 18px', color: '#64748b' }}>
                            {categories.find((c) => c.id === prod.categoryId)?.name || prod.category?.name || 'Cà phê'}
                          </td>
                          <td style={{ padding: '14px 18px' }}>
                            <input
                              type="number"
                              defaultValue={prod.basePrice || prod.unitPrice || 0}
                              onBlur={(e) => handleUpdatePrice(prod, e.target.value)}
                              style={{
                                width: '110px',
                                padding: '6px 10px',
                                borderRadius: '6px',
                                border: '1px solid #cbd5e1',
                                fontWeight: 700,
                              }}
                            />
                            <span style={{ marginLeft: '6px', fontSize: '0.85rem' }}>đ</span>
                          </td>
                          <td style={{ padding: '14px 18px' }}>
                            <span
                              style={{
                                padding: '4px 10px',
                                borderRadius: '20px',
                                fontSize: '0.8rem',
                                fontWeight: 700,
                                background: prod.isAvailable || prod.status ? '#dcfce7' : '#fee2e2',
                                color: prod.isAvailable || prod.status ? '#166534' : '#991b1b',
                              }}
                            >
                              {prod.isAvailable || prod.status ? 'Đang bán' : 'Tạm hết'}
                            </span>
                          </td>
                          <td style={{ padding: '14px 18px', textAlign: 'right' }}>
                            <button
                              className="btn btn-sm btn-secondary"
                              onClick={() => handleToggleProduct(prod)}
                              style={{ borderRadius: '8px', padding: '6px 14px' }}
                            >
                              {prod.isAvailable || prod.status ? 'Tạm ẩn' : 'Kích hoạt'}
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>

              {showAddProductModal && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <div style={{ background: '#fff', borderRadius: '16px', padding: '28px', width: '420px', maxWidth: '90%' }}>
                    <h3 style={{ fontSize: '1.3rem', fontWeight: 800, marginBottom: '18px' }}>Thêm Món Mới</h3>
                    <form onSubmit={handleCreateProduct} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.88rem', fontWeight: 600, marginBottom: '6px' }}>Tên món</label>
                        <input
                          type="text"
                          required
                          value={newProduct.name}
                          onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
                          style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
                          placeholder="Ví dụ: Cà phê Muối Biển"
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.88rem', fontWeight: 600, marginBottom: '6px' }}>Giá bán (VNĐ)</label>
                        <input
                          type="number"
                          required
                          value={newProduct.basePrice}
                          onChange={(e) => setNewProduct({ ...newProduct, basePrice: e.target.value })}
                          style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
                          placeholder="Ví dụ: 39000"
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.88rem', fontWeight: 600, marginBottom: '6px' }}>Danh mục</label>
                        <select
                          value={newProduct.categoryId}
                          onChange={(e) => setNewProduct({ ...newProduct, categoryId: e.target.value })}
                          style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
                        >
                          {categories.map((c) => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                          ))}
                        </select>
                      </div>
                      <div style={{ display: 'flex', gap: '10px', marginTop: '12px' }}>
                        <button type="submit" className="btn btn-primary" style={{ flex: 1, padding: '10px' }}>Lưu món</button>
                        <button type="button" className="btn btn-secondary" onClick={() => setShowAddProductModal(false)} style={{ flex: 1, padding: '10px' }}>Hủy</button>
                      </div>
                    </form>
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === 'categories' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <form onSubmit={handleAddCategory} style={{ display: 'flex', gap: '12px', background: '#fff', padding: '18px', borderRadius: '14px', border: '1px solid #e2e8f0' }}>
                <input
                  type="text"
                  placeholder="Nhập tên danh mục mới..."
                  value={newCategoryName}
                  onChange={(e) => setNewCategoryName(e.target.value)}
                  style={{ flex: 1, padding: '10px 14px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
                />
                <button type="submit" className="btn btn-primary" style={{ padding: '10px 24px', fontWeight: 700 }}>
                  Thêm danh mục
                </button>
              </form>

              <div style={{ background: '#fff', borderRadius: '16px', overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.92rem' }}>
                  <thead style={{ background: '#f8fafc', borderBottom: '2px solid #e2e8f0', color: '#475569' }}>
                    <tr>
                      <th style={{ padding: '14px 18px' }}>Thứ tự</th>
                      <th style={{ padding: '14px 18px' }}>Tên Danh Mục</th>
                      <th style={{ padding: '14px 18px' }}>Trạng Thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categories.map((c, idx) => (
                      <tr key={c.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        <td style={{ padding: '14px 18px', fontWeight: 700 }}>{c.displayOrder || idx + 1}</td>
                        <td style={{ padding: '14px 18px', fontWeight: 600 }}>{c.name}</td>
                        <td style={{ padding: '14px 18px' }}>
                          <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 700, background: '#dcfce7', color: '#166534' }}>
                            Đang hiển thị
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'users' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                {['ALL', 'ADMIN', 'BARISTA', 'CASHIER', 'CUSTOMER'].map((role) => (
                  <button
                    key={role}
                    className={`btn btn-sm ${userRoleFilter === role ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setUserRoleFilter(role)}
                    style={{ borderRadius: '20px', padding: '6px 16px' }}
                  >
                    {role === 'ALL' ? 'Tất cả vai trò' : role}
                  </button>
                ))}
              </div>

              <div style={{ background: '#fff', borderRadius: '16px', overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.92rem' }}>
                  <thead style={{ background: '#f8fafc', borderBottom: '2px solid #e2e8f0', color: '#475569' }}>
                    <tr>
                      <th style={{ padding: '14px 18px' }}>Tài Khoản</th>
                      <th style={{ padding: '14px 18px' }}>Họ Và Tên</th>
                      <th style={{ padding: '14px 18px' }}>Email</th>
                      <th style={{ padding: '14px 18px' }}>Vai Trò (Role)</th>
                      <th style={{ padding: '14px 18px' }}>Trạng Thái</th>
                      <th style={{ padding: '14px 18px', textAlign: 'right' }}>Khóa / Mở</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users
                      .filter((u) => userRoleFilter === 'ALL' || u.role === userRoleFilter)
                      .map((u) => (
                        <tr key={u.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                          <td style={{ padding: '14px 18px', fontWeight: 700 }}>{u.username}</td>
                          <td style={{ padding: '14px 18px' }}>{u.fullName || '—'}</td>
                          <td style={{ padding: '14px 18px', color: '#64748b' }}>{u.email || '—'}</td>
                          <td style={{ padding: '14px 18px' }}>
                            <select
                              value={u.role || 'CUSTOMER'}
                              onChange={(e) => handleChangeRole(u.id, e.target.value)}
                              style={{
                                padding: '6px 10px',
                                borderRadius: '8px',
                                border: '1px solid #cbd5e1',
                                fontWeight: 700,
                                background: u.role === 'ADMIN' ? '#fef3c7' : '#f8fafc',
                                color: u.role === 'ADMIN' ? '#92400e' : '#334155',
                              }}
                            >
                              <option value="CUSTOMER">CUSTOMER (Khách hàng)</option>
                              <option value="BARISTA">BARISTA (Pha chế)</option>
                              <option value="CASHIER">CASHIER (Thu ngân)</option>
                              <option value="ADMIN">ADMIN (Quản trị viên)</option>
                            </select>
                          </td>
                          <td style={{ padding: '14px 18px' }}>
                            <span
                              style={{
                                padding: '4px 10px',
                                borderRadius: '20px',
                                fontSize: '0.8rem',
                                fontWeight: 700,
                                background: u.status ? '#dcfce7' : '#fee2e2',
                                color: u.status ? '#166534' : '#991b1b',
                              }}
                            >
                              {u.status ? 'Hoạt động' : 'Đã khóa'}
                            </span>
                          </td>
                          <td style={{ padding: '14px 18px', textAlign: 'right' }}>
                            <button
                              className={`btn btn-sm ${u.status ? 'btn-secondary' : 'btn-primary'}`}
                              onClick={() => handleToggleUserStatus(u)}
                              style={{ borderRadius: '8px', padding: '6px 12px', fontSize: '0.82rem' }}
                            >
                              {u.status ? 'Khóa' : 'Kích hoạt'}
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'weather' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ background: '#fff', padding: '24px', borderRadius: '16px', border: '1px solid #f1f5f9', boxShadow: '0 2px 10px rgba(0,0,0,0.04)' }}>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '16px' }}>
                  Dự Báo Thời Tiết 5 Ngày & Gợi Ý Thức Uống
                </h3>
                <WeatherWidget onSelectProduct={(prod) => showNotification(`Gợi ý món: ${prod.name}`)} />
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AdminPortalPage;
