import request from '@/utils/request'

export function getSeckillLoad() {
  return request({
    url: '/tool/seckill/load',
    method: 'get'
  })
}

export function setSeckillStock(data) {
  return request({
    url: '/tool/seckill/load/stock',
    method: 'post',
    data: data,
    headers: { repeatSubmit: false }
  })
}

export function runSeckillLoad() {
  return request({
    url: '/tool/seckill/load/run',
    method: 'post',
    timeout: 60000,
    headers: { repeatSubmit: false }
  })
}

export function resetSeckillLoad() {
  return request({
    url: '/tool/seckill/load/reset',
    method: 'post',
    headers: { repeatSubmit: false }
  })
}

export function zeroSeckillLoad() {
  return request({
    url: '/tool/seckill/load/zero',
    method: 'post',
    headers: { repeatSubmit: false }
  })
}
