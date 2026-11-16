import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/freelancer/service-packages',
      name: 'freelancer-service-packages',
      component: () => import('../views/freelancer/ServicePackagesView.vue')
    },

    {
  path: '/register',
  name: 'register',
  component: () => import('../views/RegisterView.vue')
}
  ]
});

export default router;