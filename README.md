# Payment System MSA Demo

## Overview

Kafka 기반 Event-Driven Architecture와 Redis를 활용한 안정적인 결제 시스템을 MSA 구조로 구현한 프로젝트입니다.

결제 요청부터 정산 처리까지의 전체 흐름을 분리하고,
Idempotency, Distributed Lock, Retry, Dead Letter Queue(DLQ)를 적용하여
실제 서비스 수준의 안정성과 확장성을 고려했습니다.


## Architecture

```
Client
  │
  ▼
API Gateway (api-service)
  │
  ├── user-service
  ├── payment-service
  │        │
  │        ▼
  │     Kafka (payment-topic)
  │        │
  ▼        ▼
settlement-service
```


## Event Flow

1. Client가 결제 요청
2. payment-service에서 결제 요청 저장 (PENDING)
3. Kafka Topic으로 이벤트 발행
4. settlement-service에서 이벤트 소비
5. 처리 결과에 따라 상태 변경 (SUCCESS / FAILED)


## Tech Stack

### Backend

* Java 17
* Spring Boot
* Spring Cloud Gateway

### Infrastructure

* Kafka (Event Streaming)
* Redis (Idempotency + Distributed Lock)
* Docker

### Database

* MySQL


## Core Features

### 1. MSA Architecture

* user / payment / settlement 서비스 분리
* 서비스 간 독립적인 배포 및 확장 가능


### 2. Event-Driven Architecture (Kafka)

* 결제와 정산을 비동기 이벤트 기반으로 분리
* 서비스 간 결합도 감소


### 3. Redis Idempotency (중복 요청 방지)

* Idempotency-Key 기반 요청 처리
* 동일 요청 재시도 시 중복 결제 방지


### 4. Distributed Lock (Redisson)

* 사용자 기준 분산락 적용
* 동시 결제 요청으로 인한 Race Condition 방지


### 5. Retry + DLQ (Kafka 안정성)

* 실패 시 최대 3회 재시도
* 재시도 실패 시 DLQ(payment-topic-dlq)로 이동
* 장애 데이터 추적 및 복구 가능


## ERD

```
User
- id
- name
- email
- created_at

Payment
- id
- user_id
- amount
- status (PENDING / SUCCESS / FAILED)
- created_at
```


## API Spec

### Create Payment

POST /payments

Header

```
Idempotency-Key: unique-key
```

Request

```
{
  "userId": 1,
  "amount": 10000
}
```

Response

```
{
  "id": 1,
  "status": "PENDING"
}
```


## How to Run

```bash
docker-compose up
```

서비스 실행 순서

1. user-service
2. payment-service
3. settlement-service
4. api-service


## Test Scenario

### 정상 결제

* amount: 10000
  → SUCCESS 처리


### 실패 테스트

* amount: 60000
  → Retry → 실패 → DLQ 이동


### Idempotency 테스트

* 동일 Idempotency-Key 요청
  → Duplicate Request 발생


## What I Learned

* MSA 기반 서비스 분리 설계
* Kafka Event 기반 비동기 처리 구조
* Redis를 활용한 중복 요청 및 동시성 제어
* Retry / DLQ를 통한 장애 대응 설계


## Related to Real-World System

이 프로젝트는 **Toss Payments**의 결제 시스템 구조를 단순화하여 구현한 것으로,

* 결제 요청 처리
* 이벤트 기반 정산
* 장애 대응 및 데이터 안정성 확보

와 같은 실제 결제 시스템의 핵심 요소를 반영했습니다.
