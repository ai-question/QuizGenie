import axios from 'axios'

const BASE_URL = '/api'

export const authApi = {
  login(username, password) {
    return axios.post(`${BASE_URL}/login`, { 
      username, 
      password 
    })
  },
  
  logout() {
    const token = localStorage.getItem('token')
    return axios.post(`${BASE_URL}/logout`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  },
  
  register(username, password, email, invitationCode) {
    return axios.post(`${BASE_URL}/register`, { 
      username, 
      password,
      email,
      invitationCode
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
  }
}

export const login = async (username, password) => {
  try {
    const response = await axios.post(`${BASE_URL}/login`, {
      username,
      password
    })
    
    if (response.data.code === 200) {
      // 保存token到localStorage
      localStorage.setItem('token', response.data.data.token)
      return response.data
    } else {
      throw new Error(response.data.message)
    }
  } catch (error) {
    throw error.response?.data || error
  }
} 