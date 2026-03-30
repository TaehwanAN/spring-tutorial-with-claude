/**
 * 공유 모듈 — 여러 도메인 모듈이 공통으로 사용하는 API 응답 형식, 예외 처리 등.
 * OPEN 타입: 모든 모듈에서 자유롭게 접근 가능.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.demo.myapplication.shared;
