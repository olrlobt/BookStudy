# 인스턴스화를 막으려거든 private 생성자를 사용하라


유틸리티 클래스나 상수만을 담고 있는 클래스에서는 상태를 가지지 않고, 단지 메서드나 상수만을
제공하기 때문에 인스턴스가 필요하지 않다.

이러한 클래스들은 인스턴스화를 막음으로써, 클래스의 설계의도와 사용 방식을 명확하게 전달하여
불필요한 객체 생성을 방지할 수 있다.


```java
public class UtilityClass {
    // 생성자를 private으로 선언하여 외부에서의 접근을 막음
    private UtilityClass() {
        // 생성자 내에서 예외를 던져 실수로 내부에서도 인스턴스화를 방지할 수 있음
        throw new AssertionError("No UtilityClass instances for you!");
    }

    public static void utilityMethod() {
        // 유틸리티 메서드 구현
    }
}
```