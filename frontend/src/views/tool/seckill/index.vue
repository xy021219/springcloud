<template>
  <div class="app-container">
    <el-card class="seckill-card">
      <div class="seckill-bar">
        <span>{{ text.product }}</span>
        <el-select v-model="productCode" class="seckill-select" @change="load">
          <el-option v-for="item in products" :key="item.code" :label="item.name + ' ' + item.code" :value="item.code" />
        </el-select>
      </div>
      <div class="seckill-stock">{{ text.left }}{{ remain() }}</div>
      <el-button type="primary" :loading="buying" :disabled="!productCode" @click="buy">{{ text.buy }}</el-button>
      <el-button :loading="resetting" :disabled="!productCode" @click="cancelBuy">{{ text.cancel }}</el-button>
    </el-card>
  </div>
</template>

<script setup name="Seckill">
import { buySeckill, cancelSeckill } from '@/api/tool/seckill'
import { listSeckillProduct } from '@/api/tool/orders'
import useUserStore from '@/store/modules/user'
import { seckillHeaders } from '@/utils/seckillSign'

const { proxy } = getCurrentInstance()

const text = {
  product: '\u5546\u54c1',
  left: '\u5f53\u524d\u5546\u54c1\u5269\u4f59\uff1a',
  buy: '\u62a2\u8d2d',
  cancel: '\u53d6\u6d88\u8d2d\u4e70',
  ask: '\u662f\u5426\u53d6\u6d88\u8ba2\u5355',
  cancelled: '\u5df2\u53d6\u6d88\u8ba2\u5355',
  success: '\u5df2\u62a2\u8d2d\u6210\u529f',
  fail: '\u5df2\u552e\u5b8c',
  empty: '\u5df2\u552e\u7f44',
  soldOut: '\u5df2\u7ecf\u5356\u5b8c'
}

const products = ref([])
const productCode = ref('')
const redisStock = ref(0)
const buying = ref(false)
const resetting = ref(false)

function remain() {
  return redisStock.value <= 0 ? text.empty : String(redisStock.value)
}

function load() {
  return listSeckillProduct().then(response => {
    products.value = response.data || []
    if (!productCode.value && products.value.length) {
      productCode.value = products.value[0].code
    }
    const item = products.value.find(row => row.code === productCode.value)
    redisStock.value = item ? Number(item.redisStock || 0) : 0
  })
}

function buy() {
  if (!productCode.value) {
    return
  }
  buying.value = true
  seckillHeaders(useUserStore().id, productCode.value).then(headers => buySeckill(headers)).then(response => {
    redisStock.value = Number(response.data.stock || 0)
    load()
    proxy.$modal.alertSuccess(text.success)
  }).catch(error => {
    const message = error && error.message === text.soldOut ? text.fail : (error && error.message ? error.message : '')
    load().finally(() => {
      proxy.$modal.alertWarning(message)
    })
  }).finally(() => {
    buying.value = false
  })
}

function cancelBuy() {
  if (!productCode.value) {
    return
  }
  proxy.$modal.confirm(text.ask).then(() => {
    resetting.value = true
    return cancelSeckill(productCode.value)
  }).then(response => {
    if (!response) {
      return
    }
    redisStock.value = Number(response.data.stock || 0)
    load()
    proxy.$modal.alertSuccess(text.cancelled)
  }).catch(error => {
    if (error === 'cancel' || error === 'close') {
      return
    }
    proxy.$modal.alertWarning(error.message)
  }).finally(() => {
    resetting.value = false
  })
}

load()
</script>

<style scoped>
.seckill-card {
  max-width: 480px;
}
.seckill-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.seckill-select {
  width: 240px;
}
.seckill-stock {
  margin-top: 16px;
  font-size: 16px;
}
</style>
