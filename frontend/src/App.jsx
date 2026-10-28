import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';
import HomePage from './pages/HomePage';
import MenuPage from './pages/MenuPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminOrdersPage from './pages/AdminOrdersPage';

import ProfilePage from './pages/ProfilePage';
import { authService } from './services/authService';

const MainApp = () => {
  const [currentTab, setCurrentTab] = useState('home');
  const [authNotice, setAuthNotice] = useState('');
  const { isAuthenticated } = useAuth();

  const handleNavigate = (tab) => {
    const isLoggedIn = isAuthenticated || authService.isAuthenticated();

    if (tab !== 'home' && tab !== 'menu' && tab !== 'login' && tab !== 'register' && !isLoggedIn) {
      setAuthNotice('Vui lòng đăng nhập để tiếp tục sử dụng tính năng này.');
      setCurrentTab('login');
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    setAuthNotice('');
    setCurrentTab(tab);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

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
        return <AdminOrdersPage onNavigate={handleNavigate} />;
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
      <main style={{ flex: 1 }}>{renderContent()}</main>
      <Footer />
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
