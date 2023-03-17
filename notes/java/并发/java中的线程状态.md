# JAVA的六种线程状态
https://www.bilibili.com/video/BV15b4y117RJ?p=63&vd_source=d20d7115806f183c57e33cf59b26ab69   1:10
### New - 新建
> 新建的线程，仅仅是个java对象，并没有和OS底层真正的线程关联，此时不会被cpu分配到。
> 当调用了线程的start方法时，变成了**可运行**状态
### Runnable - 可运行
> 与真正的OS线程关联起来，代码会被cpu分配到资源而执行。只有可运行态的代码才可以被OS执行，其他状态都不会被执行。
> 当线程运行完毕，会变成 **终结**状态
>
> 线程运行时，可能多个线程争取一把锁
> 当获取锁失败时，会变成**阻塞**状态
>
> 持有锁的线程(Runnable状态)，觉得时机不成熟，想等一会儿在执行代码，那么它可以调用wait()方法，进入**等待**状态，并且把锁交出来给其他线程用。
> 它也可以设置等待的时间：wait(long)，进入**等待（有时限）状态**
###### 调用wait()方法的前提是首先要获取该对象的锁（synchronize对象锁）
###### 调用wait()方法会释放锁，本线程进入等待队列等待被唤醒，被唤醒后不是立即恢复执行，而是进入阻塞队列，竞争锁
### Terminated - 终结
> 线程的生命周期走到了尽头，底层关联的真正线程和资源会被释放，且状态是不可逆的，不可以再变回可运行。
### Blocked - 阻塞
> 当持有锁的线程，释放锁的时候，会通知所有阻塞线程，然后这些阻塞线程进行下一轮的锁竞争。
> 若争抢锁成功，则由阻塞态变成**可运行**状态
### Waiting - 等待
> 当时机成熟了，由另一个线程调用 notify() 方法，唤醒等待的线程，等待状态的线程会进入阻塞队列，重新争抢锁
###### 其他线程调用该等待线程的 interrupted 方法, 导致 wait 抛出 InterruptedException 异常，也可以结束等待状态。
### Timed_Waiting - 有时间等待
> 当超过等待时间了，就会自己主动唤醒。
###### 用sleep也可以进入该状态，但与锁无关。

```
public class TestThreadState {
    static final Object LOCK = new Object();

    // 注释中的数字是看debug断点的顺序。
    public static void main(String[] args) {
        testNewRunnableTerminated();
        testBlocked();
        testWaiting();
    }

    private static void testWaiting() {
        Thread t = new Thread(() -> {
            System.out.println("before waiting"); // 1
            try {
                LOCK.wait(); // 3
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t");
        t.start();
        System.out.println("state: " + t.getState()); // 2
        synchronized (LOCK) {
            System.out.println("state: " + t.getState()); // 4
            LOCK.notify();
            System.out.println("state: " + t.getState()); // 6
        }
        System.out.println("state: " + t.getState()); // 7
    }

    private static void testBlocked() {
        Thread t = new Thread(() -> {
            System.out.println("before sync"); // 3
            synchronized (LOCK) {
                System.out.println("in sync"); // 4
            }
        }, "t");
        t.start();
        System.out.println("state: " + t.getState()); // 1
        synchronized (LOCK) {
            System.out.println("state: " + t.getState()); // 2
        }
        System.out.println("state: " + t.getState()); // 5
    }

    private static void testNewRunnableTerminated() {
        Thread t = new Thread(() -> {
            System.out.println("running..."); // 3
        }, "t");
        System.out.println("state: " + t.getState()); // 1
        t.start();
        System.out.println("state: " + t.getState()); // 2
        System.out.println("state: " + t.getState()); // 4
    }
}
```

# OS层面的五种状态
1. 新建
2. 就绪 - 表示可以分到cpu了，但是还没执行
3. 运行 - 表示分到cpu了，并且已经在执行
4. 阻塞：阻塞IO，BLOCKED，WAITING，TIMED_WAITING
5. 终结
##### JAVA中的RUNNABLE可运行状态，涵盖了就绪、运行和阻塞I/O
###### 阻塞I/O可以理解网卡和磁盘自己有芯片，可以写数据，不需要依赖cpu。
