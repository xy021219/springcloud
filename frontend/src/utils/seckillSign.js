export const SECKILL_SECRET = 'seckill-demo-secret'
export const SECKILL_ACTIVITY_ID = '1'

export async function seckillHeaders(userId, productCode) {
  const requestId = crypto.randomUUID()
  const timestamp = String(Date.now())
  const payload = requestId + '\n' + SECKILL_ACTIVITY_ID + '\n' + String(userId) + '\n' + timestamp
  const sign = await hmacHex(SECKILL_SECRET, payload)
  const headers = {
    'X-Request-Id': requestId,
    'X-Activity-Id': SECKILL_ACTIVITY_ID,
    'X-Timestamp': timestamp,
    'X-Sign': sign,
    repeatSubmit: false
  }
  if (productCode) {
    headers['X-Product-Code'] = productCode
  }
  return headers
}

async function hmacHex(secret, payload) {
  const encoder = new TextEncoder()
  const key = await crypto.subtle.importKey(
    'raw',
    encoder.encode(secret),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  )
  const raw = await crypto.subtle.sign('HMAC', key, encoder.encode(payload))
  return [...new Uint8Array(raw)].map(value => value.toString(16).padStart(2, '0')).join('')
}
