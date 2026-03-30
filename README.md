# Spring Boot 튜토리얼

Spring Boot의 핵심 개념들을 단계적으로 학습하기 위한 튜토리얼 프로젝트입니다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 |
| Spring Boot | 3.2.4 |
| 빌드 도구 | Maven |
| 주요 의존성 | spring-boot-starter-web, spring-boot-starter-actuator, spring-boot-devtools |

## 빠른 시작

```bash
# 애플리케이션 실행 (기본 dev 프로파일, 포트 9999)
mvn spring-boot:run

# prod 프로파일로 실행 (포트 9090)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# JAR 패키징
mvn package

# 테스트 실행
mvn test
```

### 환경 변수 (DB 연결 시 필요)

```bash
export DB_URL=jdbc:mysql://localhost:3306/mydb
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

## 프로젝트 구조

도메인 중심 패키지 구조(Domain-driven Structure)를 사용합니다. Spring 공식 문서에서도 권장하는 방식으로, 기능(도메인) 단위로 코드를 묶어 응집도를 높입니다.

```
com.demo.myapplication/
├── MyApplication.java              # 애플리케이션 진입점
├── common/                         # 공통 유틸리티
├── domain/
│   └── example/                    # 도메인 모듈 (customer, order 등이 여기에 추가됨)
└── global/                         # 애플리케이션 전역 인프라
    ├── listener/application/       # 스프링 부트 생명주기 이벤트 리스너
    ├── monitoring/health/          # 커스텀 헬스 인디케이터 및 K8s 프로브
    ├── runner/commandline/         # 시작 시 실행되는 Runner 빈
    ├── ExternalValueConfig.java                         # @Value 바인딩
    ├── ExternalConfigurationPropertiesConfig.java       # @ConfigurationProperties 바인딩
    ├── ApplicationArgumentsLoader.java                  # CLI 인수 처리
    └── ApplicationShutdownDisposableBean.java           # 종료 처리
```

## 프로파일 설정

| 항목 | dev (기본) | prod |
|------|-----------|------|
| 포트 | 9999 | 9090 |
| 로그 레벨 | DEBUG | WARN |
| 헬스 상세 정보 | 항상 표시 | 인가된 사용자만 |
| Tomcat 최대 스레드 | 10 | 200 |

## 설정 파일 우선순위

```
./config/application-{profile}.yml   ← 최우선
./config/application.yml
src/main/resources/application-{profile}.yml
src/main/resources/application.yml   ← 최하위
```

`application.yml`은 다음 모듈 설정 파일을 동적으로 임포트합니다:
- `config/db-config.yml` — MySQL + HikariCP 설정
- `config/api-config.yml` — API 설정
- `config/batch-config.yml` — 배치 설정

## Actuator 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `/actuator/health` | 헬스 체크 (K8s Liveness/Readiness 프로브 포함) |
| `/actuator/info` | 애플리케이션 정보 |
| `/actuator/startup` | 시작 성능 메트릭 |

## 학습 노트

`.notes/` 디렉토리에 이 프로젝트에서 다루는 Spring Boot 개념들의 학습 자료가 있습니다.

| 파일 | 내용 |
|------|------|
| `001~004` | 아키텍처 패턴 (계층형, DDD, Clean, MSA) |
| `005~007` | 동적 런타임, 자동 구성, Lazy 초기화 |
| `008~012` | 애플리케이션 가용성, 이벤트, Runner, 종료 코드 |
| `013` | 가상 스레드 (Java 21) |
| `014-*` | 외부 설정 (프로퍼티 소스, YAML, 랜덤 값, 임포트) |
