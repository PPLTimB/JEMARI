import { createRouter, createWebHistory } from 'vue-router'
import FreelancerProfileView from '../views/FreelancerProfileView.vue'
import ServicePackagesView from '../views/freelancer/ServicePackagesView.vue'
import RevisionTermsView from '../views/RevisionTermsView.vue'
import PortfolioView from '../views/PortfolioView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/portfolio',
    },
    {
      path: '/portfolio',
      name: 'portfolio',
      component: PortfolioView,
    },
    {
      path: '/freelancer/profile',
      name: 'freelancer-profile',
      component: FreelancerProfileView,
    },
    {
      path: '/freelancer/packages',
      name: 'freelancer-packages',
      component: ServicePackagesView,
    },
    {
      path: '/revision-terms',
      name: 'revision-terms',
      component: RevisionTermsView,
    },
  ],
})

export default router
