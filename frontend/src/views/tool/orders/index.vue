<template>
  <div class="app-container">
    <el-card class="order-card">
      <div class="order-title">{{ text.title }}</div>
      <div class="order-bar">
        <el-select v-model="productCode" :placeholder="text.pick" style="width: 240px">
          <el-option v-for="item in products" :key="item.code" :label="item.name + ' ' + item.code" :value="item.code" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="confirm">{{ text.confirm }}</el-button>
      </div>
      <el-table v-if="queried" :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" :label="text.orderNo" min-width="140" />
        <el-table-column prop="productName" :label="text.productName" min-width="120" />
        <el-table-column prop="productCode" :label="text.productCode" min-width="120" />
        <el-table-column prop="buyer" :label="text.buyer" min-width="120" />
        <el-table-column prop="createTime" :label="text.time" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="SeckillOrders">
import { listSeckillOrders, listSeckillProduct } from '@/api/tool/orders'

const text = {
  title: '\u8ba2\u5355',
  pick: '\u9009\u62e9\u5546\u54c1',
  confirm: '\u786e\u5b9a',
  orderNo: '\u8ba2\u5355\u53f7',
  productName: '\u5546\u54c1\u540d\u79f0',
  productCode: '\u5546\u54c1\u4ee3\u7801',
  buyer: '\u9884\u8d2d\u7528\u6237',
  time: '\u4e0b\u5355\u65f6\u95f4'
}

const products = ref([])
const productCode = ref('')
const orders = ref([])
const loading = ref(false)
const queried = ref(false)

function loadProducts() {
  listSeckillProduct().then(response => {
    products.value = response.data || []
    if (!productCode.value && products.value.length === 1) {
      productCode.value = products.value[0].code
    }
  })
}

function confirm() {
  if (!productCode.value) {
    return
  }
  loading.value = true
  listSeckillOrders(productCode.value).then(response => {
    orders.value = response.data || []
    queried.value = true
  }).finally(() => {
    loading.value = false
  })
}

loadProducts()
</script>

<style scoped>
.order-card {
  max-width: 960px;
}
.order-title {
  font-size: 20px;
  font-weight: 600;
}
.order-bar {
  display: flex;
  gap: 12px;
  margin: 16px 0;
}
</style>
