import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import QuizView from '../views/QuizView.vue'
import DashboardView from '../views/DashboardView.vue'
import axios from 'axios'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: DashboardView
      },
      {
        path: 'quiz/:id?',
        name: 'Quiz',
        component: () => import('../views/QuizView.vue')
      },
      {
        path: 'manage',
        name: 'Manage',
        component: () => import('../views/ManageView.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue')
      }
    ]
  },
  {
    path: '/',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('token')
    
    if (!token) {
      next('/login')
    } else {
      try {
        // 验证 token 有效性
        const response = await axios.get('/api/auth/verify', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        if (response.data.code === 200) {
          next()
        } else {
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          next('/login')
        }
      } catch (error) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        next('/login')
      }
    }
  } else {
    next()
  }
})

export default router 