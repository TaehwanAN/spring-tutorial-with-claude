mvn spring-boot:run -Dspring-boot.run.arguments=--debug
---
mkdir -p ./debug/conditions-evaluation-report
mvn spring-boot:run -Dspring-boot.run.arguments=--debug \
> ./debug/conditions-evaluation-report/$(date +%Y%m%d%H%M%S).log
---