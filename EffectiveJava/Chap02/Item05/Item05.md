
# 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

많은 클래스는 하나 이상의 자원에 의존지만, 단 하나의 자원에만 의존할 것이라고 생각하는 것은
너무 순진한 생각이다. 

예를 들어 맞춤법 검사기는 사전에 의존하는데,\
실전에서 사전은 언어별로, 특수 어휘용으로, 심지어 테스트용 사전으로 까지 필요할 수 있다.

따라서 맞춤법 검사기 클래스를 만들때, 유틸리티 클래스나 싱글톤 방식을 사용하는 것은
적절하지 않은 방법이다.

다시말해, 사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글톤 방식이
적합하지 않다.


<br><br>


대신 클래스가 여러 자원 인스턴스를 지원해야 하며,\
클래스는 클라이언트가 원하는 자원을 바로 사용해야한다,

이를 만족하는 패턴은 의존 객체 주입 패턴으로, 수 많은 개발자들이 이름이 있다는 사실도
모른 채 사용해온 패턴이다.

## 의존 객체 주입 패턴

의존 객체 주입 패턴은 인스턴스를 생성할 때 생성자에 필요한 자원을 넘겨주는 방식이다.


```java
public class MyService {
    private final Dependency dependency;

    // 의존 객체 주입을 통해 Dependency 객체를 받음
    public MyService(Dependency dependency) {
        this.dependency = dependency;
    }

    public void doSomething() {
        // Dependency 객체 사용
        dependency.performAction();
    }
}

public class Main {
    public static void main(String[] args) {
        // 의존 객체 생성
        Dependency dependency = new ConcreteDependency();

        // MyService에 의존 객체 주입
        MyService service = new MyService(dependency);

        service.doSomething();
    }
}
```

### 의존 객체 주입의 이점

1. 결합도 감소:\
클래스가 특정 의존성의 구체적인 구현에 직접적으로 결합되지 않으므로, 코드의 결합도가 낮아진다.


2. 재사용성 향상:\
의존성이 외부에서 주입되므로, 같은 클래스를 다른 의존성과 함께 재사용하기가 쉬워진다.


3. 테스트 용이성:\
테스트 중에 실제 의존성 대신 모의 객체(mock objects)나 스텁(stubs)을 주입할 수 있어, 단위 테스트가 용이해진다.


4. 유지보수성 향상: \
의존성 변경이 필요할 때, 클래스 코드를 수정하지 않고도 외부에서 새로운 의존성을 주입할 수 있다.




## 팩터리 메서드 패턴


팩터리란 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체를 말한다.

팩터리 메서드 패턴은 객체 생성을 처리하는 인터페이스를 정의하고, 서브 클래스에 객체 생성의
책임을 위임하여 객체 생성에 대한 유연성을 제공한다.

```java
public abstract class Creator {
    public abstract Product factoryMethod();
}

public class ConcreteCreator extends Creator {
    @Override
    public Product factoryMethod() {
        return new ConcreteProduct();
    }
}
```

팩터리 메서드는 위 예제처럼 서브 클래스에서 다른 타입에 객체를 생성할 수 있게도 해주어 객체 생성의 유연성을 제공한다.
또한, 클라이언트 코드가 생성될 객체의 구체적인 클래스를 몰라도 되게 함으로써 결합도를 낮춘다.


<br><br>

이런 팩터리 메서드 패턴을 의존 객체 주입에서도 사용하도록 변형할 수 있는데,
생성자에 자원 팩터리를 넘기는 방법이다.

```java
public class MyService {
    private final ResourceFactory resourceFactory;

    public MyService(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    public void performAction() {
        Resource resource = resourceFactory.createResource();
        // ...
    }
}
```
이러한 방법을 사용하면, 테스트 용이성, 코드 재사용성, 유지보수성 향상 등의 이점을 얻을 수 있고,
객체가 실제로 필요할 때만 생성되므로, 메모리 사용 최적화, 초기 로딩 시간 단축 등의 이점을 제공한다.


