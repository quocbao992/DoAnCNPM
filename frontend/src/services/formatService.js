export const formatService = {
  formatMoney(amount) {
    const num = Number(amount) || 0;
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(num);
  },

  formatDateTime(dateStr) {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date);
    } catch {
      return dateStr;
    }
  },

  getOrderStatusInfo(status) {
    switch (status) {
      case 'PENDING':
        return { label: 'Chờ thanh toán', color: '#f59e0b', bg: '#fef3c7' };
      case 'CONFIRMED':
        return { label: 'Đã xác nhận', color: '#3b82f6', bg: '#dbeafe' };
      case 'PREPARING':
      case 'PROCESSING':
        return { label: 'Đang pha chế', color: '#8b5cf6', bg: '#ede9fe' };
      case 'READY':
        return { label: 'Sẵn sàng lấy', color: '#10b981', bg: '#d1fae5' };
      case 'COMPLETED':
      case 'PAID':
        return { label: 'Hoàn thành', color: '#059669', bg: '#ecfdf5' };
      case 'CANCELLED':
        return { label: 'Đã hủy', color: '#ef4444', bg: '#fee2e2' };
      default:
        return { label: status || 'Không rõ', color: '#6b7280', bg: '#f3f4f6' };
    }
  },
};

export default formatService;
