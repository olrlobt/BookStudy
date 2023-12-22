# 생성자에 매개변수가 많다면 빌더를 고려하라

ref) [정적 팩토리](https://olrlobt.tistory.com/70)


자바에서는 객체를 생성하기 위해, 생성자와 정적 팩토리 메소드를 사용한다.

```java
// 생성자를 이용한 객체 생성
Car electricCar = new Car("테슬라");
Car petrolCar = new Car("모닝", 50);
```
```java
// 정적 팩토리 메소드를 이용한 객체 생성
Car electricCar = Car.createElectricCar("테슬라");
Car petrolCar = Car.createPetrolCar("모닝", 50);
//Car hybridCar = Car.createHybridCar("프리우스", "화이트", 2021, 30, 500, false);
```

객체를 생성할때, 선택적인 매개변수는 클래스 내부에서 기본값으로 초기화를 하고 지정하지 않는 경우가
많다. 하지만, 위 예제의 두 경우만 보더라도 50이 무엇을 나타내는 지 쉽게 알기는 어렵다는 단점이 있다.

또한 생성자에 선택적 매개변수가 많아 질수록 다양한 매개변수 조합을 갖는 생성자를 작성해 주어야 한다.

```java
public class Pizza {
    private final String dough; // 필수
    private final String sauce;
    private final String topping;
    private final int hotSauce;
    private final int cheeseSauce;

    public Pizza(String dough) { // 1
        this(dough, "토마토", "치즈", 0, 0);
    }

    public Pizza(String dough, String sauce) { // 2
        this(dough, sauce, "치즈", 0, 0);
    }

    public Pizza(String dough, String sauce, String topping) { // 3
        this(dough, sauce, topping, 0, 0);
    }

    public Pizza(String dough, String sauce, String topping, int hotSauce) { // 4
        this(dough, sauce, topping, hotSauce, 0);
    }
    
    // 더 많은 생성자들은 생략했다.
    
    public Pizza(String dough, String sauce, String topping, int hotSauce, int cheeseSauce) { // 5
        this.dough = dough;
        this.sauce = sauce;
        this.topping = topping;
        this.hotSauce = hotSauce;
        this.cheeseSauce = cheeseSauce;
    }

    // Getter 메소드들
}
```
위 예제에서 dough 변수는 필수 매개변수이고, 나머지 변수들은 선택 매개변수이다.

이때, 사용자가 필수 매개변수인 dough만을 지정해 피자 객체를 생성하게 되면, 생성자는 ( 1 > 2 > 3 > 4 > 5 )의 순서로 호출될 것이다.

이처럼 단계적인 확장을 통해 하나 이상의 매개변수를 추가하여 이전 생성자를 확장하는 방식을
점층적 생성자 패턴(Telescoping Constructor Pattern)이라 한다.

하지만 점층적 생성자 패턴은 매개변수의 수가 많아질수록 생성자의 수가 기하급수적으로
증가하여 관리가 어려워지고, 코드가 복잡해지는 단점이 있다.

<br><br>

이 단점을 보완할 수 있는 방법으로 객체의 생성과 설정을 분리하는 방법인
자바빈즈 패턴(JavaBeans Pattern)을 사용할 수 있다.

```java
public class Pizza {
    private String dough;
    private String sauce;
    private String topping;

    public Pizza() {
        // 매개변수 없는 생성자
    }

    // Setter 메소드
    public void setDough(String dough) {
        this.dough = dough;
    }

    public void setSauce(String sauce) {
        this.sauce = sauce;
    }

    public void setTopping(String topping) {
        this.topping = topping;
    }

    // Getter 메소드들
}
```

자바빈즈 패턴은 매개변수 없는 생성자로 객체를 만든 후, Setter 메서드들을 호출해
원하는 매개변수의 값을 설정하는 방식으로, 아래 예시와 같이 사용한다.

```java
Pizza pizza = new Pizza();
pizza.setDough("씬");
pizza.setSauce("바베큐");
pizza.setTopping("페퍼로니");
```

자바빈즈 패턴에서는 점층적 생성자 패턴에서의 단점이 보이지 않지만, 이 방법 또한, 심각한
단점을 지니고 있다.

자바빈즈 패턴에서는 객체 하나를 만들기 위해 여러개의 메서드를 호출해야하고,
객체가 완전히 생성되기 전까지는 불완전한 상태로 존재한다는 것이다. 또한, 객체가
생성된 이후에도 상태가 변경될 수 있어, 스레드 안전성을 얻으려면 freezing 작업을
해 주어야한다.

```java
public void setDough(String dough) {
        if (!isFrozen) {
            this.dough = dough;
        }
}

public void freeze() {
        isFrozen = true; // 이제부터 객체는 변경할 수 없는 상태가 됨
}

Pizza pizza = new Pizza();
// 객체 생성 후
pizza.freeze(); // 객체 '동결'
pizza.setTopping("치즈"); // 동결 후 변경은 적용되지 않는다.
```

freezing 작업은 객체 생성이 끝난 후, freeze()를 호출하여 객체를 수정 불가능한 상태, 즉 사용 가능한 상태로 만든다.
하지만, freezing 작업 역시, 프로그래머가 freeze()를 확실히 호출 해 주었는 지
컴파일러가 보증할 방법이 없어서 런타임 오류에 취약하다는 단점이 있다.

<br><br>

정리하자면, 선택적 매개변수가 많은 상황에서\
점층적 생성자 패턴은 안전성이 있지만, 가독성이 떨어지고,\
자바 빈즈 패턴은 가독성이 있지만, 안정성이 떨어지게 된다.

<br>


이러한 단점들을 보완하면서, 매개변수가 많은 객체를 만들기
위한 방법으로는 빌더 패턴(Builder Pattern)을 사용할 수 있다.

---

# 빌더 패턴(Builder Pattern)

빌더 패턴(Builder Pattern)은 복잡한 객체의 생성 과정을 단순화하기 위한 디자인 패턴이다.
특히 객체 생성에 필요한 매개변수가 많거나, 객체 생성 과정이 복잡할 때 유용한 패턴이다.



## 빌더 패턴의 구체 클래스 구현

일반적인 빌더 패턴의 구조는 다음과 같다.

```java
public class Pizza {
    private final String dough; // 필수
    private final String sauce;
    private final String topping;
    private final int hotSauce;
    private final int cheeseSauce;

    private Pizza(Builder builder) {
        this.dough = builder.dough;
        this.sauce = builder.sauce;
        this.topping = builder.topping;
        this.hotSauce = builder.hotSauce;
        this.cheeseSauce = builder.cheeseSauce;
    }

    
    //Builder 인터페이스 또는 추상 클래스
    public static class Builder {
        private final String dough; // 필수
        private String sauce = "토마토"; // 기본값
        private String topping = "치즈"; // 기본값
        private int hotSauce = 0;
        private int cheeseSauce = 0;

        public Builder(String dough) { // 필수 매개변수 생성자
            this.dough = dough;
        }

        public Builder sauce(String sauce) {
            this.sauce = sauce;
            return this;
        }

        public Builder topping(String topping) {
            this.topping = topping;
            return this;
        }

        public Builder hotSauce(int hotSauce) {
            this.hotSauce = hotSauce;
            return this;
        }

        public Builder cheeseSauce(int cheeseSauce) {
            this.cheeseSauce = cheeseSauce;
            return this;
        }

        public Pizza build() {
            return new Pizza(this);
        }
    }
    
    // Getter 메소드들...
}
```

위 예시는 구체 클래스를 이용한 구현 방식으로,
빌더 패턴은 추상 클래스나 구체 클래스를 이용하여 구현할 수 있다.

구체 클래스는 보통 객체 안에 내부 클래스로 구현되는데, 내부 클래스로 구현하면 자연스럽게 캡슐화를
유지할 수 있고, 가독성과 접근성이 향상된다는 이점이 있다.

<br>

빌더의 구조는 다음과 같다.

1. 객체의 필수 매개변수를 private final 필드로 갖고,
객체의 선택 매개변수는 일반 private 필드로 갖는다.


2. 필수 매개변수를 빌더 생성자의 매개변수로 받고 있으며,
선택 매개변수들은 빌더에 Setter와 비슷한 형식의 메서드를 갖고 있다.
이 메서드들은 각각 this를 반환하며, 이 this 값은 Builder 객체 자신이다.


3. build() 메서드를 통하여 최종 객체(Pizza)를 생성한다.



필수 매개변수를 private final 필드로 갖으면서, 객체의 불변성을 보장한다. 또한 빌더의 this를 반환하는 메서드들은
this로 빌더 객체 자신을 반환하기 때문에, 메서드 체이닝을 사용하여 더 편리하고 가독성 높은 코드를 작성할 수 있다.



빌더를 이용하여 객체를 생성할 때는, 아래와 같이 생성한다.

```java

Pizza pizza = new Pizza.Builder("씬")
                .sauce("바베큐")
                .topping("페퍼로니")
                .hotSauce(5)
                .cheeseSauce(3)
                .build();

```

필수 매개변수인 dough는 Builder의 생성자를 통해 설정하고,
선택적 매개변수들은 체인 형태의 메소드 호출을 통해 설정했다.
최종적으로는 build 메소드를 호출하여 Pizza 객체를 생성한다.

위 코드에서 알 수 있듯이, 어떤 매개변수에 어떤 값이 들어가는 지 명확히 알 수 있다는 것,
메서드 체이닝을 통해 가독성이 좋다는 것이 빌더 패턴의 가장 큰 특징이다.



## 빌더 패턴의 추상클래스 구현

빌더 패턴을 추상클래스로 유연하게 설계하게 되면 재사용성과 확장성을 높일 수 있는데, 특히 다양한 종류의
유사한 객체를 생성할 때 유용하다.


예를 들어,\
피자를 만들 수 있는 PizzaBuilder를 추상 클래스로 정의한다.


```java
public abstract class PizzaBuilder {
    protected String dough;
    protected String sauce;
    protected String topping;
    protected int hotSauce;
    protected int cheeseSauce;

    public PizzaBuilder dough(String dough) {
        this.dough = dough;
        return this;
    }

    public PizzaBuilder sauce(String sauce) {
        this.sauce = sauce;
        return this;
    }

    public PizzaBuilder topping(String topping) {
        this.topping = topping;
        return this;
    }

    public PizzaBuilder hotSauce(int hotSauce) {
        this.hotSauce = hotSauce;
        return this;
    }

    public PizzaBuilder cheeseSauce(int cheeseSauce) {
        this.cheeseSauce = cheeseSauce;
        return this;
    }

    public abstract Pizza build();
}
```

이 추상 클래스를 구현한 구체적인 빌더 클래스를 정의한다.


이 예제에서는 각각 피자의 종류인 매운피자와 치즈 피자를 구현하였고,
각각 메서드에서 build() 단계를 오버라이드하여, 추가적인 설정을 넣어주었다.

```java
public class SpicyPizzaBuilder extends PizzaBuilder {
    @Override
    public Pizza build() {
        // SpicyPizzaBuilder는 핫소스를 기본으로 추가.
        hotSauce = Math.max(hotSauce, 5); // 최소 핫소스 레벨을 5로 설정
        return new Pizza(dough, sauce, topping, hotSauce, cheeseSauce);
    }
}

public class CheesyPizzaBuilder extends PizzaBuilder {
    @Override
    public Pizza build() {
        // CheesyPizzaBuilder는 추가 치즈 소스를 기본으로 설정.
        cheeseSauce = Math.max(cheeseSauce, 3); // 최소 치즈 소스 양을 3으로 설정
        return new Pizza(dough, sauce, topping, hotSauce, cheeseSauce);
    }
}
```


이제 추상 클래스를 구현한 구체적인 빌더 클래스로 객체를 생성한다.


```java
Pizza spicyPizza = new SpicyPizzaBuilder()
                    .dough("씬")
                    .sauce("바베큐")
                    .topping("페퍼로니")
                    .build();

Pizza cheesyPizza = new CheesyPizzaBuilder()
                    .dough("두꺼운")
                    .sauce("화이트")
                    .topping("모짜렐라")
                    .build();
```

이렇게 함으로써, 쉽게 코드를 확장하고 유연한 코드 작성이 가능하다.



### 자바 빈즈를 빌더처럼 만들면 안 될까?

빌더의 내부 메소드를 보면 Setter와 비슷하게 생겨서, 자바 빈즈 패턴에 반환값만 설정해주면 쉽게 구현할 수 있을 것 같았다.


```java
// 자바 빈즈 패턴의 Setter에 반환 값을 주었다.
public Pizza setSauce(String sauce) {
        this.sauce = sauce;
        return this;
}

// 메서드 체이닝이 가능하다.
Pizza pizza = new Pizza()
                .dough("씬");
```

이렇게 구현하면, 메서드 체이닝이 가능하기 때문에 가독성이 좋은 코드를 작성할 수 있는 것은 사실이다.
하지만 여전히 객체가 생성된 후에도 객체의 상태를 변경할 수 있기 때문에
객체의 불변성과 객체의 완전성은 보장되지 않는다.


---

빌더 패턴은 객체를 만들기 위해서 빌더부터 만들어야 한다.\
빌더 생성 비용이 크지는 않지만 성능에 민감한 상황에서는 문제가 될 수 있고, 매개변수의 갯수가 많지
않은 상황에서는 점층적 생성자 패턴보다 코드가 장황해질 수 있다. (4개 이상은 되어야 값어치를 한다고 한다.)

따라서, 항상 빌더로 만드는 것이 좋은것은 아니다.
하지만, API는 시간이 지날수록 매개변수가 많아지는 경향이 있기 때문에, 애초에 빌더로 시작하는
편이 나을때가 많다.




