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

// Create a single order once, before the load starts, so the GET flow has
// something to retrieve. Its externalOrderId is shared with every iteration.
export function setup() {
  const externalOrderId = `ORDER-GET-${Date.now()}`;
  const payload = JSON.stringify({
    externalOrderId,
    customerId: 'CUSTOMER-001',
    items: [
      { productId: 'PRODUCT-01', quantity: 2, unitPrice: 25.5 },
      { productId: 'PRODUCT-02', quantity: 1, unitPrice: 100.0 },
    ],
  });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const response = http.post(`${BASE_URL}/api/v1/orders`, payload, params);
  check(response, {
    'setup order was accepted': (r) => r.status === 202 || r.status === 200,
  });

  return { externalOrderId };
}

export default function (data) {
  const response = http.get(`${BASE_URL}/api/v1/orders/${data.externalOrderId}`);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'returns the requested externalOrderId': (r) => r.json('externalOrderId') === data.externalOrderId,
    'order contains items': (r) => Array.isArray(r.json('items')) && r.json('items').length > 0,
  });

  sleep(1);
}
