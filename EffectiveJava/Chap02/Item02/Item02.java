package Chap02.Item02;

public class Item02 {

    public static void main(String[] args) {

        Pizza pizza = new Pizza("thin");
        Pizza spicyPizza = new SpicyPizzaBuilder()
                .cheeseSauce(3)
                .hotSauce(1)
                .build();
    }


    public static class Pizza {
        private final String dough; // 필수
        private final String sauce;
        private final String topping;
        private final int hotSauce;
        private final int cheeseSauce;

        public Pizza(String dough) {
            this(dough, "토마토", "치즈", 0, 0);
        }

        public Pizza(String dough, String sauce) {
            this(dough, sauce, "치즈", 0, 0);
        }

        public Pizza(String dough, String sauce, String topping) {
            this(dough, sauce, topping, 0, 0);
        }

        public Pizza(String dough, String sauce, String topping, int hotSauce) {
            this(dough, sauce, topping, hotSauce, 0);
        }

        public Pizza(String dough, String sauce, String topping, int hotSauce, int cheeseSauce) {
            this.dough = dough;
            this.sauce = sauce;
            this.topping = topping;
            this.hotSauce = hotSauce;
            this.cheeseSauce = cheeseSauce;
        }

        // getter 메소드들
        // 예: public String getDough() { return dough; }
        // 나머지 getter 메소드들...
    }


    public static abstract class PizzaBuilder {
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


    public static class SpicyPizzaBuilder extends PizzaBuilder {
        @Override
        public Pizza build() {
            // SpicyPizzaBuilder는 핫소스를 기본으로 추가.
            hotSauce = Math.max(hotSauce, 5); // 최소 핫소스 레벨을 5로 설정
            return new Pizza(dough, sauce, topping, hotSauce, cheeseSauce);
        }
    }

    public static class CheesyPizzaBuilder extends PizzaBuilder {
        @Override
        public Pizza build() {
            // CheesyPizzaBuilder는 추가 치즈 소스를 기본으로 설정.
            cheeseSauce = Math.max(cheeseSauce, 3); // 최소 치즈 소스 양을 3으로 설정
            return new Pizza(dough, sauce, topping, hotSauce, cheeseSauce);
        }
    }
}
