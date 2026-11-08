import request from '@/utils/request'

export function listSeckillProduct() {
  return request({
    url: '/tool/seckill/product',
    method: 'get'
  })
}

export function listSeckillOrders(productCode) {
  return request({
    url: '/tool/seckill/orders',
    method: 'get',
    params: { productCode }
  })
}
