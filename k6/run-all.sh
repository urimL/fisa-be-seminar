#!/bin/bash

mkdir -p k6

echo ""
echo "========================================"
echo "시나리오 1: VU 점진적 증가"
echo "========================================"

echo ">>> Stage 1 (REST-MVC) 시작"
k6 run -e STAGE=1 k6/scenario1-ramp.js
echo ">>> Stage 1 완료. 10초 대기..."
sleep 10

echo ">>> Stage 2 (WEBFLUX-PARALLEL) 시작"
k6 run -e STAGE=2 k6/scenario1-ramp.js
echo ">>> Stage 2 완료. 10초 대기..."
sleep 10

echo ">>> Stage 3 (WEBFLUX-BOUNDED) 시작"
k6 run -e STAGE=3 k6/scenario1-ramp.js
echo ">>> Stage 3 완료."

echo ""
echo "========================================"
echo "시나리오 2: VU 50 고정 처리량 비교"
echo "========================================"

echo ">>> Stage 1 (REST-MVC) 시작"
k6 run -e STAGE=1 k6/scenario2-fixed.js
echo ">>> Stage 1 완료. 10초 대기..."
sleep 10

echo ">>> Stage 2 (WEBFLUX-PARALLEL) 시작"
k6 run -e STAGE=2 k6/scenario2-fixed.js
echo ">>> Stage 2 완료. 10초 대기..."
sleep 10

echo ">>> Stage 3 (WEBFLUX-BOUNDED) 시작"
k6 run -e STAGE=3 k6/scenario2-fixed.js
echo ">>> Stage 3 완료."

echo ""
echo "모든 시나리오 완료. k6/ 폴더에서 결과 확인하세요."