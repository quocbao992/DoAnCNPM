import axios from 'axios';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    const status = error.response ? error.response.status : null;
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'Đã xảy ra lỗi kết nối tới máy chủ';

    if (status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }

    return Promise.reject({
      status,
      message,
      data: error.response?.data,
      originalError: error,
    });
  }
);

export default axiosClient;
