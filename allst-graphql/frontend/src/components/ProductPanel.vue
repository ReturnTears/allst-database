<script setup>
import { reactive, ref, watch } from 'vue'
import { gql, money, productStatusLabel, when } from '../api/graphql'

const props = defineProps({ active: Boolean })
const rows = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(0)
const error = ref('')
const pending = ref(false)
const editingId = ref('')
const keyword = ref('')
const status = ref('')
const categoryId = ref('')
const form = reactive({
  name: '',
  description: '',
  price: '',
  stock: 0,
  status: 'ON_SALE',
  categoryId: ''
})

const listQuery = `query($filter: ProductFilter, $page: PageInput) {
  products(filter: $filter, page: $page) {
    total page size
    content { id name description price stock status inStock createdAt category { id name } }
  }
  categories { id name }
}`

watch(() => props.active, (value) => { if (value) load() }, { immediate: true })

function filter() {
  const value = {}
  if (keyword.value.trim()) value.keyword = keyword.value.trim()
  if (status.value) value.status = status.value
  if (categoryId.value) value.categoryId = categoryId.value
  return value
}

async function load() {
  error.value = ''
  try {
    const data = await gql(listQuery, { filter: filter(), page: { page: page.value, size: 8 } })
    rows.value = data.products.content
    total.value = data.products.total
    categories.value = data.categories
    if (!form.categoryId && data.categories.length) form.categoryId = data.categories[0].id
  } catch (e) {
    error.value = e.message
  }
}

function reset() {
  editingId.value = ''
  form.name = ''
  form.description = ''
  form.price = ''
  form.stock = 0
  form.status = 'ON_SALE'
  form.categoryId = categories.value.length ? categories.value[0].id : ''
}

function edit(row) {
  editingId.value = row.id
  form.name = row.name
  form.description = row.description || ''
  form.price = row.price
  form.stock = row.stock
  form.status = row.status
  form.categoryId = row.category.id
}

async function save() {
  pending.value = true
  error.value = ''
  const input = {
    name: form.name,
    description: form.description || null,
    price: String(form.price),
    stock: Number(form.stock),
    status: form.status,
    categoryId: form.categoryId
  }
  const document = editingId.value
    ? 'mutation($id: ID!, $input: ProductInput!) { updateProduct(id: $id, input: $input) { id } }'
    : 'mutation($input: ProductInput!) { createProduct(input: $input) { id } }'
  try {
    await gql(document, editingId.value ? { id: editingId.value, input } : { input })
    reset()
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    pending.value = false
  }
}

async function remove(row) {
  if (!window.confirm('删除「' + row.name + '」？已被订单引用的商品会失败。')) return
  try {
    await gql('mutation($id: ID!) { deleteProduct(id: $id) }', { id: row.id })
    await load()
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <section class="panel">
    <h2>商品台账</h2>
    <p v-if="error" class="note">{{ error }}</p>
    <form class="sheet grid" @submit.prevent="save">
      <label>名称<input v-model="form.name" required maxlength="80" /></label>
      <label>价格<input v-model="form.price" inputmode="decimal" required placeholder="128.00" /></label>
      <label>库存<input v-model.number="form.stock" type="number" min="0" required /></label>
      <label>状态
        <select v-model="form.status">
          <option value="ON_SALE">在售</option>
          <option value="OFF_SALE">已下架</option>
        </select>
      </label>
      <label>分类
        <select v-model="form.categoryId" required>
          <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
        </select>
      </label>
      <label class="wide">说明<input v-model="form.description" maxlength="500" /></label>
      <div class="actions">
        <button class="primary" type="submit" :disabled="pending">{{ editingId ? '保存修改' : '添加商品' }}</button>
        <button v-if="editingId" class="ghost" type="button" @click="reset">取消编辑</button>
      </div>
    </form>
    <form class="sheet grid" @submit.prevent="page = 0; load()">
      <label>关键字<input v-model="keyword" placeholder="名称或说明" /></label>
      <label>销售状态
        <select v-model="status">
          <option value="">全部</option>
          <option value="ON_SALE">在售</option>
          <option value="OFF_SALE">已下架</option>
        </select>
      </label>
      <label>分类
        <select v-model="categoryId">
          <option value="">全部</option>
          <option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option>
        </select>
      </label>
      <div class="actions"><button class="ghost" type="submit">筛选</button></div>
    </form>
    <p v-if="!rows.length" class="empty">还没有商品。先有分类，再添加到这里。</p>
    <table v-else>
      <thead>
        <tr><th>名称</th><th>分类</th><th>价格</th><th>库存</th><th>状态</th><th>录入</th><th></th></tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.name }}<div class="quiet">{{ row.description }}</div></td>
          <td>{{ row.category.name }}</td>
          <td>{{ money(row.price) }}</td>
          <td>{{ row.stock }}</td>
          <td><span class="tag" :class="row.status === 'ON_SALE' ? 'on' : 'off'">{{ productStatusLabel[row.status] }}</span></td>
          <td>{{ when(row.createdAt) }}</td>
          <td>
            <button class="link" type="button" @click="edit(row)">编辑</button>
            <button class="link" type="button" @click="remove(row)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="actions" style="margin-top: 12px">
      <button class="ghost" type="button" :disabled="page === 0" @click="page--; load()">上一页</button>
      <span class="quiet">第 {{ page + 1 }} 页，共 {{ total }} 件</span>
      <button class="ghost" type="button" :disabled="(page + 1) * 8 >= total" @click="page++; load()">下一页</button>
    </div>
  </section>
</template>
