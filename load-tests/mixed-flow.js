import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Time to wait for the asynchronous outbox/RabbitMQ processing to complete
// before retrieving the order. Configurable to match different environments.
const PROCESSING_WAIT_SECONDS = Number(__ENV.PROCESSING_WAIT_SECONDS || 3);

export const options = {
  stages: [
    { duration: '30s', target: 10 }, // ramp up to 10 virtual users
    { duration: '1m', target: 10 },  // hold the load
    { duration: '30s', target: 0 },  // ramp down
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],    // less than 5% of requests may fail
    http_req_duration: ['p(95)<1000'], // 95% of requests under 1000 ms
  },
};

function uniqueExternalOrderId() {
  return `ORDER-MIX-${__VU}-${__ITER}-${Date.now()}`;
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
  const params = { headers: { 'Content-Type': 'application/json' } };

  // 1. Create the order.
  const createResponse = http.post(
    `${BASE_URL}/api/v1/orders`,
    JSON.stringify(buildOrderPayload(externalOrderId)),
    params
  );
  check(createResponse, {
    'create returns 202 (created) or 200 (duplicate)': (r) => r.status === 202 || r.status === 200,
  });

  // 2. Wait for the asynchronous processing to finish.
  sleep(PROCESSING_WAIT_SECONDS);

  // 3. Retrieve the order and confirm it was processed.
  const getResponse = http.get(`${BASE_URL}/api/v1/orders/${externalOrderId}`);
  check(getResponse, {
    'get returns 200': (r) => r.status === 200,
    'returns the requested externalOrderId': (r) => r.json('externalOrderId') === externalOrderId,
    'order was calculated': (r) => r.json('status') === 'CALCULATED',
  });

  sleep(1);
}
