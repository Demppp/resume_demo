import {createRouter, createWebHistory} from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: '/student',
        name: 'Student',
        component: () => import('@/views/Student.vue')
      },
      {
        path: '/student-profile/:id',
        name: 'StudentProfile',
        component: () => import('@/views/StudentProfile.vue')
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
      },
      {
        path: '/warning',
        name: 'Warning',
        component: () => import('@/views/Warning.vue')
      },
      {
        path: '/analytics',
        name: 'Analytics',
        component: () => import('@/views/Analytics.vue')
      },
      {
        path: '/operation-log',
        name: 'OperationLog',
        component: () => import('@/views/OperationLog.vue')
      },
      {
        path: '/semester',
        name: 'Semester',
        component: () => import('@/views/Semester.vue')
      },
      {
        path: '/print-report',
        name: 'PrintReport',
        component: () => import('@/views/PrintReport.vue')
      }
    ]
  },
  {
    path: '/data-screen',
    name: 'DataScreen',
    component: () => import('@/views/DataScreen.vue')
  }
]

const router = createRouter({
  history: createWebHistory('/resumeDemo/'),
  routes
})

export default router
