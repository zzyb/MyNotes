package com.yber.java.thread;

import java.util.Arrays;

public class BankUnSafe {
  private final double[] accounts;

  public BankUnSafe(int n, double initialBanlance) {
    accounts = new double[n];
    Arrays.fill(accounts,initialBanlance);
  }

  public void transfer(int from, int to, double amount){
    if(accounts[from]<amount){
      return;
    } else {
      System.out.print(Thread.currentThread());
      accounts[from] -= amount;
      System.out.printf("%10.2f from %d to %d \t",amount,from,to);
      accounts[to] += amount;
      System.out.printf("Total Balance: %10.2f%n",getTotalBalance());
    }

  }

  private double getTotalBalance() {
    double sum =0;
    for(double value:accounts){
      sum += value;
    }
    return sum;
  }

  public int size(){
    return accounts.length;
  }
}
