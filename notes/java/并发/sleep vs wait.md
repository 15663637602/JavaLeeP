## 共同点
都是让当前线程放弃cpu使用权，进入阻塞状态

## 方法归属不同
- sleep(long)是Thread类的静态方法
- wait()、wait(long)是Object的成员方法，每个对象都有。每个对象都可以作为锁。

## 醒来实际不同
- sleep(long) 和wait(long)是等待超时后会唤醒
- wait()可以被notify()唤醒
- 如果得到了当前的线程对象，可以调用其interrupt()方法，打断唤醒，进入到InterruptException块中。

## 锁特性不同
- wait()的调用，必须先获取wait对象的锁，sleep无限制。
- wait()后会把锁释放掉，而sleep在synchronized代码块中执行，并不会释放锁
