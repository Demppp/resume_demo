import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    children: [
      {
        path: '/student',
        name: 'Student',
        component: () => import('@/views/Student.vue')
      },
      {
        path: '/attendance',
        name: 'Attendance',
        component: () => import('@/views/Attendance.vue')
      },
      {
        path: '/exam',
        name: 'Exam',
        component: () => import('@/views/Exam.vue')
      },
      {
        path: '/diary',
        name: 'Diary',
        component: () => import('@/views/Diary.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

