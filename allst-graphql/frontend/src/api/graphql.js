export async function gql(query, variables) {
  const response = await fetch('/graphql', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json'
    },
    body: JSON.stringify({ query, variables })
  })
  if (!response.ok) {
    throw new Error('网络请求失败：' + response.status)
  }
  const body = await response.json()
  if (body.errors && body.errors.length) {
    const first = body.errors[0]
    const error = new Error(first.message || '请求失败')
    error.code = first.extensions && first.extensions.code
    throw error
  }
  return body.data
}

export const productStatusLabel = {
  ON_SALE: '在售',
  OFF_SALE: '已下架'
}

export const orderStatusLabel = {
  CREATED: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

export function money(value) {
  const number = Number(value)
  if (Number.isNaN(number)) {
    return value
  }
  return '¥' + number.toFixed(2)
}

export function when(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 19)
}
