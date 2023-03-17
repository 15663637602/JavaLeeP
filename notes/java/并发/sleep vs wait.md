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
- wait()后会把锁释放掉，sleep并不会释放锁，但调用它的前提是当前线程占有锁(即代码要在synchronized块中)。


![watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIwMDA5MDE1,size_16,color_FFFFFF,t_70](https://img-blog.csdnimg.cn/2019050816141738.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIwMDA5MDE1,size_16,color_FFFFFF,t_70)
