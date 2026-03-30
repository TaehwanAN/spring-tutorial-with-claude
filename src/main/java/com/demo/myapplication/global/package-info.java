/**
 * 글로벌 인프라 모듈 — 앱 전역에서 사용되는 설정, 라이프사이클, 모니터링 등 공통 인프라.
 * OPEN 타입: 다른 모든 모듈에서 이 모듈의 하위 패키지에 자유롭게 접근 가능.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.demo.myapplication.global;
