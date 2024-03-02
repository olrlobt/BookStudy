# equals는 일반 규약을 지켜 재정의하라

equals 메서드는 재정의 하기 쉬워 보이지만 자칫하면 끔찍한 결과를 초래한다.
다음 열거한 상황 중 하나에 해당한다면 재정의하지 않는 것이 최선이다.

1. 각 인스턴스가 본질적으로 고유하다.
2. 인스턴스의 '논리적 동치성'을 검사할 일이 없다.
3. 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.
4. 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.

그렇다면, equlas 메서드를 재정의해야 할 때는 언제일까?
바로, 객체 식별성이 아니라 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 때다.
주로 값 클래스들이 여기 해당한다. 값 클래스인 String, Integer를 사용하는 개발자들은 객체가 같은지가 아니라, 값이 같은지를 알고 싶어 할 것이다.

하지만, 값 클래스라 해도, 값이 같은 인스턴스가 둘 이상 만들어지지 않음이 보장되면 equals를 재정의하지 않아도 된다.


## equals 재정의


equals 메서드를 재정의할 때는 반드시 일반 규약을 따라야 한다.

equals 메서드는 동치관계를 구현하며, 다음을 만족해야한다.
### 1. 반사성 : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true이다.

이 경우는 일부러 어기는 경우가 아니라면 만족시키지 못하기가 더 어렵다.

### 2. 대칭성 : null이 아닌 모든 참조 값 x,y에 대해, x.equals(y)가 true이면 y.equals(x) 도 true이다.

대소문자를 구별하지 않는 문자열을 구현한 다음 equals를 다음과 같이 작성한다면,

```java

@Override
public boolean equals(Object o){
	if(o instanceof CaseInsensitiveString)
		return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
	if(o instanceof String)
		return s.equalsIgnoreCase(((String) o));
	return false;
}

CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
String s = "polish";
```

`cis.equals(s)`는 true를 반환한다.
하지만, `s.equals(cis)`는 true를 반환할 수도, false를 반환할 수도 있다.



### 3. 추이성 : null이 아닌 모든 참조 값 x,y,z에 대해, x.equals(y)가 true이면 y.equals(z) 도 true이면, x.equals(z)도 true이다.


```java
public class Point {
	private final int x;
	private final int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Point)) return false;
		Point p = (Point) o;
		return p.x == x && p.y == y;
	}
}

public class ColorPoint extends Point {
	private final String color;

	public ColorPoint(int x, int y, String color) {
		super(x, y);
		this.color = color;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ColorPoint)) return false;
		return super.equals(o) && ((ColorPoint) o).color.equals(color);
	}
}
```

Point p = new Point(1,2);\
ColorPoint cp = new ColorPoint(1, 2, Color.RED);\
이 경우, p.equals(cp)는 true를, cp.equals(p)는 false를 반환한다.

```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof Point)) return false;
    // 색상 비교를 무시하고, Point의 equals 로직만 사용
    return super.equals(o);
}
```
Point p = new Point(1, 1);\
ColorPoint cp1 = new ColorPoint(1, 1, "red");\
ColorPoint cp2 = new ColorPoint(1, 1, "blue");

p.equals(cp1) → true \
cp1.equals(cp2) → true (색상을 비교하지 않으므로 좌표만 같으면 같은 객체로 간주)

하지만 p.equals(cp2)는 당연히 true이지만, 
이는 cp1과 cp2가 서로 다른 색상임에도 불구하고 둘 다 p와 동등하다고 판단하는 것으로,
추이성을 위반한다.

java의 Date를 나노초까지 확장한 Timestamp에는 위와 같은 추이성 문제가 있으며,
Timestamp API설명에도 명시되어 있다.



### 4. 일관성 : null이 아닌 모든 참조 값 x,y에 대해, x.equals(y)를 반복해서 호출하면 항상 true나 false를 반환한다.

클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안 된다.



```java
URL url1 = new URL("http://example.com");
URL url2 = new URL("http://example.com");

boolean isEqual = url1.equals(url2); // 이론적으로는 true이지만, 내부 DNS 조회 결과에 따라 달라질 수 있음
```
java의 URL.equals() 메서드는 실제로 호스트의 IP 주소로의 해석을 포함하여 
두 URL이 같은 자원을 가리키는지를 결정하기 위해 내부적으로 DNS 조회를 할 수 있다.

항상 DNS 조회를 하는 것은 아니지만, 이 과정이 equals 메서드의 결과에 직접적인 영향을 미치기도 한다.

이는 일관성 문제를 일으키며, 따라해서는 안 되는 구현 방법이다.



### 5. null-아님: null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false이다.

동치성 검사에서 equals는 건네받은 객체를 적절히 형변환을 한 후 instanceof로 타입 검사를 진행한다.
이 때, 묵시적인 Null 검사가 이루어지기 때문에 따로 Null 검사를 하지 않아도 된다. 


<br><br>


앞선 내용들로 양질의 equals 메서드를 구현하는 방법을 단계별로 정리하면 다음과 같다.

1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
2. instanceof 연산자로 입력이 올바른 타입인지 확인한다.
3. 입력을 올바른 타입으로 형변환한다.
4. 입력 객체와 자기 자신의 대응되는 '핵심'필드들이 모두 일치하는지 하나씩 검사한다.

float과 double을 제외한 기본 타입 필드는 == 연산자로 비교하고,
참조 타입 필드는 각각의 equals 메서드로, float, double 필드는 Float.NaN, -0.0f, 특수한 부동소수 값
등을 다루어야 하기 때문에 각각 정적 메서드인 compare을 이용한다.



<br>

때로는 null도 정상 값으로 취급하는 참조 타입 필드가 있다. 이는 Object.equals()로 비교해 NPE를 방지하자.
또한, 어떤 필드를 먼저 비교하느냐가 equals의 성능을 좌우하기도 한다.



