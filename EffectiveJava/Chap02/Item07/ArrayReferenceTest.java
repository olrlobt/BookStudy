package Chap02.Item07;

public class ArrayReferenceTest {
    public static void main(String[] args) {
        String[] array = new String[3];
        array[0] = "Java";
        array[1] = "Python";
        array[2] = "C++";

        // 배열에서 객체를 제거하는 것처럼 보임
        int removeIndex = 2; // "C++" 제거
        array[removeIndex] = null; // 참조 해제

        // 배열 내용 출력
        for (int i = 0; i < array.length; i++) {
            System.out.println("Array[" + i + "]: " + array[i]);
        }
    }
}
