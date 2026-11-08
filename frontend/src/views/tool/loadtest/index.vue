<template>
  <div class="app-container">
    <el-card class="load-card">
      <div class="load-title">{{ text.title }}</div>
      <div class="load-product">{{ productName }} {{ productCode }}</div>
      <div class="load-fields">
        <span>{{ text.redis }}</span>
        <el-select v-model="redisMode" class="load-mode">
          <el-option :label="text.plus" value="add" />
          <el-option :label="text.minus" value="sub" />
        </el-select>
        <el-input-number v-model="redisDelta" :min="0" :step="1" />
        <span>{{ text.db }}</span>
        <el-select v-model="dbMode" class="load-mode">
          <el-option :label="text.plus" value="add" />
          <el-option :label="text.minus" value="sub" />
        </el-select>
        <el-input-number v-model="dbDelta" :min="0" :step="1" />
        <el-button :loading="saving" @click="save">{{ text.save }}</el-button>
      </div>
      <div class="load-actions">
        <el-button type="primary" :loading="running" @click="run">{{ text.run }}</el-button>
        <el-button :loading="resetting" @click="reset">{{ text.reset }}</el-button>
        <el-button :loading="zeroing" @click="zero">{{ text.zero }}</el-button>
        <a class="load-link" :href="insightUrl" target="_blank" rel="noopener">{{ text.insight }}</a>
      </div>
      <div v-if="message" class="load-message">{{ message }}</div>
      <el-row :gutter="16">
        <el-col :span="12">
          <div class="load-panel-title">{{ text.before }}</div>
          <div>{{ text.redisStock }}{{ show(before.redisStock) }}</div>
          <div>{{ text.dbStock }}{{ show(before.dbStock) }}</div>
          <div class="load-summary">{{ text.summary }}{{ show(before.summary) }}</div>
          <div>{{ text.buyers }}{{ before.buyers.length }}</div>
          <div class="load-buyers">
            <el-tag v-for="name in shown(before.buyers, openBefore)" :key="'b-' + name" class="load-tag">{{ name }}</el-tag>
          </div>
          <el-button v-if="before.buyers.length > preview" link type="primary" @click="openBefore = !openBefore">{{ openBefore ? text.fold : text.expand }}</el-button>
        </el-col>
        <el-col :span="12">
          <div class="load-panel-title">{{ text.after }}</div>
          <div>{{ text.redisStock }}{{ show(after.redisStock) }}</div>
          <div>{{ text.dbStock }}{{ show(after.dbStock) }}</div>
          <div class="load-summary">{{ text.summary }}{{ show(after.summary) }}</div>
          <div>{{ text.buyers }}{{ after.buyers.length }}</div>
          <div class="load-buyers">
            <el-tag v-for="name in shown(after.buyers, openAfter)" :key="'a-' + name" class="load-tag">{{ name }}</el-tag>
          </div>
          <el-button v-if="after.buyers.length > preview" link type="primary" @click="openAfter = !openAfter">{{ openAfter ? text.fold : text.expand }}</el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup name="SeckillLoad">
import { getSeckillLoad, resetSeckillLoad, runSeckillLoad, setSeckillStock, zeroSeckillLoad } from '@/api/tool/loadtest'

const { proxy } = getCurrentInstance()

const text = {
  title: '200 \u4e2a\u7528\u6237\u540c\u65f6\u62a2',
  save: '\u786e\u8ba4\u4fee\u6539\u5e93\u5b58',
  run: '\u6267\u884c\u6d4b\u8bd5',
  reset: '\u91cd\u7f6e',
  zero: '\u91cd\u7f6e\u5e93\u5b58',
  plus: '\u589e\u52a0',
  minus: '\u51cf\u5c11',
  before: '\u6267\u884c\u524d',
  after: '\u6267\u884c\u540e',
  redis: 'Redis \u5e93\u5b58',
  db: '\u6570\u636e\u5e93\u5e93\u5b58',
  redisStock: 'Redis \u5e93\u5b58\uff1a',
  dbStock: '\u6570\u636e\u5e93\u5e93\u5b58\uff1a',
  buyers: '\u9884\u8d2d\u7528\u6237\uff1a',
  summary: '\u6c47\u603b\uff1a',
  empty: '\u6ca1\u6709',
  expand: '\u5c55\u5f00',
  fold: '\u6536\u8d77',
  insight: '\u5728 RedisInsight \u4e2d\u67e5\u770b'
}

const preview = 8

function emptyView() {
  return { redisStock: '', dbStock: '', summary: '', buyers: [], insightUrl: 'http://127.0.0.1:5540/', productCode: '', productName: '', orderCount: 0 }
}

const before = ref(emptyView())
const after = ref(emptyView())
const insightUrl = ref('http://127.0.0.1:5540/')
const productCode = ref('')
const productName = ref('')
const redisDelta = ref(0)
const dbDelta = ref(0)
const redisMode = ref('add')
const dbMode = ref('add')
const saving = ref(false)
const running = ref(false)
const resetting = ref(false)
const zeroing = ref(false)
const message = ref('')
const openBefore = ref(false)
const openAfter = ref(false)

function show(value) {
  return value === '' || value === null || value === undefined ? text.empty : value
}

function shown(list, open) {
  if (open || list.length <= preview) {
    return list
  }
  return list.slice(0, preview)
}

function payload() {
  return {
    redisStock: Number(redisDelta.value || 0),
    dbStock: Number(dbDelta.value || 0),
    redisMode: redisMode.value,
    dbMode: dbMode.value
  }
}

function fillInputs(view) {
  productCode.value = view.productCode || productCode.value
  productName.value = view.productName || productName.value
  insightUrl.value = view.insightUrl || insightUrl.value
}

function soldCount() {
  const view = after.value.productCode ? after.value : before.value
  return Number(view.orderCount || 0)
}

function load() {
  getSeckillLoad().then(response => {
    before.value = response.data
    fillInputs(response.data)
  })
}

function save() {
  saving.value = true
  setSeckillStock(payload()).then(response => {
    before.value = response.data
    fillInputs(response.data)
    message.value = response.msg
  }).catch(error => {
    message.value = error.message
  }).finally(() => {
    saving.value = false
  })
}

function run() {
  running.value = true
  openBefore.value = false
  openAfter.value = false
  runSeckillLoad().then(response => {
    before.value = response.data.before
    after.value = response.data.after
    fillInputs(response.data.after)
    insightUrl.value = response.data.after.insightUrl || insightUrl.value
    message.value = response.data.after.summary
  }).catch(error => {
    message.value = error.message
  }).finally(() => {
    running.value = false
  })
}

function reset() {
  const sold = soldCount()
  proxy.$modal.confirm('\u5c06\u91cd\u7f6e\u6240\u6709\u8ba2\u5355\uff0c\u5e76\u628a\u5e93\u5b58\u5f52\u8fd8\u3002\u5df2\u8d2d' + sold + '\uff0cRedis\u5e93\u5b58\u4e0d\u53d8\uff0c\u6570\u636e\u5e93\u5e93\u5b58\u589e\u52a0' + sold + '\u3002').then(() => {
    resetting.value = true
    return resetSeckillLoad()
  }).then(response => {
    if (!response) {
      return
    }
    before.value = response.data
    after.value = emptyView()
    fillInputs(response.data)
    openBefore.value = false
    openAfter.value = false
    message.value = response.msg
  }).catch(error => {
    if (error === 'cancel' || error === 'close') {
      return
    }
    message.value = error.message
  }).finally(() => {
    resetting.value = false
  })
}

function zero() {
  zeroing.value = true
  zeroSeckillLoad().then(response => {
    before.value = response.data
    fillInputs(response.data)
    message.value = response.msg
  }).catch(error => {
    message.value = error.message
  }).finally(() => {
    zeroing.value = false
  })
}

load()
</script>

<style scoped>
.load-card {
  max-width: 960px;
}
.load-title {
  font-size: 20px;
  font-weight: 600;
}
.load-product {
  margin-top: 8px;
}
.load-fields {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.load-mode {
  width: 100px;
}
.load-actions {
  margin: 16px 0;
}
.load-link {
  margin-left: 12px;
}
.load-message {
  margin-bottom: 12px;
  word-break: break-word;
}
.load-panel-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.load-summary {
  margin: 8px 0;
}
.load-buyers {
  margin-top: 8px;
}
.load-tag {
  margin: 0 8px 8px 0;
}
</style>
