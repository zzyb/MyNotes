package com.yber.java.algorithms.sort.custom;

public class Student implements Comparable<Student> {
    private final String name;
    private final int credit;

    public Student(String name, int credit) {
        this.name = name;
        this.credit = credit;
    }

    public String getName() {
        return name;
    }

    public int getCredit() {
        return credit;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", credit=" + credit +
                '}';
    }

    @Override
    public int compareTo(Student o) {
        if (this.credit > o.credit) {
            return 1;
        } else if (this.credit < o.credit) {
            return -1;
        } else {
            return this.name.length() - o.name.length();
        }
    }

    public static void main(String[] args) {
        Student tom = new Student("tom", 88);
        Student liliX = new Student("liliX", 88);
        Student dopa = new Student("dopa", 100);


        System.out.println(tom.toString());
        System.out.println(liliX.toString());
        System.out.println(dopa.toString());
        System.out.println(tom.compareTo(liliX));
        System.out.println(tom.compareTo(dopa));
        System.out.println(dopa.compareTo(liliX));
    }
}
