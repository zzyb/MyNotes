package com.yber.java.thread;

public class ThreadTest3 {
  public static void main(String[] args) {
      /**
       * 设置线程中断状态，并检查中断状态，如果监测到中断状态退出循环。
       */
    Runnable r1 = () -> {
            for(int i=0;i<=9 && !Thread.currentThread().isInterrupted();i++){
              System.out.println( i + " From r1.");
              if(i == 2){
                Thread.currentThread().interrupt();
              }
            }
    };
      new Thread(r1).setDaemon(true);
      new Thread(r1).start();

    Runnable r2 =
        () -> {
          try {
            for (int i = 0; i <= 9; i++) {
              System.out.println(i + " From r2.");
              //设置中断状态后，sleep方法会出现InterruptedException异常
                Thread.sleep(1000);
              if (i == 2) {
                Thread.currentThread().interrupt();
                //true 中断状态
                System.out.println(Thread.currentThread().isInterrupted());
              }
            }
          } catch (InterruptedException e) {
              System.out.println("sleep方法被中断");
              System.out.println("执行catch代码");
              System.out.println("出现中断异常");
              //false 中断状态被清除！！！
              System.out.println(Thread.currentThread().isInterrupted());
          }
        };
    //      new Thread(r2).start();

    Runnable r3 =
        () -> {
          try {
            for (int i = 0; i <= 9 && !Thread.currentThread().isInterrupted(); i++) {
              System.out.println(i + " From r3.");
              if(i==3){
                  //设置中断状态,会在循环检查处退出循环
                  Thread.currentThread().interrupt();
              }
            }
            //输出当前的线程中断状态。 输出为 true
              System.out.println(Thread.currentThread().isInterrupted());
            //*** 此时调用sleep ***
              Thread.sleep(2000);
          } catch (InterruptedException e) {
              //输出当前的线程中断状态。 输出为 catch : false 也就是说，中断状态被清除。
              System.out.print("catch : ");
              System.out.println(Thread.currentThread().isInterrupted());
          } finally {
              //输出当前的线程中断状态。 输出为 finally : false 也就是说，中断状态被清除。
              System.out.print("final : ");
              System.out.println(Thread.currentThread().isInterrupted());
          }
        };

//      new Thread(r3).start();


      /**
       * 在线程休眠sleep期间，调用线程中断，sleep会被一个interruptException异常中断。
       */
      Runnable r4 = () -> {
          try {
              for (int i = 0; i <= 10 && !Thread.currentThread().isInterrupted(); i++) {
                  System.out.println( i + " From r4.");
                  Thread.sleep(3000);
              }
          } catch (InterruptedException e) {
              System.out.println("执行catch代码");
          }
      };

//          Thread thread4 = new Thread(r4);
//          thread4.start();
//          //主线程休眠7秒让thread4运行
//          try {
//              Thread.sleep(7000);
//          } catch (InterruptedException e) {
//              e.printStackTrace();
//          }
//          //中断thread4
//          thread4.interrupt();

    Runnable r5 =
        () -> {
          int i = 0;
          while (!Thread.currentThread().isInterrupted()) {
            System.out.println("hello i am here " + i);
            i++;
            if (i == 10) {
              Thread.currentThread().interrupt();
                // 静态方法interrupted ,检测当前线程是否被中断，同时清除线程中断状态。
                // 意味着 在输出语句中调用后，循环处就检测不到了（因为被清除了）。
              System.out.println(Thread.interrupted());
            }
            if (i == 20) {
              Thread.currentThread().interrupt();
                //实例方法isInterrupted ,检测线程是否被中断，不会改变线程中断状态。
                //循环处，依然能够监测到。
              System.out.println(Thread.currentThread().isInterrupted());
            }
          }
        };
    //      new Thread(r5).start();

    Runnable r6 =
        () -> {
          //            a();
          //            b();
          try {
            c();
          } catch (InterruptedException e) {
            System.out.println("捕捉到c的InterruptException");
            System.out.println("可以设置中断，或者其他操作");
          }
        };
    /*
          Thread thread6 = new Thread(r6);
          thread6.start();

          //休眠主线程，让thread6运行3秒
          try {
              Thread.sleep(3000);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          //中断thread6
          thread6.interrupt();

     */






  }
  public static void a(){
      try {
          System.out.println("休眠前");
          Thread.sleep(30000);
          System.out.println("休眠前");
      } catch (InterruptedException e) {
          // 1- 忽略中断异常，同时调用者没有中断状态 不推荐！！！
           System.out.println(" catch: "+Thread.currentThread().isInterrupted());
      }
  }

    public static void b(){
        try {
            System.out.println("休眠前");
            Thread.sleep(30000);
            System.out.println("休眠前");
        } catch (InterruptedException e) {
            // 2- 在catch中设置中断状态，让调用者可以捕捉到。
            Thread.currentThread().interrupt();
            System.out.println(" catch: "+Thread.currentThread().isInterrupted());
        }
    }

    public static void c() throws InterruptedException {
        System.out.println("休眠前");
        Thread.sleep(30000);
        System.out.println("休眠前");
    }
}
