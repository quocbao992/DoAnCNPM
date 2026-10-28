import React from 'react';
import {
  IconDashboard,
  IconOrders,
  IconCoffee,
  IconCategory,
  IconUsers,
  IconWeather,
  IconStore,
  IconLogout,
} from './Icons';
import { useAuth } from '../../context/AuthContext';

/**
 * Thanh công cụ của Admin hiện sang bên trái từ trên xuống (Left Sidebar)
 * Thiết kế các nút vuông bo góc, tooltip màu đen trỏ sang trái chuẩn Hình 2
 * Đầy đủ tính năng của toàn hệ thống
 */
export const AdminLeftSidebar = ({ adminTab, onSelectTab, onNavigateToCustomer }) => {
  const { logout, user } = useAuth();

  return (
    <aside className="admin-left-sidebar" aria-label="Thanh công cụ quản trị hệ thống">
      {/* Logo đỉnh thanh bên */}
      <div
        className="admin-sidebar-logo"
        onClick={() => onSelectTab('dashboard')}
        title="BrewLite Admin"
      >
        BL
      </div>

      {/* Danh sách các tính năng từ trên xuống */}
      <nav className="admin-sidebar-nav">
        {/* 1. Tổng quan / Dashboard */}
        <button
          type="button"
          className={`tool-square-btn ${adminTab === 'dashboard' ? 'active' : ''}`}
          onClick={() => onSelectTab('dashboard')}
          aria-label="Tổng quan hệ thống"
        >
          <IconDashboard size={22} />
          <span className="dock-tooltip dock-tooltip-right">Tổng quan (Dashboard)</span>
        </button>

        {/* 2. Quản lý Đơn hàng */}
        <button
          type="button"
          className={`tool-square-btn ${adminTab === 'orders' ? 'active' : ''}`}
          onClick={() => onSelectTab('orders')}
          aria-label="Quản lý đơn hàng"
        >
          <IconOrders size={22} />
          <span className="dock-tooltip dock-tooltip-right">Quản lý Đơn hàng</span>
        </button>

        {/* 3. Quản lý Sản phẩm */}
        <button
          type="button"
          className={`tool-square-btn ${adminTab === 'products' ? 'active' : ''}`}
          onClick={() => onSelectTab('products')}
          aria-label="Quản lý sản phẩm"
        >
          <IconCoffee size={22} />
          <span className="dock-tooltip dock-tooltip-right">Quản lý Sản phẩm</span>
        </button>

        {/* 4. Quản lý Danh mục */}
        <button
          type="button"
          className={`tool-square-btn ${adminTab === 'categories' ? 'active' : ''}`}
          onClick={() => onSelectTab('categories')}
          aria-label="Quản lý danh mục"
        >
          <IconCategory size={22} />
          <span className="dock-tooltip dock-tooltip-right">Quản lý Danh mục</span>
        </button>

        {/* 5. Quản lý Người dùng & Phân quyền */}
        <button
          type="button"
          className={`tool-square-btn ${adminTab === 'users' ? 'active' : ''}`}
          onClick={() => onSelectTab('users')}
          aria-label="Quản lý người dùng & phân quyền"
        >
          <IconUsers size={22} />
          <span className="dock-tooltip dock-tooltip-right">Người dùng & Phân quyền</span>
        </button>

        {/* 6. Dự báo Thời tiết & Đề xuất */}
        <button
          type="button"
          className={`tool-square-btn ${adminTab === 'weather' ? 'active' : ''}`}
          onClick={() => onSelectTab('weather')}
          aria-label="Thời tiết & Gợi ý"
        >
          <IconWeather size={22} />
          <span className="dock-tooltip dock-tooltip-right">Dự báo thời tiết & Đề xuất</span>
        </button>
      </nav>

      {/* Nhóm nút dưới cùng */}
      <div className="admin-sidebar-bottom">
        {/* 7. Xem giao diện khách hàng */}
        <button
          type="button"
          className="tool-square-btn"
          onClick={onNavigateToCustomer}
          aria-label="Xem trang khách hàng"
        >
          <IconStore size={22} />
          <span className="dock-tooltip dock-tooltip-right">Xem trang Khách hàng</span>
        </button>

        {/* 8. Đăng xuất */}
        <button
          type="button"
          className="tool-square-btn"
          onClick={() => {
            logout();
            onNavigateToCustomer();
          }}
          aria-label="Đăng xuất"
          style={{ color: '#ef4444' }}
        >
          <IconLogout size={22} />
          <span className="dock-tooltip dock-tooltip-right">
            Đăng xuất ({user?.username || 'Admin'})
          </span>
        </button>
      </div>
    </aside>
  );
};

export default AdminLeftSidebar;

