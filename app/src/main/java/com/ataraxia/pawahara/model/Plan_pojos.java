package com.ataraxia.pawahara.model;

public class Plan_pojos {

        private int plan_type;
        private String name;
        private int price;
        private int minuates;

        public Plan_pojos(int plan_type,String name, int price,int minuates) {
            this.plan_type=plan_type;
            this.name = name;
            this.price = price;
            this.minuates = minuates;
        }
    public int getplantype() {
        return plan_type;
    }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }
    public int getminuates() {
        return minuates;
    }



}
