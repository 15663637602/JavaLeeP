# RocksDB是什么
 >高效发挥存储硬件性能的嵌入式**KV**存储引擎
 >>嵌入式 是指 将底层的存储封装好了，可以在它的上面搭建数据库服务
 >>> redis，mysql，mongo底层存储引擎都可以用它，如pika，myrocks
 # RocksDB解决了什么问题
>解决***写多读少***的问题，相对而言读的需求没那么旺盛，但是它读的性能也很好。 
>>有名的Mysql InnoDB B+树，主要是解决***读多写少***的问题
 # RocksDB是怎么解决写多读少的问题的
 >LSM-Tree
 >LSM-Tree不是数据结构，是磁盘中组织数据的一种方式
 
 ## 举个例子：
 * 通常我们的服务程序，在机器上面写日志为什么很快？--- **追加写**
     > 写日志是用**追加写**的方式来将数据写入到磁盘的，利用了磁盘的顺序IO，所以很快
     > **硬件访问速度**：
     > 内存顺序IO（如对数组的连续访问）100ns [远快于] 内存随机IO（如对树结构的访问）[约等于]     磁盘顺序IO [远快于] 磁盘随机IO 10ms
 
 * 依旧举B+树的例子，B+树是**就地写**，innoDB中某个数据是存储在某个**页**中，那么在对该数据做修改的时候，就必须通过数据结构的手段，找到该页，才能对它做修改。
 * 那么RocksDB呢，它则是**追加写**，不论之前磁盘中是否存在数据，都追加写。那么避免不了的就会产生**冗余数据**
    > 例如这三次操作过后，会追加写三条数据，第三次put不会覆盖第一次put：
    > Put(k1,v1)，Put(k2,v2)，Put(k1,v11)
 * 顺序写是很快了，但是查询就相对慢了，如何对查询做优化呢？
    > 可以将数据以**有序**数据块的方式存储，查找有序数组可以将复杂度 O(n) -> O(logN)
 * 冗余数据该如何处理呢？
    > 通过压缩合并的方式(merge)，清除冗余数据。
    > 但是压缩合并操作会阻塞查询和插入操作，那么就需要将：
    > 1. **将写的文件 拆分成多个小文件**，那么发生阻塞也阻塞一部分数据
    > 2. **在内存中做缓存**，类似于innoDB的 buffer pool，内存IO很快，所以可以用就地写(运用数据结构)，达到阈值后再将内存中的数据dump到磁盘。
    > 3. 数据查询时，先查询内存缓存的数据，缓存的数据是不存在冗余数据的。
    > 4. 如果内存中的数据还没有dump到磁盘，但是掉电了怎么办：**写内存之前先写到WAL**（类似于redo log）
 # LSM-Tree
https://pic2.zhimg.com/80/v2-37576525d52091fd713bb13556c92861_720w.webp
* 为什么要设计两个memtable？
> 内存中的两个 memtable，一个是正常的接收写入请求的 memtable，一个是不可修改的immutable memtable
> 为了防止 内存数据落盘时 导致的阻塞，首先将要落盘的数据在内存中复制一份到Active Memtable，然后将自己修改为只读的Immutable Memtable进行落盘，数据修改操作在Active Memtable中进行。
* WAL log
> 类似于redo log的作用
* Level N分层
> SStable （Sorted String Table），有序字符串表，这个有序的字符串就是数据的 key。SStable 一共有七层（L0 到 L6）。下一层的总大小限制是上一层的 10 倍
> 首先，**level 0** 中 的任意一个 SSTable的内部，是没有重复数据的，因为它就是内存memtable就地写得来的(写的过程中自然会对 当前这次写 中重复的数据做处理)，但是不同的SSTable之间就是有重复数据了。
其次，**level 1 ~ level N**，是由上一层的多个SSTable和本层SSTable的数据 取 **交集**之后，再压缩合并成一个SSTable的（逐层compact）。Level1-LevelN中，每一层是没有冗余数据的，但是层与层之间是有冗余数据的。

### 写流程
1. 将写入操作顺序写入WAL日志中，接下来把数据写到 memtable中（采用SkipList结构实现）
2. MemTable达到一定大小后，将这个 memtable 切换为不可更改的 immutable memtable，并新开一个 memtable 接收新的写入请求
3. 这个 immutable memtable进行持久化到磁盘，成为L0 层的 SSTable 文件
4. 每一层的所有文件总大小是有限制的，每下一层大十倍。一旦某一层的总大小超过阈值了，就选择一个文件和下一层的文件合并。

注意： 所有下一层被影响到的文件都会参与 Compaction。合并之后，保证 L1 到 L6 层的每一层的数据都是在 key 上全局有序的。而 L0 层是可以有重叠的

### 写流程的约束
- 日志文件用于崩溃恢复
- 每个MemTable及SST文件中的Key都是有序的（字符顺序的升序）
- 日志文件中的Key是无序的
- **删除操作是标记删除，是插入操作的一种，真正的删除要在Compaction的时候实现**
- 无更新实现，记录更新通过插入一条新记录实现

### 读流程
先查memtable，再查 immutable memtable，然后查 L0 层的所有文件，最后一层一层往下查。
https://img-blog.csdnimg.cn/20210602171338512.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BlbnJpdmVy,size_16,color_FFFFFF,t_70

### SSM tree 总结
	1. 按冷热数据进行分层
	2. 在内存中就地写，在磁盘中追加写
	3. 分治思想：Level0是有冗余文件的，level1-levelN，每一层内部是没有冗余数据的
	4. 数据是有序的，level1 - leveln的SSTable文件间是有序的：key1~key10的数据放到第一个文件，key11~key30放到第二个文件，利用多路归并(由小的有序文件组合成一个大的有序文件)
