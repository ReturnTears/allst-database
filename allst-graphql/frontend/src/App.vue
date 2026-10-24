<script setup>
import { onMounted, ref } from 'vue'
import { gql } from './api/graphql'
import ProductPanel from './components/ProductPanel.vue'
import CategoryPanel from './components/CategoryPanel.vue'
import CustomerPanel from './components/CustomerPanel.vue'
import OrderPanel from './components/OrderPanel.vue'

const tab = ref('product')
const health = ref('checking')
const tabs = [
  { id: 'product', label: '商品' },
  { id: 'category', label: '分类' },
  { id: 'customer', label: '客户' },
  { id: 'order', label: '订单' }
]

onMounted(async () => {
  try {
    const data = await gql('query { health }')
    health.value = data.health === 'ok' ? 'ok' : 'bad'
  } catch (error) {
    health.value = 'bad'
  }
})
</script>

<template>
  <div class="app">
    <nav class="rail">
      <div class="brand">岸</div>
      <button
        v-for="item in tabs"
        :key="item.id"
        :class="{ on: tab === item.id }"
        type="button"
        @click="tab = item.id"
      >{{ item.label }}</button>
    </nav>
    <main class="main">
      <header class="top">
        <div>
          <h1>岸口货栈</h1>
          <p class="quiet">用一份 GraphQL 请求看商品、客户和订单。</p>
        </div>
        <p class="live" :class="health === 'ok' ? 'ok' : 'bad'">
          {{ health === 'checking' ? '正在连接柜台' : health === 'ok' ? '柜台已连接' : '柜台没连上，先启动 8089' }}
        </p>
      </header>
      <ProductPanel v-show="tab === 'product'" :active="tab === 'product'" />
      <CategoryPanel v-show="tab === 'category'" :active="tab === 'category'" />
      <CustomerPanel v-show="tab === 'customer'" :active="tab === 'customer'" />
      <OrderPanel v-show="tab === 'order'" :active="tab === 'order'" />
    </main>
  </div>
</template>
