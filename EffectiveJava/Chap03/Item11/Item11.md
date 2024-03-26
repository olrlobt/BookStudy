# equals는 재정의하려거든 hashCode도 재정의하라

equlas를 재정의한 클래스 모두에서 hashCode도 재정의해야 한다. 그렇지 않으면 hashCode 일반 규약을 어기게 되어 해당 클래스의 인스턴스를
HashMap, HashSet 같은 컬렉션의 원소로 사용할 때 문제를 일으킬 것이다.

hashCode 일반 규약
- equals 비교에 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안 그 객체의 hashCode 메서드는
몇 번을 호출해도 일관되게 항상 같은 값을 반환해야 한다.
- equals가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.
- equals가 두 객체를 다르다고 판단했더라도, 두 객체의 hashCode가 서로 다른 값을 반환할 필요는 없다. 단,
다른 객체에 대해서는 다른 값을 반환해야 해시테이블의 성능이 좋아진다.

```java
Map<Phone, String> m = new HashMap<>();
m.put(new Phone(010,1111,2222), "손흥민");
m.get(new Phone(010,1111,2222));
```

위 코드에서 get을 실행하면, "손흥민"이 아닌 null을 반환한다.
이때 Phone 클래스는 hashCode를 재정의하지 않았기 때문에 논리적 동치인 두 객체가 서로 다른 해시코드를 반환하여
두번째 규약을 지키지 못한다.

이 문제는 간단히 Phone 객체에 hashCode 메서드만 작성해주면 해결된다.

```java
// 사용되서는 안 되는 구현
@Override
public int hashCode(){
	return 42; 
}
```
이 코드는 동치인 모든 객체에 대해 똑같은 해시코드를 반환해주어 적합해 보이지만, 그냥 모든 객체에 똑같은 값만 내 주므로 사용하면 안 된다.
사용하게 된다면 모든 객체가 해시테이블의 버킷 하나에 담겨 마치 연결리스트처럼 동작하고, 그 결과 평균 수행시간이 O(1)인 해시테이블이
O(N)으로 느려져 객체가 많아지면 도저히 쓸 수 없게 된다.

hashCode를 작성하는 간단한 요령은 다음과 같다.

1. int 변수 result를 선언 후 값 (field의 해시 코드)로 초기화한다. 
2. 해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업을 수행한다.
   - 기본 타입 필드라면, Type.hashCode(f)를 수행한다.
   - 참조 타입 필드이면서 equals 메서드가 이 필드의 equals를 재귀적으로 호출해 비교한다면,
     이 필드의 hashCode를 재귀적으로 호출한다.
   - 필드가 배열이라면, 핵심 원소 각각을 별도의 필드처럼 다룬다.
   - result = 31 * result + (field의 해시 코드);


예시 :
```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + intField;
    result = 31 * result + (booleanField ? 1 : 0);
    result = 31 * result + (objectField != null ? objectField.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(arrayField);
    return result;
}
```

이때 31을 곱하는 이유는 hashCode 메소드 설계에 있어서 오랜 기간 동안 검증된 관습이다.
1. 31은 소수이다.
2. 31을 사용한 연산은 비트 시프트와 뺄셈으로 최적화될 수 있다.
   (31을 곱하는 연산은 컴퓨터가 (i << 5) - i로 최적화할 수 있다.)


Objects 클래스는 임의의 개수만큼 객체를 받아 해시코드를 계산해주는 정적 메서드인 hash를 제공한다.
이는, 앞서 요령대로 구현한 코드와 비슷한 수준의 hashCode를 함수 한 줄로 작성할 수 있지만,
아쉽게도 속도는 더 느리다.

입력 인수를 담기 위한 배열이 만들어지고, 입력 중 기본 타입이 있다면 박싱과 언박싱도 거쳐야하기 때문이다.



클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기보다는 캐싱하는 방식을 고려해야한다.
이 타입의 객체가 주로 해시의 키로 사용될 것 같다면 인스턴스가 만들어질 때 해시코드를 계산해둬야한다.

해시코드를 지연 초기화하는 hashCode :
```java
private int hashCode;

@Override
public int hashCode(){
	int result = hashCode;
    if(result == 0){
		result = 31 * Short.hashCode(areaCode);
		result = 31 * result + Short.hashCode(prefix);
		result = 31 * result + Short.hashCode(lineNum);
		hashCode = result;
	}
	return result;
}
```


성능을 높인답시고 해시코드를 계산할 때 핵심 필드를 생략해서는 안 된다.
hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말자.
그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수도 있다.