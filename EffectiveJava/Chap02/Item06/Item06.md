
# 불필요한 객체 생성을 피하라

똑같은 기능의 객체를 매번 생성하기보다는 객체 하나를 재사용하는 편이 나을 때가 많고 빠르다.

극단 적인 예로, String에서 다음과 같이 인스턴스를 생성한다고 해보자.
```java
for(int i = 0; i < 100_000_000; i++) {
    String s = new String("sameObject");
}
```

자바에서 문자열 String은 불변객체이고, 이 코드는 1억번 반복하며 동일한 내용의
새로운 문자열 객체를 생성한다.

이는 자바의 문자열 상수 풀을 사용하지 않아, 메모리가 낭비되는 코드이고,
가독성도 떨어지며, 성능도 떨어지게 된다.

개선된 버전은 아래와 같이 문자열 리터럴을 사용하면 된다.


```java
for(int i = 0; i < 100_000_000; i++) {
    String s = "sameObject";
}
```

위 예시에서 프로그램이 실행될때, JVM은 문자열 상수 풀에서 이 문자열을 찾고, 해당 문자열이
존재하지 않으면 새로운 문자열 객체를 생성해 풀에 추가한다.
이 후로는, 동일한 문자열 객체의 참조를 재 사용해 메모리 효율성과 성능을 높인다.

<br><br>

## 불변 클래스의 정적 팩터리 메서드 사용

생성자 대신 정적 팩터리 메서드를 제공하는 불변 클래스에서는 정적 팩터리 메서드를 사용하는 것이
불필요한 객체 생성을 피할 수 있다.



```java
Boolean trueValue1 = new Boolean("true");
Boolean trueValue2 = new Boolean("true");

System.out.println("trueValue1 == trueValue2: " + (trueValue1 == trueValue2)); // false
```
예를 들어 Boolean 객체를 만들 때, new Boolean(String)으로 새로운 객체를 만든다면
각각의 객체는 다른 메모리를 참조하는 새로운 객체 일 것이다.


```java
Boolean trueValue1 = Boolean.valueOf("true");
Boolean trueValue2 = Boolean.valueOf("true");

System.out.println("trueValue1 == trueValue2: " + (trueValue1 == trueValue2)); // true
```

하지만, 정적 팩터리 메서드인 Boolean.valueOf(String)는 Boolean 클래스 안의 미리 선언된
Boolean.True의 참조를 반환하기때문에 불필요한 객체 생성을 피할 수 있다.

이는, 불변객체 뿐만 아니라 가변객체에서도 변경 되지 않을 것임을 안다면 객체를 재사용 가능하게 하는
방법이다.

<br><br><br>


## 생성 비용이 비싼 객체
생성 비용이 비싼 경우에도 캐싱하여 재사용 하길 권해진다.

예를 들어, DB 연결 객체를 만든다고 가정하고 객체 생성에 1초의 시간이 걸린다고
생각해보자.

```java
public class DatabaseConnection {
    private String connectionString;

    public DatabaseConnection(String connectionString) {
        this.connectionString = connectionString;
        // 데이터베이스 연결 설정에 시간이 걸린다고 가정
        try {
            Thread.sleep(1000); // 시뮬레이션을 위한 1초 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void executeQuery(String query) {
        System.out.println("Executing query on " + connectionString);
        // 쿼리 실행 로직
    }
}
```

이 객체를 DB와 연결할 때마다 호출을 하게 된다면, 호출마다 객체를 생성하기 때문에 1초의 시간이 걸린다는 것이다.


이를 해결하기 위해 이 객체를 팩터리 클래스를 통해 캐싱해두고, 캐싱한 객체를 반환하는 방법을 사용한다.

```java
public class DatabaseConnectionFactory {
    private Map<String, DatabaseConnection> connectionCache = new HashMap<>();

    public DatabaseConnection getConnection(String connectionString) {
        if (!connectionCache.containsKey(connectionString)) { // 객체가 캐싱되지 않았다면, 캐싱한다.
            connectionCache.put(connectionString, new DatabaseConnection(connectionString));
        }
        return connectionCache.get(connectionString); // 캐싱된 객체를 반환한다.
    }
}
```

이렇게 하면, 값비싼 불필요한 객체가 반복 생성되는 것을 방지할 수 있어 성능 저하를 피할 수 있다.

<br><br>

또, 비싼 값을 갖는 예제로 정규식 표현식 예제를 들 수 있다.
String.matches는 정규  표현식으로 문자열 형태를 확인하는 가장 쉬운 방법이지만,

정규표현식에서 사용하는 Pattern 인스턴스는 입력받은 정규표현식에 해당하는
유한 상태 머신을 만들기 때문에 인스턴스 생성 비용이 높다.


```java
public class EmailValidator {
    // 이메일 주소의 유효성을 검사하는 정규식 패턴
    public boolean validate(String email) {
        return email.matcher("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$").matches();
    }
}
```

따라서 메서드가 위 예시처럼 내부에서 만드는 Pattern 인스턴스는, 한 번 쓰고 버려져서 곧바로 GC의 대상이 되고,
불필요한 값비싼 객체를 반복 생성하면서 성능 저하를 초래한다.

이를 개선하기 위해서 Pattern 인스턴스를 정적 초기화를 통해 캐싱해두고,
메서드가 호출될 때마다 재사용하는 방법이 있다.

```java
public class EmailValidator {
    // 이메일 주소의 유효성을 검사하는 정규식 패턴
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    public boolean validate(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
```

이렇게 개선하면, 메서드가 빈번히 호출되는 상황에서 성능을 상당히 끌어올릴 수 있고, 가독성 또한 좋아졌다.


<br><br>

위 방법에서는 정적 초기화를 사용해 캐싱하기 때문에, 메서드를 한 번도 호출하지 않는다면
불필요한 객체를 생성한 것이 아닌가 하는 생각이 들 수 있다.

이때, 메서드가 처음 호출될 때 필드를 초기화 하는 지연 초기화를 사용해 불필요한 초기화를 없앨 수 있지만,
권장하는 방법은 아니다.

지연초기화는 코드를 복잡하게 만드는데 반해, 성능은 크게 개선되지 않을때가 많기 때문에
성능상의 이점이 분명히 입증될때만 사용하길 권한다.

<br><br>

## Map의 keySet은 같은 뷰를 반환한다.

```java
// 여기서 keySet()을 한 번만 호출
Set<String> keys = map.keySet();
for (String key : keys) {
    System.out.println(key);
}

// 맵이 변경된 후 다시 keySet을 호출할 필요가 없음
map.put("Four", 4);
for (String key : keys) {
    System.out.println(key); // "Four"가 포함되어 있음
}
```

Map의 keySet()은 Map객체 안의 키 전부를 담은 Set뷰를 반환한다.
여기서, keySet이 호출때마다 새로운 Set 인스턴스가 만들어지리라 생각할 수 도 있지만,
그럴 때도 있고, 아닐 때도 있다.

반환된 Set 인스턴스가 일반적으로 가변이더라도 반환된 인스턴스들은 기능적으로 똑같아서,
반환한 객체 중 하나를 수정하면 모든 객체가 바뀌게 된다.

따라서, keySet()으로 뷰 객체를 여러 개 만들어도 상관은 없지만, 그럴 필요도 없고, 이득도 없다.

<br><br>

## 오토박싱(Auto Boxing)

오토방식은 기본 타입과 그에 대응하는 박싱된 기본 타입의 구분을 흐려주지만, 
완전히 없애 주는 것은 아니다.


```java
private static long sum() {
    Long sum = 0L;
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }
    return sum;
}
```

위 코드에서 sum 변수가 Long으로 선언되어 있어,
각 반복마다 기본형 'long' 값을 'Long' 객체로 박싱하는 데 비용이 발생하게 된다.

`sum += i` 연산에서 내부적으로는 `sum = Long.valueOf(sum.longValue() + i);`
와 같이 처리되며, 이 과정에서 수 많은 Long 객체가 불필요하게 생성된다.

따라서, 박싱된 기본 타입보다는 기본 타입을 사용하고, 의도치 않은 오토박싱이 숨어들지 않도록 주의해야 한다.

<br><br>

---

# 결론

이번 아이템을 `불필요한 객체 생성은 하지 말라` 라고 오해하면 안 된다.

요즘의 JVM에서는 별다른 일을 하지 않는 작은 객체를 생성하고 회수하는 일이 크게 부담되는 일이 아니다.
또한, 프로그램의 명확성, 간결성, 기능을 위해서 객체를 추가로 생성하는 것이라면 일반적으로 좋은 일이다.

만약, 불필요한 객체 생성을 피하려고 자신만의 객체 풀을 만들게 되면, 오히려 코드를 헷갈리게 만들고 메모리 사용량을 늘려서 오히려 성능을 떨어뜨릴 수 있다.
JVM의 GC 컬렉터는 상당히 잘 최적화 되어 있어 가벼운 객체용을 다룰 때는 직접 만든 객체 풀을 사용할 때보다 훨씬 빠르다.

<br>

그리고 이번 아이템은 아이템 50의 `새로운 객체를 만들어야 한다면 기존 객체를 재사용 하지 마라` 라는 내용과 대조적이다.

아이템 50의 `방어적 복사`가 필요한 상황에서 객체를 재사용했을 때의 피해가, 필요 없는 객체를 반복 생성했을 때의 피해보다 훨씬 
크다는 사실을 기억해 두자.

방어적 복사에 실패하면 언제 터져 나올지 모르는 버그와 보안 구멍으로 이어지지만,
불필요한 객체 생성은 그저 코드 형태와 성능에만 영향을 준다.