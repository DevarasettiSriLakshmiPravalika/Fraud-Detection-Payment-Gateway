import api from './api';

const paymentService = {
  createPayment: (paymentData) => api.post('/payments', paymentData),
  getUserPayments: () => api.get('/payments'),
  getPaymentDetail: (transactionId) => api.get(`/payments/${transactionId}`),
  getMyAccount: () => api.get('/account/me'),
};

export default paymentService;
