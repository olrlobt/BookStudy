package Chap02.Item07;

import java.util.ArrayList;
import java.util.Stack;

public class Item07 {

    public static void main(String[] args) {
        Stack<String> stack = new Stack<>();

        // 스택에 문자열 객체를 추가
        stack.push("Java");
        stack.push("Python");
        stack.push("C++");

        // 스택에서 객체를 제거하고 참조 유지
        String usedObject = stack.pop(); // "C++" 제거하고 참조

        // 스택의 현재 상태 출력
        System.out.println("Stack after pop: " + stack);

        // 사용한 객체 참조 출력
        System.out.println("Used object: " + usedObject);
    }
}
