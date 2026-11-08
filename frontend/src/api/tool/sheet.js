import request from '@/utils/request'

export function previewSheet(file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: '/tool/sheet/preview',
    method: 'post',
    data,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', repeatSubmit: false },
    timeout: 30000
  })
}

export function exportSheet(data) {
  return request({
    url: '/tool/sheet/export',
    method: 'post',
    data,
    responseType: 'blob',
    headers: { repeatSubmit: false },
    timeout: 30000
  })
}
