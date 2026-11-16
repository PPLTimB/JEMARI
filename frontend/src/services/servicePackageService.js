import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/service-packages';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export default {
  getAllActivePackages() {
    return axios.get(API_BASE_URL);
  },

  getMyPackages() {
    return axios.get(`${API_BASE_URL}/me`, {
      headers: getAuthHeaders()
    });
  },

  createPackage(payload) {
    return axios.post(API_BASE_URL, payload, {
      headers: getAuthHeaders()
    });
  },

  updatePackage(id, payload) {
    return axios.put(`${API_BASE_URL}/${id}`, payload, {
      headers: getAuthHeaders()
    });
  },

  deletePackage(id) {
    return axios.delete(`${API_BASE_URL}/${id}`, {
      headers: getAuthHeaders()
    });
  }
};