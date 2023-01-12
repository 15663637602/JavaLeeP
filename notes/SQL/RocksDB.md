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
    > B+树读效率高而写效率差；log型文件操作写效率高而读效率差；因此要在排序和log型文件操作之间做个折中，于是就引入了log-structed merge tree LSM模型，通过名称可以看出LSM既有日志型的文件操作，提升写效率，又在每个sstable中排序，保证了查询效率。
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

### LSM tree 总结
	1. 按冷热数据进行分层
	2. 在内存中就地写，在磁盘中追加写
	3. 分治思想：Level0是有冗余文件的，level1-levelN，每一层内部是没有冗余数据的
	4. 数据是有序的，level1 - leveln的SSTable文件间是有序的：key1~key10的数据放到第一个文件，key11~key30放到第二个文件，利用多路归并(由小的有序文件组合成一个大的有序文件)

* 为什么要设计两个memtable？
> 内存中的两个 memtable，一个是正常的接收写入请求的 memtable，一个是不可修改的immutable memtable
> 为了防止 内存数据落盘时 导致的阻塞，首先将要落盘的数据在内存中复制一份到Active Memtable，然后将自己修改为只读的Immutable Memtable进行落盘，数据修改操作在Active Memtable中进行。
* WAL log
> 类似于redo log的作用
* Level N分层
> SStable （Sorted String Table），有序字符串表，这个有序的字符串就是数据的 key。SStable 一共有七层（L0 到 L6）。下一层的总大小限制是上一层的 10 倍
> 首先，**level 0** 中 的任意一个 SSTable的内部，是没有重复数据的，因为它就是内存memtable就地写得来的(写的过程中自然会对 当前这次写 中重复的数据做处理)，但是不同的SSTable之间就是有重复数据了。
其次，**level 1 ~ level N**，是由上一层的多个SSTable和本层SSTable的数据 取 **交集**之后，再压缩合并成一个SSTable的（逐层compact）。Level1-LevelN中，每一层是没有冗余数据的，但是层与层之间是有冗余数据的。

## 写流程
1. 将写入操作顺序写入WAL日志中（为了性能，也可以不写入 WAL，但这样就可能面临崩溃丢失数据的风险），接下来把数据写到 memtable中（通常是一个能支持并发写入的 skiplist，但 RocksDB 同样也支持多种不同的 skiplist，用户可以根据实际的业务场景进行选择。）
2. MemTable达到一定大小后，将这个 memtable 切换为不可更改的 immutable memtable，并新开一个 memtable 接收新的写入请求，RocksDB 在后台会通过一个 flush 线程将这个 immutable Memtable flush 到磁盘
3. 这个 immutable memtable进行持久化到磁盘，成为L0 层的 SSTable 文件
4. 当 Level 0 层的 SST 文件个数超过阈值之后，就会通过 Compaction 策略将其放到 Level 1 层，以此类推。
5. 每一层的所有文件总大小是有限制的，每下一层大十倍。一旦某一层的总大小超过阈值了，就选择一个文件和下一层的文件合并。

注意： 所有下一层被影响到的文件都会参与 Compaction。合并之后，保证 L1 到 L6 层的每一层的数据都是在 key 上全局有序的。而 L0 层是可以有重叠的
https://user-images.githubusercontent.com/87458342/132664994-8281764e-2ce1-4808-9fd8-6e793c2dc77a.png

### 写流程的约束
- 日志文件用于崩溃恢复
- 每个MemTable及SST文件中的Key都是有序的（字符顺序的升序）
- 日志文件中的Key是无序的
- **删除操作是标记删除，是插入操作的一种，真正的删除要在Compaction的时候实现**
- 无更新实现，记录更新通过插入一条新记录实现

## 读流程
先查memtable，再查 immutable memtable，然后查 L0 层的所有文件，最后一层一层往下查。
https://img-blog.csdnimg.cn/20210602171338512.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BlbnJpdmVy,size_16,color_FFFFFF,t_70

- 在 memtable->immutable_memtable->sstFiles 这些环节中，读取 SST Files 是最复杂的。
- 在 RocksDB 中，默认的 SST 是 BlockBasedTable，从字面意思上看，我们可以知道其核心是“Block”，在 kv store 中，数据的最小单位就是 kv，一个 kv，小则几个字节，大则数十数百KB，更多的是在几十到几百字节这个范围。而 Block，一般是 4K, 8K, 16K ...，一个 Block 会包含数十到数百个 kv。
- 但是，由于Block 的存储形态，一般是压缩的，这样，读取 SST File 的时候，我们只能按 Block 读，因为只有解压后才能定位具体的每条 kv，但是显然我们不能每次读取都去解压一个 Block，这就需要 BlockCache……
- 所以主要有两条路
- - **从内存读**:MemTable->Immutable MemTable
- - **从持久化设备读**:首先通过 table cache获取到文件的元数据如布隆过滤器以及数据块索引, 然后读取 block cache, block cache中缓存了SST的数据块,如果命中那就直接读取成功,否则便需要从SST中读取数据块并插入到block cache
- RocksDB 帮我们处理了这其中所有肮脏的细节，而从使用者来看，一切都很简洁


## RocksDB如何提供 并发 读写能力
通用的有几种数据结构--- 红黑树 OlogN，跳表OlogN，B+树 O(m*logN)
假设用红黑树，那么对数据做增删操作的话，就涉及到对树重新着色或者旋转，那么在多线程的环境下，就需要对整个树结构加锁，锁的力度过大。

**所以跳表更适合多线程并发读写**

实际代码中还用了很多办法，为了能够在多线程环境钟不出错的情况下，尽可能的提升效率
可以看下inlineskiplist.h中对max_height_、next_的atomic定义
max_height_.store用的memory_order_relaxed内存模型
内存模型relaxed和release的区别 -->
如果用release：那么不允许指令重排，那么上面的for循环代码会被优化到下面，那么插入17会导致17元素的位置会立即生成一个有高度的索引节点，那么可以提升查询效率
如果用relaxed：for循环代码会被优化到下面去，导致不会立马多出一个索引节点，但是数据是正确的，并发能力得到了提升

## 其他术语
### WAL（Write Ahead Log）
顾名思义，就是在实际操作数据前先写日志，便于恢复。WAL在很多数据库中都存在。

RocksDB中的每个更新操作都会写到两个地方：

- 一个内存数据结构，名为memtable(后面会被刷盘到SST文件)
- 写到磁盘上的WAL日志。在出现崩溃的时候，WAL日志可以用于完整的恢复memtable中的数据，以保证数据库能恢复到原来的状态。在默认配置的情况下，RocksDB通过在每次写操作后对WAL调用flush来保证一致性。

### Memtable
MemTable是一个内存数据结构，他保存了落盘到SST文件前的数据。他同时服务于读和写——新的写入总是将数据插入到memtable，读取在查询SST文件前总是要查询memtable，因为memtable里面的数据总是更新的。一旦一个memtable被写满，他会变成不可修改的，并被一个新的memtable替换。一个后台线程会把这个memtable的内容落盘到一个SST文件，然后这个memtable就可以被销毁了。

默认的memtable实现是基于skiplist的。除了默认的memtable实现，用户可以使用其他memtable实现，例如HashLinkList，HashSkipList或者Vector，以加快查询速度。
https://user-images.githubusercontent.com/87458342/132665555-982042b8-c06b-4093-b52a-b7911e43c2d9.png
影响memtable的最重要的几个选项是：

memtable_factory: memtable对象的工厂。通过声明一个工厂对象，用户可以改变底层memtable的实现，并提供事先声明的选项。
write_buffer_size：一个memtable的大小。
db_write_buffer_size：多个列族的memtable的大小总和。这可以用来管理memtable使用的总内存数。
write_buffer_manager：除了声明memtable的总大小，用户还可以提供他们自己的写缓冲区管理器，用来控制总体的memtable使用量。这个选项会覆盖db_write_buffer_size。
max_write_buffer_number：内存中可以拥有刷盘到SST文件前的最大memtable数。
这些都可以在RocksDB的Option对象中配置。

### Flush
有三种场景会导致memtable落盘被触发：

Memtable的大小在一次写入后超过write_buffer_size。
所有列族中的memtable大小超过db_write_buffer_size了，或者write_buffer_manager要求落盘。在这种场景，最大的memtable会被落盘。
WAL文件的总大小超过max_total_wal_size。在这个场景，有着最老数据的memtable会被落盘，这样才允许携带有跟这个memtable相关数据的WAL文件被删除。
就结果来说，memtable可能还没写满就落盘了。这是为什么生成的SST文件小于对应的memtable大小。压缩是另一个导致SST文件变小的原因，因为memtable里的数据是没有压缩的。

### Compaction
LSM-Tree 能将离散的随机写请求都转换成批量的顺序写请求（WAL + Compaction），以此提高写性能。但也带来了一些问题：

- 读放大（Read Amplification）。LSM-Tree 的读操作需要从新到旧（从上到下）一层一层查找，直到找到想要的数据。这个过程可能需要不止一次 I/O。特别是 range query 的情况，影响很明显。
- 空间放大（Space Amplification）。因为所有的写入都是顺序写（append-only）的，不是 in-place update ，所以过期数据不会马上被清理掉。
- 写放大。实际写入 HDD/SSD 的数据大小和程序要求写入数据大小之比。正常情况下，HDD/SSD 观察到的写入数据多于上层程序写入的数据。
RocksDB 和 LevelDB **通过后台的 compaction 来减少读放大（减少 SST 文件数量）和空间放大（清理过期数据）**，但也**因此带来了写放大（Write Amplification）的问题**。

**写放大、读放大、空间放大，三者就像 CAP 定理一样**，需要做好权衡和取舍。

压缩算法有很多种，RocksDB也支持很多种，这里我们看两个经典的压缩算法及区别
Tiered Compaction vs Leveled Compaction
https://user-images.githubusercontent.com/87458342/132666300-89414a96-987b-4d59-94cb-773107499560.png
上图是两种 compaction 的区别，当 Level 0 刷到 Level 1，让 Level 1 的 SST 文件达到设定的阈值，就需要进行 compaction。

**Tiered：** 我们会将所有的 Level 1 的文件 merge 成一个 Level 2 SST 放在 Level 2。也就是说，compaction 其实就是**将上层的所有小的 SST merge 成下层一个更大的 SST ** 的过程。

**Leveled： 不同 Level 里面的 SST 大小都是一致的，Level 1 里面的 SST 会跟 Level 2 一起进行 merge 操作，最终在 Level 2 形成一个有序的 SST，而各个 SST 不会重叠。**

------------
RocksDB替换Redis：Pika
解决重启加载数据慢的问题。redis是内存数据库，比如数据到50G以上，redis重启从磁盘加载数据到内存中就需要大量的时间。
RocksDB替换innoDB：MyRocks
解决存储效率的问题。举例子，mysql存一份数据是150G，但是用rocksDB存储相同的数据占用空间只有50G，因为磁盘顺序读写，填充率会很高，而且B+是为了追求读效率而牺牲了写效率，空间换时间。
TiDB用rocksDB做持久化，增加节点就不需要像mysql做分库分表那样费力了。
