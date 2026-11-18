import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import RequestListView from '../views/RequestListView.vue'
import RequestForm from '../views/RequestForm.vue'
import RequestDetail from '../views/RequestDetail.vue'
import PortfolioCard from '../views/PortfolioCard.vue'
import FreelancerProfileView from '../views/FreelancerProfileView.vue'
import ServicePackagesView from '../views/freelancer/ServicePackagesView.vue'
import RevisionTermsView from '../views/RevisionTermsView.vue'
import RegisterView from '../views/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/request-saya',
      name: 'request-saya',
      component: RequestListView,
    },
    {
      path: '/request/buat',
      name: 'request-buat',
      component: RequestForm,
    },
    {
      path: '/request/:id',
      name: 'request-detail',
      component: RequestDetail,
    },
    {
      path: '/request/edit/:id',
      name: 'request-edit',
      component: RequestForm,
    },
    {
      path: '/portfolio',
      name: 'portfolio',
      component: PortfolioCard,
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
    {
      path: '/paket/:id',
      name: 'paket-detail',
      component: () => import('../views/PaketDetailView.vue'),
    },
    {
      path: '/freelancer/paket/:id/varian',
      name: 'kelola-varian',
      component: () => import('../views/KelolaVarianView.vue'),
    },
  ],
})

export default router