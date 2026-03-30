# SSL Support

> 참고: https://docs.spring.io/spring-boot/reference/features/ssl.html
>
> **현재 프로젝트에는 미적용** — 추후 HTTPS/mTLS 적용 시 참고

---

## 개요

Spring Boot는 **SSL 번들(bundle)** 개념으로 인증서/키 설정을 중앙화한다.
설정 prefix: `spring.ssl.bundle` → 하위 `.jks` (KeyStore 방식) 또는 `.pem` (PEM 방식)

---

## JKS (Java KeyStore) 방식

### 서버 측 — Keystore (인증서 + 개인키 보호)

```yaml
spring:
  ssl:
    bundle:
      jks:
        mybundle:
          key:
            alias: "application"
          keystore:
            location: "classpath:application.p12"
            password: "secret"
            type: "PKCS12"
```

### 클라이언트 측 — Truststore (서버 인증서 신뢰)

```yaml
spring:
  ssl:
    bundle:
      jks:
        mybundle:
          truststore:
            location: "classpath:server.p12"
            password: "secret"
```

- Base64 인코딩 컨텐츠 지원: `base64:` 접두사 사용
- 환경변수로 번들 이름 설정 시 **소문자로 변환**됨
- 전체 프로퍼티 목록: `JksSslBundleProperties` 참고

---

## PEM Certificate 방식

### 서버 측

```yaml
spring:
  ssl:
    bundle:
      pem:
        mybundle:
          keystore:
            certificate: "classpath:application.crt"
            private-key: "classpath:application.key"
```

### 클라이언트 측

```yaml
spring:
  ssl:
    bundle:
      pem:
        mybundle:
          truststore:
            certificate: "classpath:server.crt"
```

### 인라인 인증서 (파일 없이 직접 내용 입력)

```yaml
spring:
  ssl:
    bundle:
      pem:
        mybundle:
          truststore:
            certificate: |
              -----BEGIN CERTIFICATE-----
              MIID1zCCAr+gAwIBAgIUNM5Q...
              -----END CERTIFICATE-----
```

`BEGIN` / `END` 마커가 있으면 파일 경로가 아닌 **인라인 PEM 컨텐츠**로 처리.

---

## SSL 번들 적용 대상

번들 이름으로 참조 가능한 컴포넌트:
- 내장 웹 서버 (Embedded Web Server)
- 데이터 기술 (Data technologies)
- REST 클라이언트

---

## 프로그래밍 방식 (`SslBundles` 빈)

Spring Boot가 `SslBundles` 빈을 자동 구성한다.

```java
@Component
public class MyComponent {
    public MyComponent(SslBundles sslBundles) {
        SslBundle sslBundle = sslBundles.getBundle("mybundle");
        SSLContext sslContext = sslBundle.createSslContext();
    }
}
```

| `SslBundle` 메서드 | 역할 |
|--------------------|------|
| `getStores()` | `KeyStore` 인스턴스 및 비밀번호 접근 |
| `getManagers()` | `KeyManagerFactory`, `TrustManagerFactory`, `KeyManager[]`, `TrustManager[]` 접근 |
| `createSslContext()` | 새 `SSLContext` 인스턴스 생성 |

---

## SSL 번들 Hot Reload

지원 서버: **Tomcat**, **Netty**

```yaml
spring:
  ssl:
    bundle:
      pem:
        mybundle:
          reload-on-update: true
          keystore:
            certificate: "file:/some/directory/application.crt"
            private-key: "file:/some/directory/application.key"
```

- 파일 와처가 인증서/키 파일 모니터링, 변경 감지 시 SSL 번들 리로드
- Tomcat이 커넥터의 인증서 자동 교체
- **주의**: `classpath:` 경로 불가 → `file:` 경로만 동작
- 파일 와처 quiet period 조정:

```properties
spring.ssl.bundle.watch.file.quiet-period=<duration>
```

---

## JKS vs PEM 비교

| 항목 | JKS 방식 | PEM 방식 |
|------|---------|---------|
| 파일 형식 | `.jks`, `.p12` (PKCS12) | `.crt`, `.key` |
| 생성 도구 | `keytool` | OpenSSL 등 |
| 인라인 컨텐츠 | Base64 (`base64:`) | 직접 PEM 블록 또는 Base64 |
| Hot Reload | 미지원 | 지원 (`reload-on-update: true`) |
