package Chap02.Item01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 정적 팩터리 메서드 사용 예시
 */

public class Item01 {

    public static void main(String[] args) {
        Car electricCar = Car.createElectricCar("테슬라", "레드", 2022);
        Car petrolCar = Car.createPetrolCar("모닝", "블루", 2020, 50);
        Car hybridCar = Car.createHybridCar("프리우스", "화이트", 2021, 30);

        List<String> list = new ArrayList<>();
        List<String> strings = Collections.unmodifiableList(list); // 구체적인 구현을 숨긴다
    }

    public static class Car {
        private final String name;
        private final String color;
        private final int year;
        private final int oil;
        private final CarType type;

        private Car(String name, String color, int year, int oil, CarType type) {
            this.name = name;
            this.color = color;
            this.year = year;
            this.oil = oil;
            this.type = type;
        }

        public static Car createPetrolCar(String name, String color, int year, int oil) {
            return new Car(name, color, year, oil, CarType.PETROL);
        }

        public static Car createElectricCar(String name, String color, int year) {
            return new Car(name, color, year, 0, CarType.ELECTRIC);
        }

        public static Car createHybridCar(String name, String color, int year, int oil) {
            return new Car(name, color, year, oil, CarType.HYBRID);
        }

        // CarType 열거형 정의
        public enum CarType {
            ELECTRIC, PETROL, HYBRID
        }
    }
}


