
# Finalizer와 Cleaner 사용을 피하라


## Finalizer

finalize() 메소드는 java.lang.Object 클래스에 정의되어 있으며, 
자바에서 객체가 가비지 컬렉션에 의해 제거될 때 실행된다.
즉, Finalizer는 자바에서 객체가 소멸될 때 마지막으로 수행할 수 
있는 작업을 정의하는 데 사용된다.

주로 파일 핸들, 네트워크 연결, 데이터베이스 연결과 같은 
시스템 리소스를 정리하는 용도가 이런 작업이다.

하지만, Finalizer는 예측할 수 없고, 상황에 따라 위험할 수 있어 불필요하며, 오작동, 낮은 성능,
이식성 문제의 원인으로 기본적으로는 쓰지 말아야한다.


### finalize()를 상속한 리소스 예제

```java
public class Resource {
    
    private boolean isOpen;

    public Resource() {
        this.isOpen = true; // Resource open.
    }

    // 리소스 사용을 위한 메소드
    public void useResource() {
        if (!isOpen) {
            throw new IllegalStateException("Resource is closed.");
        }
    }

    public void closeResource() {
        isOpen = false; // Resource closed.
    }

    // finalize 메소드 오버라이드
    @Override
    protected void finalize() throws Throwable {
        
        if (isOpen) { 
            closeResource(); // 리소스가 아직 열려있다면, 리소스를 닫음
        }
        super.finalize();  // finalize 메소드를 super 클래스에게도 호출
    }

    public static void main(String[] args) {
        
        Resource resource = new Resource(); // 리소스 객체 생성
        resource.useResource(); // 리소스 사용

        // 리소스 해제를 명시적으로 호출하지 않음
        // 객체가 가비지 컬렉션될 때 finalize 메소드가 호출됨
    }
}


```





<br>

### Finalizer 불확실한 실행

Finalizer는 다음과 같은 방식으로 작동된다.

1. 객체가 더 이상 필요하지 않을 때, 즉 더 이상 참조되지 않을 때 객체는 GC의 대상이 된다.
2. 가비지 컬렉터가 이 객체를 회수하기 전에, JVM은 finalize() 메소드를 호출한다.
3. finalize() 메소드에서는 객체가 사용하던 자원을 해제하거나, 정리하는 등의 작업을 수행할 수 있다.
4. finalize() 메소드가 실행된 후, 객체는 가비지 컬렉터에 의해 실제로 메모리에서 제거된다.

Finalizer의 동작 방식에서 객체가 GC의 대상이 되어야만 작동한다는 것을 알 수 있다.
즉, GC 알고리즘에 따라 finalize()가 언제 실행될 지 예측할 수 없다.

또한, 자바에서 Finalizer 스레드는 일반적으로 다른 애플리케이션 스레드보다 낮은 우선순위를 가진다. 
이는 시스템의 리소스가 제한적일 때나 시스템이 높은 부하 하에 있을 때
Finalizer 스레드가 실행될 기회를 충분히 얻지 못하게 만들 수 있다.

JAVA 명세에도 어떤 스레드가 Finalizer를 실행할지 구체적으로 명시하지 않고 있는데,
이는 JVM 구현에서 Finalizer의 동작이 일관되지 않을 수 있다는 것을 의미한다.



<br>

### Finalizer 성능 저하

객체에 Finalizer가 정의되어 있으면, GC는 객체를 단순히 메모리에서 회수하는 것 이상의
작업을 수행해야 한다. 이런 객체들은 일반 가비지 컬렉션 프로세스에서 제외되고,
finalize()가 실행된 후에만 메모리에서 제거된다.

이 과정은 GC의 효율을 떨어뜨려 전체 시스템의 성능에 영향을 미치게 된다.


또한, 낮은 우선순위로 인해 Finalizer 스레드가 실행되지 않으면, 
해당 객체들이 메모리를 계속 점유하게 된다. 
이는 리소스 해제 지연과 메모리 누수로 이어질 수 있으며, 
시스템의 전반적인 성능에 영향을 줄 수 있다.

<br>

### Finalizer 동시성 문제

Finalizer는 다른 스레드에서 비동기적으로 실행되므로, 동시성 문제를 야기할 수 있다.

또한,  Finalizer 내에서 예외가 발생하면, 이는 무시되며 객체의 정상적인 정리 과정을 방해할 수 있다.


<br>

### Finalizer 보안 문제

자바에서 Finalizer는 객체가 가비지 컬렉션에 의해 제거될 때 호출되는 finalize() 메소드를 포함한다. 
이 과정에서 발생할 수 있는 보안 취약점, 즉 Finalizer 공격은 두 가지 주요 방식으로 이루어진다.

#### 객체 재생성과 보안 검사 우회
객체 재생성: \
Finalizer 공격의 첫 번째 전략은 finalize() 메소드를 오버라이딩하여,
가비지 컬렉터가 객체를 수집하려는 순간에 객체를 다시 "살려내는" 것이다.
이는 finalize() 내에서 객체에 대한 새로운 참조를 생성하여,
객체가 가비지 컬렉션에서 제외되도록 만드는 방식으로 이루어진다.

보안 검사 우회: \
두 번째 전략은 객체가 파괴되는 시점에 실행되는
Finalizer를 통해 보안 검사를 우회하거나 민감한 정보에 접근하는 것이다.
이 방식으로, 애플리케이션의 정상적인 흐름에서는 불가능한 보안 관련 행위들을 수행할 수 있게 된다.

#### 위험성: 데이터 노출과 시스템 보안 무력화
이러한 공격 방식은 크게 두 가지 위험을 초래한다.

데이터 노출: \
이미 사용이 완료되어 보안상 취약한 상태인 객체가 다시 활성화될 수 있다.
이 과정에서 민감한 데이터가 노출되어, 개인정보 유출 등의 심각한 문제를 일으킬 수 있다.

시스템 보안 무력화: \
공격자는 Finalizer 공격을 통해 애플리케이션의 보안 메커니즘을 우회할 수 있다. 
이는 시스템에 더 깊이 침투하거나, 더 심각한 손상을 가하는 결과를 초래할 수 있다.



<br>
<br>


### runFinalizersOnExit()의 결함

Finalizer를 실행 할 수 있게 보장해주는 메서드가 2개 있는데,
System.runFinalizersOnExit와 그 쌍둥이인 Runtime.runFinalizersOnExit이다.
하지만 이 두 메서드는 심각한 결함이 있어서 수십년간 지탄 받아왔다.


이 메서드들은 프로그램이 종료될 때 남아 있는 모든 객체에 대해 finalize() 메소드를 강제로 호출하는 기능을 한다.

하지만 이 역시, 앞서 설명한 Finalizer의 동시성문제, 보안 문제, 성능 저하 문제를 모두 포함하고 있어,
자바 9이후 이 메서드들은 사용이 권장되지 않거나 제거되었다.

<br>


### Finalizer 동작중 발생한 예외 처리

finalize() 메소드 내에서 발생하는 예외는 JVM에 의해 잡히지 않는다. 
만약 finalize() 메소드 실행 중에 예외가 발생하면, 
이 예외는 무시되고, 해당 메소드의 나머지 부분은 실행되지 않는다.

즉, 중요한 리소스 해제나 정리 작업이 완료되지 않을수도 있다는 말이다.



---

    
# Cleaner

자바 9에서는 Finalizer는 사용 자제 API로 지정되었고 Cleaner를 그 대안으로 소개하기도 했다.

Cleaner도 Finalizer와 마찬가지로, 리소스를 정리하기 위해 사용되어지기 위한 목적으로 설계되었다.

하지만, Cleaner는 Finalizer보다는 덜 위험하지만, 여전히 예측 불가능하고, 느리고 일반적으로
불필요하다.


```java
public class Resource {
    private static final Cleaner Cleaner = Cleaner.create();

    private static class ResourceCleaner implements Runnable {
        @Override
        public void run() {
            // 리소스 정리 로직
        }
    }

    private final Cleaner.Cleanable cleanable;

    public Resource() {
        this.cleanable = Cleaner.register(this, new ResourceCleaner()); // Resource 객체에 대한 정리 작업 등록
    }

    public void useResource() {
        System.out.println("Resource is being used.");
    }

    public static void main(String[] args) {
        Resource resource = new Resource();
        resource.useResource();

        // Resource 객체에 대한 참조를 명시적으로 제거
        // Cleaner가 작동하기 위해서는 객체에 대한 모든 강한 참조가 제거되어야 함
        resource = null;

        // GC 수행시 Cleaner에서 객체 정리 로직 run() 수행
    }
}

```

### Cleaner가 해결한 Finalizer의 문제

제어 가능한 스레드: \
Cleaner는 자체적으로 관리하는 스레드를 사용하여 정리 작업을 수행하기 때문에,
finalize()가 실행되는 스레드가 불명확하고 제어하기 어려운 것과 대비된다.
Cleaner의 스레드는 특정 정리 작업에 할당되어 더 효율적이고 관리하기 쉽다.

안정성과 예측 가능성: \
Finalizer는 예외 발생 시 객체 정리가 중단될 수 있으며, 이로 인해 리소스 누수가 발생할 수 있다. 
반면 Cleaner는 예외 처리를 좀 더 안정적으로 관리할 수 있으며,
예측 가능한 방식으로 리소스를 정리한다.

리소스 누수와 안전성: \
Finalizer는 잘못 사용될 경우 리소스 누수를 일으킬 수 있다.
Cleaner는 이러한 리소스 누수의 위험을 줄이고, 보다 안전한 리소스 관리를 제공한다.



### Cleaner가 여전히 가지고 있는 문제

비동기적 실행: \
Cleaner의 정리 작업은 객체가 가비지 컬렉터에 의해 회수된 후 비동기적으로 수행된다.
이는 Finalizer와 유사하게, 정리 작업의 실행 시점을 정확히 예측할 수 없게 만든다.

리소스 해제 지연: \
Cleaner도 Finalizer와 마찬가지로, 리소스가 즉시 해제되지 않을 수 있다.
가비지 컬렉터가 객체를 회수하고, Cleaner가 해당 작업을 수행하는 데까지 시간이 걸릴 수 있으며,
이는 특히 리소스가 제한적인 환경에서 문제가 될 수 있다.


---

# Finalizer와 Cleaner의 대안책



## AutoCloseable 인터페이스 구현

Finalizer와 Cleaner의 대안책으로는 AutoCloseable이 있다.
단순히 AutoCloseable 인터페이스를 구현하여 close() 메서드를 호출하는 것으로
명시적으로 리소스를 해제할 수 있다.


```java
public class CustomResource implements AutoCloseable {
    public CustomResource() {
        //Resource opened.
    }
    
    @Override
    public void close() {
        //Resource closed.
    }

    public static void main(String[] args) {
        CustomResource resource = null;
        try {
            resource = new CustomResource();
            //resource do something...
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


```


## try-with-resources 구문 사용

try-with-resources 구문은 Finalizer와 Cleaner의 대안으로 가장 권장되는 방법이다.

try-with-resources 구문은 AutoCloseable 인터페이스를 구현하는 객체를 자동으로 관리하기 때문에,
이 구문 안에서 선언된 리소스는 구문이 종료될 때 자동으로 close() 메서드를 호출하여 리소스를 안전하게 닫는다.

```java
public class ResourceManagementExample {

    public static void main(String[] args) {
        // 파일 읽기
        try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 파일 쓰기
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write("Hello, world!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


```


### 명시적 리소스 관리의 장점

자원 해제 보장: \
try-with-resources 구문을 사용하면, 
리소스를 사용하는 코드 블록이 종료될 때 자동으로 리소스가 해제된다. 
이는 리소스 누수를 방지한다.

예외 처리 용이: \
이 구문을 사용하면, 리소스 사용 중 발생하는 예외를 쉽게 처리할 수 있다. 
또한 리소스 해제 중 발생하는 예외도 적절히 처리할 수 있다.

코드의 가독성 향상: \
리소스 관리 코드가 명확하고 간결해져, 가독성과 유지보수성이 향상된다.



---

# Finalizer와 Cleaner의 쓰임새

Finalizer와 Cleaner를 앞서 본 것과 같이 명시적 리소스 해제로 대체할 수 있다면,
도대체 어디에 쓰일까?

## 1. 최종 안전망

객체의 정리가 try-with-resources를 통해 처리되지 않은 경우,
Finalizer나 Cleaner가 최종 안전망으로 사용할 수 있다.

특히 예외 발생이나 비정상적인 프로그램 흐름으로 인해 자원 해제가 누락된 경우에,
최종 보완 방법으로 사용할 수 있는데, 클라이언트가 하지 않은 자원 회수를
늦게라도 해주는 것이 아예 안 하는 것보다 낫다.

하지만 이런 안전망 역할을 사용할 때도, 앞선 단점들을 감안할 값어치가 있는지
심사숙고 해야한다.



## 2. 네이티브 피어와 연결된 객체의 회수

네이티브 피어란 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브
객체를 의미한다. 다시말해, 자바나 다른 고수준 프로그래밍 언어로 직접 만들어진
객체가 아닌, 주로 C나 C++과 같은 저수준 언어로 작성된 코드에 의해 생성되고
관리되는 객체를 의미한다.

이러한 객체는 Java의 관리 하에 있지 않기 때문에, GC의 대상이 되지 않는다.
또한, try-with-resources 구문의 경우 Java 내부의 자원을 자동으로 해제해 주는
기능이기 때문에 네이티브 피어 객체의 경우 Cleaner를 이용하여 직접 객체를 회수해야 한다.



---

Finalizer는 앞선 설명과 같이 단점들이 너무나도 많다.
이를 위해 Cleaner가 등장했지만, 기존 코드와의 호환성 문제로 사라지지 않고
점진적인 폐지가 이루어 지고 있다.

만약 Finalizer를 사용해야 할 일이 있다면, Java의 권장 사항대로
명시적 리소스 해제로 대체할 수 없는 지 생각해보고, 최종적으로 Cleaner로
대체해 사용하도록 하자.
