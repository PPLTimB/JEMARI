import { createRouter, createWebHistory } from 'vue-router'
import RequestListView from '../views/RequestListView.vue'
import RequestForm from '../views/RequestForm.vue'
import RequestDetail from '../views/RequestDetail.vue'
import PortfolioCard from '../views/PortfolioCard.vue'
import RevisionTermsView from '../views/RevisionTermsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/request-saya',
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
      path: '/revision-terms',
      name: 'revision-terms',
      component: RevisionTermsView,
    },
  ],
})

export default router
