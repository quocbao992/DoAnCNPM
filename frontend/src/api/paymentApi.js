import axiosClient from './axiosClient';

export const paymentApi = {
  create(data) {
    return axiosClient.post('/api/payments', data);
  },
  updateStatus(paymentId, status, transactionRef = '') {
    return axiosClient.put(`/api/payments/${paymentId}/status`, null, {
      params: { status, transactionRef },
    });
  },
  getById(paymentId) {
    return axiosClient.get(`/api/payments/${paymentId}`);
  },
  getByOrder(orderId) {
    return axiosClient.get(`/api/payments/order/${orderId}`);
  },
};

export default paymentApi;
