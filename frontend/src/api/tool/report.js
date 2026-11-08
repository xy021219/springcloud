import request from '@/utils/request'

export function getReportOverview() {
  return request({
    url: '/tool/report/overview',
    method: 'get'
  })
}
