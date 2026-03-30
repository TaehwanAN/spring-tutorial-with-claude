# JSON

> 참고: https://docs.spring.io/spring-boot/3.5/reference/features/json.html

Spring Boot는 세 가지 JSON 매핑 라이브러리와 통합을 제공한다.

| 라이브러리 | 상태 | Auto-Config | 빈 타입 | 커스터마이징 |
|-----------|------|------------|---------|------------|
| Jackson | 기본값 | ✓ | `ObjectMapper` | `@JsonComponent`, 설정 프로퍼티 |
| Gson | 지원 | ✓ | `Gson` | `spring.gson.*`, `GsonBuilderCustomizer` |
| JSON-B | 지원 | ✓ | `Jsonb` | 구현체별 방식 |

---

## Jackson

`spring-boot-starter-json`을 통해 자동 설정이 제공된다. Jackson이 클래스패스에 있으면 `ObjectMapper` 빈이 자동으로 등록된다.

> Spring Boot 3.5.x는 Jackson **2.x** (`com.fasterxml.jackson`)를 사용한다.
> Jackson 3.x (`tools.jackson`)는 Spring Boot 4.0+ 전용이다.

### 커스텀 Serializer / Deserializer

`@JsonComponent` 어노테이션으로 커스텀 직렬화/역직렬화기를 Spring 빈으로 등록할 수 있다.

- `@JsonComponent`는 `@Component`를 메타 어노테이션으로 가지므로 컴포넌트 스캔이 적용된다.
- 등록된 빈은 `ObjectMapper`에 자동으로 추가된다.
- `JsonSerializer`, `JsonDeserializer`, `KeyDeserializer` 구현체에 직접 적용 가능하다.

#### 기본 방식 (Jackson 클래스 직접 상속)

```java
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class MyJsonComponent {

    public static class Serializer extends JsonSerializer<MyObject> {
        @Override
        public void serialize(MyObject value, JsonGenerator jgen, SerializerProvider serializers)
                throws IOException {
            jgen.writeStartObject();
            jgen.writeStringField("name", value.getName());
            jgen.writeNumberField("age", value.getAge());
            jgen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<MyObject> {
        @Override
        public MyObject deserialize(JsonParser jsonParser, DeserializationContext ctxt)
                throws IOException {
            ObjectCodec codec = jsonParser.getCodec();
            JsonNode tree = codec.readTree(jsonParser);
            String name = tree.get("name").textValue();
            int age = tree.get("age").intValue();
            return new MyObject(name, age);
        }
    }
}
```

#### Spring Boot 헬퍼 클래스 방식 (권장)

`JsonObjectSerializer` / `JsonObjectDeserializer`를 상속하면 `writeStartObject()` / `writeEndObject()` 호출이 자동 처리되고, `nullSafeValue()` 같은 유틸 메서드를 사용할 수 있다.

```java
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;

@JsonComponent
public class MyJsonComponent {

    public static class Serializer extends JsonObjectSerializer<MyObject> {
        @Override
        protected void serializeObject(MyObject value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeStringField("name", value.getName());
            jgen.writeNumberField("age", value.getAge());
        }
    }

    public static class Deserializer extends JsonObjectDeserializer<MyObject> {
        @Override
        protected MyObject deserializeObject(JsonParser jsonParser, DeserializationContext context,
                ObjectCodec codec, JsonNode tree) throws IOException {
            String name = nullSafeValue(tree.get("name"), String.class);
            int age = nullSafeValue(tree.get("age"), Integer.class);
            return new MyObject(name, age);
        }
    }
}
```

### Jackson Mixins

`@JsonMixin`으로 어노테이션된 클래스를 Spring Boot가 자동으로 스캔하여 `ObjectMapper`에 등록한다 (`JsonMixinModule` 경유).

- 직접 수정할 수 없는 외부 클래스에 Jackson 어노테이션을 추가할 때 사용한다.

---

## Gson

클래스패스에 Gson이 있으면 `Gson` 빈이 자동 설정된다.

- `spring.gson.*` 프로퍼티로 기본 설정 조정 가능
- 세밀한 제어가 필요하면 `GsonBuilderCustomizer` 빈을 등록한다.

---

## JSON-B

JSON-B API와 구현체가 모두 클래스패스에 있으면 `Jsonb` 빈이 자동 설정된다.

- 권장 구현체: **Eclipse Yasson** (Spring Boot가 의존성 관리 제공)
