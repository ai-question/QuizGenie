import api from './config'

export const authApi = {
  login(username, password) {
    return api.post('/api/login', { username, password })
  },
  
  logout() {
    return api.post('/api/logout')
  },
  
  register(username, password) {
    return api.post('/api/register', { username, password })
  }
} 