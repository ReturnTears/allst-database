<script setup>
import { reactive, ref, watch } from 'vue'
import { gql, when } from '../api/graphql'

const props = defineProps({ active: Boolean })
const rows = ref([])
const total = ref(0)
const error = ref('')
const pending = ref(false)
const editingId = ref('')
const form = reactive({
  name: '', email: '', phone: '',
  receiver: '', addressPhone: '', province: '', city: '', detail: ''
})
const extra = reactive({ customerId: '', receiver: '', phone: '', province: '', city: '', detail: '' })

const query = `query {
  customers(page: { page: 0, size: 20 }) {
    total
    content { id name email phone createdAt addresses { id receiver phone province city detail defaultAddress } }
  }
}`

watch(() => props.active, (value) => { if (value) load() }, { immediate: true })

async function load() {
  error.value = ''
  try {
    const data = await gql(query)
    rows.value = data.customers.content
    total.value = data.customers.total
    if (!extra.customerId && rows.value.length) extra.customerId = rows.value[0].id
  } catch (e) {
    error.value = e.message
  }
}

function reset() {
  editingId.value = ''
  Object.assign(form, { name: '', email: '', phone: '', receiver: '', addressPhone: '', province: '', city: '', detail: '' })
}

function edit(row) {
  editingId.value = row.id
  form.name = row.name
  form.email = row.email
  form.phone = row.phone
}

async function save() {
  pending.value = true
  error.value = ''
  try {
    if (editingId.value) {
      await gql('mutation($id: ID!, $input: CustomerInput!) { updateCustomer(id: $id, input: $input) { id } }', {
        id: editingId.value,
        input: { name: form.name, email: form.email, phone: form.phone }
      })
    } else {
      await gql('mutation($input: CreateCustomerInput!) { createCustomer(input: $input) { id } }', {
        input: {
          name: form.name,
          email: form.email,
          phone: form.phone,
          address: {
            receiver: form.receiver,
            phone: form.addressPhone,
            province: form.province,
            city: form.city,
            detail: form.detail,
            defaultAddress: true
          }
        }
      })
    }
    reset()
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    pending.value = false
  }
}

async function remove(row) {
  if (!window.confirm('删除客户「' + row.name + '」？已有订单的客户会失败。')) return
  try {
    await gql('mutation($id: ID!) { deleteCustomer(id: $id) }', { id: row.id })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function addAddress() {
  error.value = ''
  try {
    await gql('mutation($customerId: ID!, $input: AddressInput!) { createAddress(customerId: $customerId, input: $input) { id } }', {
      customerId: extra.customerId,
      input: { receiver: extra.receiver, phone: extra.phone, province: extra.province, city: extra.city, detail: extra.detail, defaultAddress: false }
    })
    extra.receiver = ''
    extra.phone = ''
    extra.province = ''
    extra.city = ''
    extra.detail = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function removeAddress(id) {
  try {
    await gql('mutation($id: ID!) { deleteAddress(id: $id) }', { id })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function addressText(address) {
  return (address.defaultAddress ? '默认 ' : '') + address.receiver + ' ' + address.province + address.city + address.detail
}
</script>

<template>
  <section class="panel">
    <h2>客户</h2>
    <p v-if="error" class="note">{{ error }}</p>
    <form class="sheet grid" @submit.prevent="save">
      <label>姓名<input v-model="form.name" required /></label>
      <label>邮箱<input v-model="form.email" type="email" required /></label>
      <label>电话<input v-model="form.phone" required /></label>
      <template v-if="!editingId">
        <label>收货人<input v-model="form.receiver" required /></label>
        <label>收货电话<input v-model="form.addressPhone" required /></label>
        <label>省<input v-model="form.province" required /></label>
        <label>市<input v-model="form.city" required /></label>
        <label class="wide">详细地址<input v-model="form.detail" required /></label>
      </template>
      <div class="actions">
        <button class="primary" type="submit" :disabled="pending">{{ editingId ? '保存客户' : '添加客户' }}</button>
        <button v-if="editingId" class="ghost" type="button" @click="reset">取消编辑</button>
      </div>
    </form>
    <form v-if="rows.length" class="sheet grid" @submit.prevent="addAddress">
      <label>给谁加地址
        <select v-model="extra.customerId">
          <option v-for="row in rows" :key="row.id" :value="row.id">{{ row.name }}</option>
        </select>
      </label>
      <label>收货人<input v-model="extra.receiver" required /></label>
      <label>电话<input v-model="extra.phone" required /></label>
      <label>省<input v-model="extra.province" required /></label>
      <label>市<input v-model="extra.city" required /></label>
      <label class="wide">详细地址<input v-model="extra.detail" required /></label>
      <div class="actions"><button class="ghost" type="submit">添加地址</button></div>
    </form>
    <p class="quiet">共 {{ total }} 位客户</p>
    <p v-if="!rows.length" class="empty">还没有客户。</p>
    <table v-else>
      <thead><tr><th>姓名</th><th>邮箱</th><th>电话</th><th>地址</th><th>建档</th><th></th></tr></thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.name }}</td>
          <td>{{ row.email }}</td>
          <td>{{ row.phone }}</td>
          <td>
            <div v-for="address in row.addresses" :key="address.id">
              {{ addressText(address) }}
              <button class="link" type="button" @click="removeAddress(address.id)">删除</button>
            </div>
          </td>
          <td>{{ when(row.createdAt) }}</td>
          <td>
            <button class="link" type="button" @click="edit(row)">编辑</button>
            <button class="link" type="button" @click="remove(row)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
