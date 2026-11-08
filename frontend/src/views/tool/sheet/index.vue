<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-upload
          action="#"
          :show-file-list="false"
          accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          :http-request="handleUpload"
          :before-upload="beforeUpload"
        >
          <el-button type="primary" icon="Upload" v-hasPermi="['tool:sheet:list']">上传表格</el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Download" :disabled="selected.length < 1 || selected.length > 3" @click="handleExport" v-hasPermi="['tool:sheet:list']">导出</el-button>
      </el-col>
      <el-col :span="1.5" v-if="fileName" class="sheet-file-name">{{ fileName }}</el-col>
    </el-row>

    <el-alert
      title="勾选 1 到 3 列后才能改文字。导出时只保存这些列的修改，其余列保持上传时的原文。只读取第一个工作表，文件不超过 5 MB、500 行、50 列。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-table v-if="headers.length" v-loading="loading" :data="rows" border>
      <el-table-column
        v-for="(header, index) in headers"
        :key="index"
        :label="header"
        :min-width="180"
      >
        <template #header>
          <el-checkbox :model-value="selected.includes(index)" @change="(checked) => toggleColumn(index, checked)">
            {{ header }}
          </el-checkbox>
        </template>
        <template #default="scope">
          <el-input v-if="selected.includes(index)" v-model="scope.row[index]" />
          <span v-else>{{ original[scope.$index][index] }}</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="请上传 .xlsx，第一行是表头" />
  </div>
</template>

<script setup name="SheetEdit">
import { previewSheet, exportSheet } from '@/api/tool/sheet'
import { blobValidate } from '@/utils/ruoyi'
import { saveAs } from 'file-saver'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const fileName = ref('')
const headers = ref([])
const rows = ref([])
const original = ref([])
const selected = ref([])

function clearTable() {
  headers.value = []
  rows.value = []
  original.value = []
  selected.value = []
}

function beforeUpload(file) {
  const lowerName = file.name.toLowerCase()
  if (!lowerName.endsWith('.xlsx')) {
    proxy.$modal.msgError('只支持 .xlsx 文件')
    clearTable()
    fileName.value = ''
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    proxy.$modal.msgError('文件超过 5 MB')
    clearTable()
    fileName.value = ''
    return false
  }
  return true
}

function handleUpload(option) {
  loading.value = true
  fileName.value = option.file.name
  previewSheet(option.file).then(response => {
    headers.value = response.data.headers
    rows.value = response.data.rows.map(row => row.slice())
    original.value = response.data.rows.map(row => row.slice())
    selected.value = []
  }).catch(() => {
    clearTable()
  }).finally(() => {
    loading.value = false
  })
}

function toggleColumn(index, checked) {
  if (checked) {
    if (selected.value.length >= 3) {
      proxy.$modal.msgWarning('最多修改 3 列')
      return
    }
    selected.value = selected.value.concat(index)
    return
  }
  selected.value = selected.value.filter(item => item !== index)
  rows.value.forEach((row, rowIndex) => {
    row[index] = original.value[rowIndex][index]
  })
}

function handleExport() {
  const editedColumns = selected.value.slice().sort((a, b) => a - b)
  const payload = {
    headers: headers.value,
    editedColumns,
    rows: rows.value.map((row, rowIndex) => row.map((value, column) => {
      return editedColumns.includes(column) ? value : original.value[rowIndex][column]
    }))
  }
  exportSheet(payload).then(async (data) => {
    if (blobValidate(data)) {
      saveAs(new Blob([data]), 'edited.xlsx')
      return
    }
    const text = await data.text()
    const response = JSON.parse(text)
    proxy.$modal.msgError(response.msg || '导出失败')
  })
}
</script>

<style scoped>
.sheet-file-name {
  line-height: 32px;
  color: var(--el-text-color-secondary);
}
</style>
