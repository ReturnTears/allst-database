<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { gql, money, orderStatusLabel, when } from '../api/graphql'

const props = defineProps({ active: Boolean })
const orders = ref([])
const customers = ref([])
const products = ref([])
const total = ref(0)
const page = ref(0)
const status = ref('')
const error = ref('')
const pending = ref(false)
const draft = reactive({ customerId: '', addressId: '', remark: '', productId: '', quantity: 1, lines: [] })

const query = `query($status: OrderStatus, $page: PageInput) {
  orders(status: $status, page: $page) {
    total
    content {
      id orderNo status totalAmount remark receiver addressDetail createdAt itemCount
      customer { name }
      items { productName quantity amount }
    }
  }
  customers(page: { page: 0, size: 50 }) {
    content { id name addresses { id receiver province city detail defaultAddress } }
  }
  products(filter: { status: ON_SALE }, page: { page: 0, size: 50 }) {
    content { id name price stock }
  }
}`

const addresses = computed(() => {
  const customer = customers.value.find(item => item.id === draft.customerId)
  return customer ? customer.addresses : []
})

watch(() => props.active, (value) => { if (value) load() }, { immediate: true })
watch(() => draft.customerId, () => {
  const preferred = addresses.value.find(item => item.defaultAddress) || addresses.value[0]
  draft.addressId = preferred ? preferred.id : ''
})

async function load() {
  error.value = ''
  try {
    const data = await gql(query, { status: status.value || null, page: { page: page.value, size: 8 } })
    orders.value = data.orders.content
    total.value = data.orders.total
    customers.value = data.customers.content
    products.value = data.products.content
    if (!draft.customerId && customers.value.length) draft.customerId = customers.value[0].id
    if (!draft.productId && products.value.length) draft.productId = products.value[0].id
  } catch (e) {
    error.value = e.message
  }
}

function addLine() {
  error.value = ''
  if (!draft.productId) return
  if (draft.lines.some(line => line.productId === draft.productId)) {
    error.value = '同一订单里商品不能重复，请合并数量'
    return
  }
  const product = products.value.find(item => item.id === draft.productId)
  draft.lines.push({
    productId: product.id,
    name: product.name,
    price: product.price,
    quantity: Number(draft.quantity)
  })
}

async function create() {
  if (!draft.lines.length) {
    error.value = '先把商品加进这张订单'
    return
  }
  pending.value = true
  error.value = ''
  try {
    await gql('mutation($input: CreateOrderInput!) { createOrder(input: $input) { id } }', {
      input: {
        customerId: draft.customerId,
        addressId: draft.addressId,
        remark: draft.remark || null,
        items: draft.lines.map(line => ({ productId: line.productId, quantity: line.quantity }))
      }
    })
    draft.lines = []
    draft.remark = ''
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    pending.value = false
  }
}

async function changeStatus(row, statusName) {
  const document = statusName === 'CANCELLED'
    ? 'mutation($id: ID!) { cancelOrder(id: $id) { id } }'
    : 'mutation($id: ID!, $status: OrderStatus!) { updateOrderStatus(id: $id, status: $status) { id } }'
  try {
    await gql(document, statusName === 'CANCELLED' ? { id: row.id } : { id: row.id, status: statusName })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function remove(row) {
  if (!window.confirm('删除这张已取消的订单？')) return
  try {
    await gql('mutation($id: ID!) { deleteOrder(id: $id) }', { id: row.id })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function nextActions(statusName) {
  if (statusName === 'CREATED') return [['PAID', '标记已支付'], ['CANCELLED', '取消订单']]
  if (statusName === 'PAID') return [['SHIPPED', '发货'], ['CANCELLED', '取消订单']]
  if (statusName === 'SHIPPED') return [['COMPLETED', '完成']]
  return []
}
</script>

<template>
  <section class="panel">
    <h2>订单</h2>
    <p v-if="error" class="note">{{ error }}</p>
    <form class="sheet" @submit.prevent="create">
      <div class="grid">
        <label>客户
          <select v-model="draft.customerId">
            <option v-for="item in customers" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>
        <label class="wide">收货地址
          <select v-model="draft.addressId">
            <option v-for="item in addresses" :key="item.id" :value="item.id">
              {{ item.receiver }} {{ item.province }}{{ item.city }}{{ item.detail }}
            </option>
          </select>
        </label>
        <label>备注<input v-model="draft.remark" maxlength="200" /></label>
        <label>商品
          <select v-model="draft.productId">
            <option v-for="item in products" :key="item.id" :value="item.id">{{ item.name }}（库存 {{ item.stock }}）</option>
          </select>
        </label>
        <label>数量<input v-model.number="draft.quantity" type="number" min="1" max="999" /></label>
        <div class="actions">
          <button class="ghost" type="button" @click="addLine">加入明细</button>
          <button class="primary" type="submit" :disabled="pending">下单</button>
        </div>
      </div>
      <ul v-if="draft.lines.length" class="lines">
        <li v-for="line in draft.lines" :key="line.productId">{{ line.name }} × {{ line.quantity }} {{ money(line.price) }}</li>
      </ul>
    </form>
    <form class="sheet grid" @submit.prevent="page = 0; load()">
      <label>状态
        <select v-model="status">
          <option value="">全部</option>
          <option v-for="(label, key) in orderStatusLabel" :key="key" :value="key">{{ label }}</option>
        </select>
      </label>
      <div class="actions"><button class="ghost" type="submit">筛选</button></div>
    </form>
    <p v-if="!orders.length" class="empty">还没有订单。</p>
    <table v-else>
      <thead><tr><th>单号</th><th>客户</th><th>金额</th><th>状态</th><th>明细</th><th></th></tr></thead>
      <tbody>
        <tr v-for="row in orders" :key="row.id">
          <td>{{ row.orderNo }}<div class="quiet">{{ when(row.createdAt) }}</div></td>
          <td>{{ row.customer.name }}<div class="quiet">{{ row.receiver }} {{ row.addressDetail }}</div></td>
          <td>{{ money(row.totalAmount) }}</td>
          <td><span class="tag" :class="{ stop: row.status === 'CANCELLED', on: row.status === 'COMPLETED' }">{{ orderStatusLabel[row.status] }}</span></td>
          <td>
            <div v-for="item in row.items" :key="item.productName + item.quantity">{{ item.productName }} × {{ item.quantity }}</div>
          </td>
          <td>
            <button v-for="action in nextActions(row.status)" :key="action[0]" class="link" type="button" @click="changeStatus(row, action[0])">{{ action[1] }}</button>
            <button v-if="row.status === 'CANCELLED'" class="link" type="button" @click="remove(row)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="actions" style="margin-top: 12px">
      <button class="ghost" type="button" :disabled="page === 0" @click="page--; load()">上一页</button>
      <span class="quiet">第 {{ page + 1 }} 页，共 {{ total }} 单</span>
      <button class="ghost" type="button" :disabled="(page + 1) * 8 >= total" @click="page++; load()">下一页</button>
    </div>
  </section>
</template>
