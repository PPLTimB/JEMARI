import { createRouter, createWebHistory } from 'vue-router'
import RevisionTermsView from '../views/RevisionTermsView.vue'
import RegisterView from '../views/RegisterView.vue'
import LoginView from '../views/LoginView.vue'
import ProfileView from '../views/ProfileView.vue'
import EditProfileView from '../views/EditProfileView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/revision-terms',
    },
    {
      path: '/revision-terms',
      name: 'revision-terms',
      component: RevisionTermsView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/profile',
      name: 'account-profile',
      component: ProfileView,
      meta: { requiresAuth: true, role: 'UMKM' },
    },
    {
      path: '/profile/edit',
      name: 'account-profile-edit',
      component: EditProfileView,
      meta: { requiresAuth: true, role: 'UMKM' },
    },
  ],
})

export default router
