<script setup>
import { reactive, ref, watch } from 'vue'
import { gql } from '../api/graphql'

const props = defineProps({ active: Boolean })
const rows = ref([])
const error = ref('')
const pending = ref(false)
const editingId = ref('')
const form = reactive({ name: '', parentId: '' })

const query = `query {
  categories { id name productCount parent { id name } children { id name } }
}`

watch(() => props.active, (value) => { if (value) load() }, { immediate: true })

async function load() {
  error.value = ''
  try {
    const data = await gql(query)
    rows.value = data.categories
  } catch (e) {
    error.value = e.message
  }
}

function reset() {
  editingId.value = ''
  form.name = ''
  form.parentId = ''
}

function edit(row) {
  editingId.value = row.id
  form.name = row.name
  form.parentId = row.parent ? row.parent.id : ''
}

async function save() {
  pending.value = true
  error.value = ''
  const input = { name: form.name, parentId: form.parentId || null }
  const document = editingId.value
    ? 'mutation($id: ID!, $input: CategoryInput!) { updateCategory(id: $id, input: $input) { id } }'
    : 'mutation($input: CategoryInput!) { createCategory(input: $input) { id } }'
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
  if (!window.confirm('删除分类「' + row.name + '」？')) return
  try {
    await gql('mutation($id: ID!) { deleteCategory(id: $id) }', { id: row.id })
    await load()
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <section class="panel">
    <h2>分类</h2>
    <p v-if="error" class="note">{{ error }}</p>
    <form class="sheet grid" @submit.prevent="save">
      <label>名称<input v-model="form.name" required maxlength="40" /></label>
      <label>上级
        <select v-model="form.parentId">
          <option value="">作为一级分类</option>
          <option v-for="item in rows" :key="item.id" :value="item.id" :disabled="item.id === editingId">{{ item.name }}</option>
        </select>
      </label>
      <div class="actions">
        <button class="primary" type="submit" :disabled="pending">{{ editingId ? '保存修改' : '添加分类' }}</button>
        <button v-if="editingId" class="ghost" type="button" @click="reset">取消编辑</button>
      </div>
    </form>
    <p v-if="!rows.length" class="empty">还没有分类。</p>
    <table v-else>
      <thead><tr><th>名称</th><th>上级</th><th>下级</th><th>商品数</th><th></th></tr></thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.name }}</td>
          <td>{{ row.parent ? row.parent.name : '—' }}</td>
          <td>{{ row.children.map(item => item.name).join('、') || '—' }}</td>
          <td>{{ row.productCount }}</td>
          <td>
            <button class="link" type="button" @click="edit(row)">编辑</button>
            <button class="link" type="button" @click="remove(row)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
