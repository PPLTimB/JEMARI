
import { createRouter, createWebHistory } from 'vue-router'
import RevisionTermsView from '../views/RevisionTermsView.vue'

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
  ],
})

export default router
