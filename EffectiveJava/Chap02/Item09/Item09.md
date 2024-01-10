
# try-finally 보다는 try-with-resources를 사용하라


try-finally와 try-with-resources는 자바에서 자원을 관리하기 
위한 두 가지 메커니즘이다. 
각각의 특징을 이해하면, 왜 try-with-resources를 try-finally보다 
선호하는지 명확해진다.


## try-finally
#### 동작 방식: 
try 블록에서 자원을 사용하고, finally 블록에서 자원을 해제한다.
#### 문제점:
코드가 복잡하고 길어질 수 있다. 각 자원 해제에 대한 명시적인 close() 호출이 필요하다.

예외 처리가 복잡해지고, try 블록과 finally 블록 양쪽에서 예외가 발생할 수 있으며, 이를 적절히 처리하는 것이 복잡하다.
자원 누수의 위험이 있고, 만약 finally 블록 내에서 예외가 발생하거나 close() 메서드가 제대로 호출되지 않으면 자원이 제대로 해제되지 않을 수 있다.


#### 예제:

InputStream과 OutputStream 두 개의 자원을 사용하는 예제를 보자.

```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FiteOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}
```

위 예제의 경우, InputStream과 OutputStream이 
각각 별도의 finally 블록에서 닫힌다. 만약 try 블록과 finally 
블록 양쪽에서 예외가 발생한다면, finally 블록에서 발생한 예외가 
우선적으로 처리되고, try 블록에서 발생한 예외는 무시된다.

즉, 일부 중요한 예외 정보가 손실될 수 있다는 것을 의미한다.

<br><br>


또한, 아래처럼 DB 연결과 파일 읽기를 같이하는 예제를 보자.

```java
public class DoubleExceptionHandling {

    public static void main(String[] args) {
        Connection conn = null;
        BufferedReader reader = null;

        try {// 데이터베이스 연결 시도 // 파일 읽기 시도
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "username", "password");
            reader = new BufferedReader(new FileReader("myFile.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (SQLException sqlEx) {
            System.out.println("Database error occurred: " + sqlEx.getMessage());
        } catch (IOException ioEx) {
            System.out.println("File read error occurred: " + ioEx.getMessage());
        } finally {
            try {
                if (conn != null) { 
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
            }

            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing file reader: " + e.getMessage());
            }
        }
    }
}
```
위 예제도 두 가지 리소스를 try-finally를 이용하여 finally 블록에서
자원을 close()해 주고 있고,
자원 해제 과정에서 발생할 수 있는 예외 역시 try-catch문을 이용하여
예외를 처리해 주고 있다.

이처럼 try-finally문을 사용하게되면, 코드가 너무 복잡해지고 한 눈에 알아보기 어렵다는 단점이 있다.

<br><br>

---

## try-with-resources
#### 동작 방식:
AutoCloseable 또는 Closeable 인터페이스를 구현한 객체를 자동으로 닫아준다. 이 구문은 Java 7부터 사용할 수 있다.

#### 사용 예제:

앞서, try-finally 구문에서 봤던 예제들을 try-with-resources문으로 변경해보자.

먼저,
InputStream과 OutputStream 두 리소스를 사용했던 예제를 try-with-resources구문으로 변경해 보았다.

```java
static void copy(String src, String dst) throws IOException {
    try (InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst)) {
        
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0) {
            out.write(buf, 0, n);
        }
    }
}
```
try-with-resources문은 try 블록의 () 괄호 안에 리소스 선언하는 방식으로 사용한다.
이 방식은 try 블록의 실행이 끝나면 JVM에서 자동으로 리소스를 닫아준다.
이는 정상 실행 완료 시 뿐만 아니라 예외가 발생했을 때에도 마찬가지로 동작한다.

try-with-resources문을 사용하면, resource의 예외와 자원 해제 중의 예외를
모두 처리 할 수 있고, 주요 예외가 처리되는 동안 처리되지 않은 예외들은
억제된 예외(Suppressed Exceptions)로 처리해준다.

이런 억제된 예외들은 주요 예외 객체에 대해 `getSuppressed()`메서드를 호출하여
발생한 예외 정보를 얻을 수 있게 해 준다.

<br><br>

다음으로, DB 연결과 파일 읽기를 했던 예제를 보자.

```java
public class DoubleExceptionHandling {

    public static void main(String[] args) {
        // try-with-resources 구문을 사용하여 자동으로 자원을 닫습니다
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "username", "password");
             BufferedReader reader = new BufferedReader(new FileReader("myFile.txt"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (SQLException sqlEx) {
            System.out.println("Database error occurred: " + sqlEx.getMessage());
        } catch (IOException ioEx) {
            System.out.println("File read error occurred: " + ioEx.getMessage());
        }
        // 별도의 finally 블록이 필요 없다
    }
}
```

try-with-resources문을 사용하여 말도 안 되게 간결해진 것을 알 수 있다.
이렇게 간결해 졌음에도 모든 예외를 추적할 수 있도록 도와주고,
안전성에서도 문제가 없다.





#### 장점:
- 코드가 간결해져서 가독성과 유지보수 면에서 좋다.
- 자원 해제를 위한 별도의 close() 호출이 필요 없으며, 자원의 선언과 초기화가 try 구문 내에서 이루어진다.
- 예외 처리가 더 쉬워지고, try 블록에서 발생한 예외가 finally 블록에서 발생한 예외에 의해 가려지지 않는다.
- 자원 누수의 위험이 줄어든다. try 블록이 정상적으로 종료되거나 예외가 발생하더라도 close() 메서드가 자동으로 호출된다.



