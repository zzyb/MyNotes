package com.yber.java.thread;

public class BankTest {
    //账户个数
    public static final int NACCOUNTS = 100;
    //初始金额
    public static final double INITIAL_BALANCE = 1000;
    //账户最大单次转账金额
    public static final double MAX_AMOUNT = 1000;
    //休眠时间
    public static final int DELAY = 10;

  public static void main(String[] args) {
//      BankUnSafe bank = new BankUnSafe(NACCOUNTS, INITIAL_BALANCE);
      BankSafe bank = new BankSafe(NACCOUNTS, INITIAL_BALANCE);
      for(int i = 0;i< NACCOUNTS; i++){

          int fromAccount = i;
          Runnable r = () -> {
              try {
                  while(true){
                  int toAccount = (int)(bank.size() * Math.random());
                  double amount = MAX_AMOUNT * Math.random();
//                  bank.transfer(fromAccount,toAccount,amount);
                  bank.transfer2(fromAccount,toAccount,amount);
                      Thread.sleep((long) (DELAY * Math.random()));
                  }
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          };
          Thread thread = new Thread(r);
          thread.start();
      }
  }
}
