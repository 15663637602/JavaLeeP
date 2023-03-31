# volatile 保证共享变量的可见性和有序性，不能保证原子性

## 线程安全考虑三方面
### 可见性
- 一个线程对共享变量修改，另一个线程能看到最新的结果 
- 可以阻止jvm对加了volatile的变量做热点优化。
- 下例中，线程1会在100毫秒后修改stop变量为true，看起来主线程的foo函数会被stop，但是实际没有停止。
```java
    static boolean stop = false;

public static void main(String[] args) {
    new Thread(() -> {
    try {
    Thread.sleep(100);
    } catch (InterruptedException e) {
    e.printStackTrace();
    }
    stop = true;
    System.out.println("modify stop to true...");
    }).start();

    new Thread(() -> {
    try {
    Thread.sleep(200);
    } catch (InterruptedException e) {
    throw new RuntimeException(e);
    }
    System.out.println(stop);
    }).start();

    foo();
    }

private static void foo() {
    int i = 0;
    while (!stop) {
    i++;
    }
    System.out.println("stopped...");
    }
```
#### 一种错误的解释：
1. 每个CPU对共享变量的操作，都是将 **内存** 中的共享变量复制一个副本到 **自己的高速缓存**中。
2. 然后cpu对自己高速缓存中的副本进行操作。
3. 如果没有正确同步，即使CPU0修改了某个变量，这个已修改的值还是只存在于副本中。
4. 此时CPU1要用它，会去内存中读取到修改前的值，而不是CPU0自己的高速缓存中的值。
#### 但实际上，第2个线程，是可以打印出stop为true的，说明其他线程也是可以看见stop真实的值的。
#### 实际上，是JIT对代码做了优化，它发现while(!stop){} 是热点代码（循环了太多次）。所以为了减少去内存中取值，就把stop变量直接改为false了。
#### 变成了 while(!false){i++;}
> 解决办法
> 1. 可以加vm参数 -Xint 禁用JIT优化。
> 2. 将线程1的sleep时间改成1毫秒，那么jvm就不会认为while循环是热点代码了。
> 3. 将变量用volatile优化。
### 有序性
- 一个线程内的代码按照编写顺序执行
#### volatile使用内存屏障，解决指令重排序，会为加了volatile的变量的读取操作后面加一个内存屏障，后面的指令不允许被排到它之前；在写操作的前面加一个内存屏障，前面的指令不允许被排到它后面。
#### 所以对于加了volatile的变量，要保证最后给它赋值（因为在它后面赋值的语句，可能会被排到它前面，而前面的不会排到后面，不会变），最先读取它的值（因为在它前面读取的指令不会被排到后面）。
> 最好还是用锁来解决问题，volatile的门槛比较高。
### 原子性
- 一个线程内多行代码以一个整体运行，期间不能有其他线程的代码插队
```java
static volatile int balance = 10;

// 下面这一行语句其实在字节码里是四个指令：获取static值，准备数字5，isub减法，put static值。
balance -= 5;
// 下面这一行语句在字节码里也是四个指令
balance += 5;
```
- 假设两个语句的四个指令中发生了交错，就会有问题。
