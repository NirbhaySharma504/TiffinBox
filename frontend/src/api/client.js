import axios from "axios";

// All requests go through the API Gateway — never directly to a service.
const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const client = axios.create({ baseURL });

// Attach the JWT (if logged in) to every request.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401 the token is missing/expired — clear it and bounce to login.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (!window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  }
);

export default client;
