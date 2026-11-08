import request from '@/utils/request'

export function getSeckillActivity() {
  return request({
    url: '/tool/seckill/activity',
    method: 'get'
  })
}

export function buySeckill(headers) {
  return request({
    url: '/tool/seckill/order',
    method: 'post',
    headers
  })
}

export function cancelSeckill(productCode) {
  return request({
    url: '/tool/seckill/cancel',
    method: 'post',
    params: { productCode },
    headers: { repeatSubmit: false }
  })
}
