package com.example.drools.model;

public class Person {
    private int age;
    private double salary;

    public Person() {
    }

    public Person(int age, double salary) {
        this.age = age;
        this.salary = salary;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Person{age=" + age + ", salary=" + salary + "}";
    }
}