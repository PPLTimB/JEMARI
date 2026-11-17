import { createRouter, createWebHistory } from 'vue-router'
import RevisionTermsView from '../views/RevisionTermsView.vue'
import FreelancerProfileView from '../views/FreelancerProfileView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/freelancer/profile',
    },
    {
      path: '/freelancer/profile',
      name: 'freelancer-profile',
      component: FreelancerProfileView,
    },
    {
      path: '/revision-terms',
      name: 'revision-terms',
      component: RevisionTermsView,
    },
  ],
})

export default router