string类型是redis最简单的存储类型，string类型的最大空间不能大于512M
#### 其value是字符串，根据字符串的格式不同，可以分为3类：
- string：普通字符串
- int：整数类型，可做自增、自减操作
- float：浮点类型，可做自增、自减操作

### 不管是那种格式，底层都是字节数组形式存储，只不过编码方式不同。

字符串对象底层数据结构实现为简单动态字符串（SDS）和直接存储，但其编码方式可以是int、raw或者embstr，区别在于内存结构的不同。
* 1. int编码
字符串保存的是**整数值**，并**且这个值可以用long类型来表示**，那么其就会**直接保存在redisObject的ptr属性里**，并将**编码设置为int**，如图：<br/>
![image](https://user-images.githubusercontent.com/87458342/132519704-605f9566-20c2-45c4-b5a3-23faad04d111.png)

* 2. raw编码
字符串保存的**大于32字节的字符串**值，则**使用简单动态字符串（SDS）结构**，并将**编码设置为raw**，此时内存结构与SDS结构一致，**内存分配次数为两次**，创建redisObject对象和sdshdr结构，如图：<br/>
![image](https://user-images.githubusercontent.com/87458342/132519802-72780b33-00a3-440a-a036-169675c1a079.png)

* 3. embstr编码
字符串保存的**小于等于32字节**的字符串值，使用的也是简单的**动态字符串（SDS结构），但是内存结构做了优化**，用于保存顿消的字符串；**内存分配也只需要一次就可完成，分配一块连续的空间即可**，如图：<br/>
![image](https://user-images.githubusercontent.com/87458342/132519975-152ef3c0-f2e8-4bdb-94f0-15d80070b8d1.png)

* 字符串对象总结：
   * 在Redis中，存储long、double类型的**浮点数是先转换为字符串再进行存储的**。
   * **raw与embstr编码效果是相同的，不同在于内存分配与释放，raw两次，embstr一次**。
   * **embstr内存块连续**，能更好的利用缓存带来的优势
   * int编码和embstr编码如果做追加字符串等操作，满足条件下会被转换为raw编码；**embstr编码的对象是只读的，一旦修改会先转码到raw**。
 
* 应用场景：
   1. **访问量统计**：每次访问博客和文章使用 INCR 命令进行递增。
   2. 一般做一些复杂的技术功能的缓存。


### 相关commands
- SET：添加或修改已经存在的一个**string类型**的kv
- GET：根据key获取string类型的value
- MSET：批量添加多个string类型kv
- MGET：根据多个key获取多个string类型的v
- INCR：让一个整型的key自增1，对于不存在的key则等效于set key 1
- INCRBY：让一个整型的key自增并指定步长，对于不存在的key则等效于set key 步长值
- INCRBYFLOAT：让一个浮点类型数字自增并指定步长
- SETNX：添加一个kv，前提是这个key不存在，否则不执行
- SETEX：添加一个kv，并指定有效期


层级存储，将key以：分割
