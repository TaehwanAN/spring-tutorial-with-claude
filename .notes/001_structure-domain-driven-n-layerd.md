Spring Boot 구조는 크게 2가지 전략이 있음:

  1️⃣ Layered Architecture (계층형)

    com.demo.myapplication
        controller
            customerController.java
            orderController.java
            productController.java
        service
            customerService.java
            orderService.java
            productService.java
        repository
            customerRepository.java
            orderRepository.java
            productRepository.java
        ...

  2️⃣ Domain-driven Structure (추천)

    com.demo.myapplication
      MyApplication.java
      customer/
        customerController.java
        customerService.java
        customerRepository.java
        internal
            UserInternalLogic.java
      order/
      product/
  customer, order 단위로 묶음

👉 Spring 공식 문서도 점점 도메인 중심 구조를 권장
| 기준  | 도메인 기반               | 계층 기반                    |
| --- | -------------------- | ------------------------ |
| 중심  | 기능 (customer, order) | 역할 (controller, service) |
| 응집도 | 높음                   | 낮음                       |
| 탐색  | 쉬움                   | 어려움                      |
| 확장성 | 좋음                   | 커지면 복잡                   |


