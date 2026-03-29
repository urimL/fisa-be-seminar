import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const STAGE    = __ENV.STAGE    || '1';

const latency   = new Trend('latency_ms', true);
const errorRate = new Rate('error_rate');

export const options = {
  vus:      50,
  duration: '30s',
  tags: { stage: STAGE },
  summaryTrendStats: ['p(50)', 'p(95)'],
  thresholds: {
    'error_rate': ['rate<0.1'],
  },
};

export default function () {
  const productId = Math.ceil(Math.random() * 5);
  const res = http.get(`${BASE_URL}/product/${productId}`, {
    tags: { stage: STAGE },
  });

  const ok = check(res, {
    'status 200': (r) => r.status === 200,
  });

  latency.add(res.timings.duration, { stage: STAGE });
  errorRate.add(!ok);

  sleep(0.1);
}

export function handleSummary(data) {
  const m = data.metrics;
  const p50  = m['latency_ms']?.values?.['p(50)']  ?? '-';
  const p95  = m['latency_ms']?.values?.['p(95)']  ?? '-';
  const reqs = m['http_reqs']?.values?.count        ?? '-';
  const rps  = m['http_reqs']?.values?.rate         ?? '-';
  const errs = m['error_rate']?.values?.rate        ?? '-';

  const summary = `
========================================
Stage ${STAGE} 결과 (VU 50 고정, 30s)
========================================
총 요청수:  ${reqs}
RPS:        ${typeof rps  === 'number' ? rps.toFixed(1)  : rps}
에러율:     ${typeof errs === 'number' ? (errs*100).toFixed(2)+'%' : errs}

Latency
  p50:  ${typeof p50 === 'number' ? p50.toFixed(0)+'ms' : p50}
  p95:  ${typeof p95 === 'number' ? p95.toFixed(0)+'ms' : p95}
========================================
`;
  console.log(summary);
  return {
    stdout: summary,
    [`k6/result-stage${STAGE}-fixed.json`]: JSON.stringify(data, null, 2),
  };
}
