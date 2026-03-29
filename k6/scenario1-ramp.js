import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 브랜치 전환 시 여기만 바꾸기
// rest-mvc 브랜치:       STAGE=1
// webflux-parallel 브랜치: STAGE=2
// webflux-bounded 브랜치:  STAGE=3
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const STAGE    = __ENV.STAGE    || '1';

const latency   = new Trend('latency_ms', true);
const errorRate = new Rate('error_rate');

export const options = {
  stages: [
    { duration: '30s', target: 50  },
    { duration: '30s', target: 100 },
    { duration: '30s', target: 200 },
    { duration: '30s', target: 0   },
  ],
  summaryTrendStats: ['p(50)', 'p(95)', 'p(99)'],
  thresholds: {
    'error_rate': ['rate<0.1'],
  },
  tags: { stage: STAGE },
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

  sleep(0.5);
}

export function handleSummary(data) {
  const m = data.metrics;
  const p50  = m['latency_ms']?.values?.['p(50)']  ?? '-';
  const p95  = m['latency_ms']?.values?.['p(95)']  ?? '-';
  const p99  = m['latency_ms']?.values?.['p(99)']  ?? '-';
  const errs = m['error_rate']?.values?.rate        ?? '-';
  const reqs = m['http_reqs']?.values?.count        ?? '-';
  const rps  = m['http_reqs']?.values?.rate         ?? '-';

  const summary = `
========================================
Stage ${STAGE} 결과
========================================
총 요청수:  ${reqs}
RPS:        ${typeof rps  === 'number' ? rps.toFixed(1)  : rps}
에러율:     ${typeof errs === 'number' ? (errs*100).toFixed(2)+'%' : errs}

Latency
  p50:  ${typeof p50 === 'number' ? p50.toFixed(0)+'ms' : p50}
  p95:  ${typeof p95 === 'number' ? p95.toFixed(0)+'ms' : p95}
  p99:  ${typeof p99 === 'number' ? p99.toFixed(0)+'ms' : p99}
========================================
`;
  console.log(summary);
  return {
    stdout: summary,
    [`k6/result-stage${STAGE}-ramp.json`]: JSON.stringify(data, null, 2),
  };
}
