
# private 생성자나 열거 타입으로 싱글턴임을 보증하라

싱글톤(Singleton)은 특정 클래스의 인스턴스가 애플리케이션 내에서 단 하나만 존재하도록 
보장하는 패턴이다. 

싱글톤은 전역 상태를 생성하거나, 리소스를 공유하는 데 유용하며, 
객체의 중복 생성을 방지하고 전체 시스템에서 하나의 인스턴스만을 사용하도록 한다.






### 싱글톤을 쓰는 이유

싱글톤 패턴을 이용하는 이유는 다음과 같다.



1. 리소스 관리 및 접근 제어 :\
   싱글턴 패턴은 특정 자원이나 서비스에 대한 접근을 제어하는 데 유용하다.
   싱글턴은 한 번에 하나의 인스턴스만이 자원을 사용하도록 보장함으로써,
리소스의 과도한 사용을 방지할 수 있다.


2. 메모리 효율성 :\
싱글턴은 필요한 시점에만 인스턴스를 생성하고, 이후에는 동일 인스턴스를 재사용한다.
   이는 메모리 사용을 줄이고 시스템의 전반적인 효율성을 높이는 데 도움된다.


3. 공유 상태의 일관성 : \
   싱글턴 인스턴스는 애플리케이션에서 전역 상태를 유지한다. 
이는 여러 컴포넌트 간에 상태를 공유하고 일관성을 유지하는 데 유용하다.





### 싱글턴 패턴의 구현



### 1. public static final 필드 방식의 싱글턴
```java
public class Singleton {
    public static final Singleton INSTANCE = new Singleton();

    private Singleton() {
        // private 생성자
    }
}
```

private 생성자는 public static final 필드인 `INSTANCE`를 초기화 할 때 ,
딱 한 번만 호출된다. public이나 protected 생성자가 없기 때문에, 초기화될 때
만들어진 인스턴스가 전체 시스템에서 하나 뿐임이 보장된다.

해당 방식은 Singleton.INSTANCE로 쉬운 접근성을 갖고, 간결하고 명백하다는 것이 특징이며,
해당 클래스가 싱글턴임이 API에 명백히 드러난다는 점이 가장 큰 특징이다.


<br><br>

### 2. 정적 팩터리 메서드 방식의 싱글턴

```java
public class Singleton {
    private static final Singleton INSTANCE = new Singleton();

    private Singleton() {
        // private 생성자
    }

    public static Singleton getInstance() {
        return INSTANCE;
    }
}
```

public static final 방식과 마찬가지로, 해당 방식도 `INSTANCE`를 초기화 할 때 딱 한 번만 호출된다.
정적 팩터리 메서드인 `getInstance()`는 항상 같은 객체의 참조를 반환하므로 인스턴스가 전체 시스템에서 하나 뿐임이 보장된다.


2-1. 정적 팩터리 메서드 지연 초기화 방식의 싱글턴

```java
public class LazySingleton {
    private static LazySingleton instance;

    private LazySingleton() {
        // private 생성자
    }

    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
```

지연 초기화 방식은 객체의 생성을 실제로 사용되는 시점까지 지연시키는 기법이다.
이 방식은 특히 객체의 생성 비용이 높거나, 사용되지 않을 가능성이 있는 경우에 유용하다.


정적 팩터리 방식은 해당 클래스를 싱글턴이 아니게 하고 싶을때, API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다.
예를 들어 `getInstance()`에서 반환 값을 `return new Singleton();`으로만 바꿔주어도 싱글톤이 아니게 된다.

그리고 원한다면 정적 팩터리를 제네릭 싱글턴 팩터리로도 만들 수도 있고, 정적 팩터리의 메서드 참조를 공급자로 사용할 수 있어
훨씬 유용한 면이 있다.


위의 두 방법으로 만든 싱글턴 클래스는 직렬화 하기 위해서 단순히 Serializable 인터페이스를 구현하고 있다면,
역직렬화 과정에서 새로운 인스턴스가 생성될 수 있다. 이를 해결하기 위해서는 readResolve 메서드를 구현해
싱글턴이 보장되게 해야한다는 단점이 있다.

또한, 지연초기화(Lazy Initialization) 방법을 사용하게 된다면,
멀티스레드 환경에서 동시 접근으로 인해 문제가 생길 수 있다.

이를 해결하기위해 메서드 동기화 (Synchronized Method), 더블 체크 락킹 (Double-Checked Locking), 
초기화-온-디맨드 홀더 (Initialization-on-demand Holder) 패턴을 적용해야 한다.


<br><br>


### 3. Enum을 이용한 싱글턴

enum을 사용하여 싱글턴을 구현하는 방법은 간단하면서도 효율적인 방법 중 하나이다.
enum을 이용한 싱글턴 구현은 직렬화(serialization)와 스레드 안전성(thread safety) 문제를 자동으로 해결해주며,
추가적인 구현 없이 싱글턴을 보장한다.

```java
public enum Singleton {
    INSTANCE;
    
    public void doSomething() {
        // 싱글턴이 할 작업
    }
}
```
이 코드에서 Singleton은 enum으로 선언되었으며, INSTANCE는 Singleton의 유일한 인스턴스이다. 
doSomething 메소드는 이 싱글턴 인스턴스에 대한 메소드로, 필요에 따라 다양한 기능을 구현할 수 있다.




```java
public class Main {
    public static void main(String[] args) {
        Singleton singleton = Singleton.INSTANCE;
        singleton.doSomething();
    }
}
```

Singleton 인스턴스에 접근하기 위해서는 위와 같이 Singleton.INSTANCE를 사용하면 된다.
가장 간결하고, 직렬화와 스레드 안전성 문제도 없으며, 리프렉션 공격도 방지할 수 있는 장점이 있지만,
enum은 본질적으로 final 클래스이기 때문에, 상속을 지원하지 않는다는 단점이 있다.


이러한 점들 떄문에, 상속을 받지 않는다면 enum을 사용한 싱글턴 생성이 가장 좋은 방법이다.

---

## Spring에서 싱글턴

Spring 프레임워크에서 싱글턴 패턴을 구현하는 것은 매우 간단하다.

Spring의 핵심 기능 중 하나는 빈(Bean) 관리 기능이며, 기본적으로 Spring 컨테이너는 모든 빈을 싱글턴으로 관리하기 때문이다. 
이는 각각의 빈 정의에 대해 컨테이너 내에서 단 하나의 인스턴스만을 생성하고 관리한다는 것을 의미한다.

### 1. 컴포넌트 스캔

```java
@Component
public class MySingletonService {
    // 클래스 구현
}
```

클래스에 @Component 또는 @Service, @Repository 등의 어노테이션을 사용하여 빈으로 등록하여 싱글턴으로 만든다.


### 2. 수동 빈 등록

```java
@Configuration
public class AppConfig {
    @Bean
    public MySingletonService mySingletonService() {
        return new MySingletonService();
    }
}
```

@Bean 어노테이션으로 수동으로 빈을 등록하여 싱글턴으로 만든다.


### 3. 의존성 주입

```java
@Service
public class SomeService {
   private final MySingletonService mySingletonService;

   @Autowired
   public SomeService(MySingletonService mySingletonService) {
      this.mySingletonService = mySingletonService;
   }

   // 여기서 mySingletonService를 사용하는 로직
}

```

Spring에서 의존성을 주입하는 방법은 여러가지고, 그 중 생성자 주입의 방법이 가장 많이 쓰인다.
Spring에서 의존성 주입을 통해 생성된 인스턴스는 빈으로 등록되어 싱글턴이 보장된다.