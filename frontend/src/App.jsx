import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';
import CustomerBottomDock from './components/common/CustomerBottomDock';
import AdminLeftSidebar from './components/common/AdminLeftSidebar';
import HomePage from './pages/HomePage';
import MenuPage from './pages/MenuPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminPortalPage from './pages/AdminPortalPage';
import ProfilePage from './pages/ProfilePage';
import { authService } from './services/authService';

const MainApp = () => {
  const [currentTab, setCurrentTab] = useState('home');
  const [adminSubTab, setAdminSubTab] = useState('dashboard');
  const [authNotice, setAuthNotice] = useState('');
  const { isAuthenticated, isAdmin } = useAuth();

  const handleNavigate = (tab) => {
    const isLoggedIn = isAuthenticated || authService.isAuthenticated();

    if (tab !== 'home' && tab !== 'menu' && tab !== 'login' && tab !== 'register' && !isLoggedIn) {
      setAuthNotice('Vui lòng đăng nhập để tiếp tục sử dụng tính năng này.');
      setCurrentTab('login');
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    if (tab === 'admin' && !isAdmin) {
      setAuthNotice('Bạn cần đăng nhập tài khoản Quản trị viên (Admin) để truy cập.');
      setCurrentTab('login');
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    setAuthNotice('');
    setCurrentTab(tab);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Nếu đang ở màn hình Quản trị Admin: Hiển thị thanh công cụ Admin bên trái từ trên xuống
  if (currentTab === 'admin' && isAdmin) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', background: '#f8fafc' }}>
        <AdminLeftSidebar
          adminTab={adminSubTab}
          onSelectTab={setAdminSubTab}
          onNavigateToCustomer={() => handleNavigate('home')}
        />
        <div style={{ marginLeft: '76px', flex: 1, minHeight: '100vh', overflowY: 'auto' }}>
          <AdminPortalPage
            activeTab={adminSubTab}
            onNavigate={handleNavigate}
          />
        </div>
      </div>
    );
  }

  const renderContent = () => {
    switch (currentTab) {
      case 'home':
        return <HomePage onNavigate={handleNavigate} />;
      case 'menu':
        return <MenuPage onNavigate={handleNavigate} />;
      case 'cart':
        return <CartPage onNavigate={handleNavigate} />;
      case 'checkout':
        return <CheckoutPage onNavigate={handleNavigate} />;
      case 'orders':
        return <OrderHistoryPage onNavigate={handleNavigate} />;
      case 'profile':
        return <ProfilePage onNavigate={handleNavigate} />;
      case 'admin':
        return (
          <AdminPortalPage
            activeTab={adminSubTab}
            onNavigate={handleNavigate}
          />
        );
      case 'login':
        return <LoginPage onNavigate={handleNavigate} initialNotice={authNotice} />;
      case 'register':
        return <RegisterPage onNavigate={handleNavigate} />;
      default:
        return <HomePage onNavigate={handleNavigate} />;
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar currentTab={currentTab} onNavigate={handleNavigate} />
      <main style={{ flex: 1 }} className="page-has-bottom-dock">
        {renderContent()}
      </main>
      <Footer />

      {/* Thanh công cụ ở dưới cho người dùng dễ thao tác (Hình vuông bo góc theo Hình 2) */}
      <CustomerBottomDock currentTab={currentTab} onNavigate={handleNavigate} />
    </div>
  );
};

export const App = () => {
  return (
    <AuthProvider>
      <CartProvider>
        <MainApp />
      </CartProvider>
    </AuthProvider>
  );
};

export default App;
