import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '30s', target: 10 }, // ramp up to 10 virtual users
    { duration: '1m', target: 10 },  // hold the load
    { duration: '30s', target: 0 },  // ramp down
  ],
};

function uniqueExternalOrderId() {
  return `ORDER-${__VU}-${__ITER}-${Date.now()}`;
}

function buildOrderPayload(externalOrderId) {
  return {
    externalOrderId,
    customerId: `CUSTOMER-${__VU}`,
    items: [
      { productId: 'PRODUCT-01', quantity: 2, unitPrice: 25.5 },
      { productId: 'PRODUCT-02', quantity: 1, unitPrice: 100.0 },
    ],
  };
}

export default function () {
  const externalOrderId = uniqueExternalOrderId();
  const payload = JSON.stringify(buildOrderPayload(externalOrderId));
  const params = { headers: { 'Content-Type': 'application/json' } };

  const response = http.post(`${BASE_URL}/api/v1/orders`, payload, params);

  check(response, {
    'status is 202 (created) or 200 (duplicate)': (r) => r.status === 202 || r.status === 200,
    'returns the requested externalOrderId': (r) => r.json('externalOrderId') === externalOrderId,
    'response contains a status field': (r) => r.json('status') !== undefined,
  });

  sleep(1);
}
