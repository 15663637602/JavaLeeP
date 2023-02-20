# RocksDB是什么
 >一个单机的key-value数据库，是facebook基于levelDB的一个fork做了一些优化
 >可以高效发挥存储硬件性能的嵌入式**KV**存储引擎
 >>嵌入式 是指 将底层的存储封装好了，可以在它的上面搭建数据库服务
 >>> redis，mysql，mongo底层存储引擎都可以用它，如pika，myrocks
 # RocksDB解决了什么问题
>解决**写多读少**的问题，相对而言读的需求没那么旺盛，但是它读的性能也很好。
>>有名的Mysql InnoDB B+树，主要是解决**读多写少**的问题
>>Mysql存在空间放大的问题
 # RocksDB是怎么解决写多读少的问题的
 >LSM-Tree
 >LSM-Tree不是数据结构，是磁盘中组织数据的一种方式
 
 ## 举个例子：
 * 通常我们的服务程序，在机器上面写日志为什么很快？--- **追加写**
     > 写日志是用**追加写**的方式来将数据写入到磁盘的，利用了磁盘的顺序IO，所以很快
     > **硬件访问速度**：
     > 内存顺序IO（如对数组的连续访问）100ns [远快于] 内存随机IO（如对树结构的访问）[约等于]     磁盘顺序IO [远快于] 磁盘随机IO 10ms
 
 * 依旧举B+树的例子，B+树是**就地写 or 随机写**，innoDB中某个数据是存储在某个**页**中，那么在对该数据做修改的时候，就必须通过数据结构的手段，找到该页，才能对它做修改。
 * 那么RocksDB呢，它则是**追加写**，不论之前磁盘中是否存在数据，都追加写。那么避免不了的就会产生**冗余数据**
    > 例如这三次操作过后，会追加写三条数据，第三次put不会覆盖第一次put：
    > Put(k1,v1)，Put(k2,v2)，Put(k1,v11)
 * 顺序写是很快了，但是查询就相对慢了，如何对查询做优化呢？
    > 可以将数据以**有序**数据块的方式存储，查找有序数组可以将复杂度 O(n) -> O(logN)
    > 追加写 又是怎么做到以有序数据块的方式存储数据呢？ -- LSM树的SST结构
 * 冗余数据该如何处理呢？
    > 通过压缩合并的方式(compaction)，清除冗余数据。
    > 但是压缩合并操作会阻塞查询和插入操作，那么就需要将：
    > 1. **将写的文件 拆分成多个小文件**，那么发生阻塞也阻塞一部分数据
    > 2. **在内存中做缓存**，类似于innoDB的 buffer pool，内存IO很快，所以可以用就地写(运用数据结构)，达到阈值后再将内存中的数据dump到磁盘。
    > 3. 数据查询时，先查询内存缓存的数据，缓存的数据是不存在冗余数据的。
    > 4. 如果内存中的数据还没有dump到磁盘，但是掉电了怎么办：**写内存之前先写到WAL**（类似于redo log）
 # LSM-Tree
![image.png](http://image.huawei.com/tiny-lts/v1/images/cdf2f478314fe13c3298ce00b4a0a41a_973x486.png@900-0-90-f.png)
### LSM tree 总结
	1. 按冷热数据进行分层
	2. 在内存中就地写，在磁盘中追加写
	3. 分治思想：Level0是有冗余文件的，level1-levelN，每一层内部是没有冗余数据的
	4. 数据是有序的，level1 - leveln的SSTable文件间是有序的：key1~key10的数据放到第一个文件，key11~key30放到第二个文件，利用多路归并(由小的有序文件组合成一个大的有序文件)

* 为什么要设计两个memtable？
> MemTable是一个内存数据结构，他保存了落盘到SST文件前的数据。他同时服务于读和写——新的写入总是将数据插入到memtable，读取在查询SST文件前总是要查询memtable，因为memtable里面的数据总是更新的。一旦一个memtable被写满，他会变成不可修改的，并被一个新的memtable替换。一个后台线程会把这个memtable的内容落盘到一个SST文件，然后这个memtable就可以被销毁了。
>
> 内存中的两个 memtable，一个是正常的接收写入请求的 memtable，一个是不可修改的immutable memtable
>
> 为了防止 内存数据落盘时 导致的阻塞，首先将要落盘的数据在内存中复制一份到Active Memtable，然后将自己修改为只读的Immutable Memtable进行落盘，数据修改操作在Active Memtable中进行。
>
> 默认的memtable实现是基于skiplist的。除了默认的memtable实现，用户可以使用其他memtable实现，例如HashLinkList，HashSkipList或者Vector，以加快查询速度。
![image.png](https://user-images.githubusercontent.com/87458342/132665555-982042b8-c06b-4093-b52a-b7911e43c2d9.png)
* WAL log
![image.png](http://image.huawei.com/tiny-lts/v1/images/338dba9d633c864c58c4646354269d83_717x356.png@900-0-90-f.png)
> 类似于redo log的作用
> 一个memtable会对应一个wal，当memtable被flush到sst中后，对应的wal也会被froze然后删掉
> 在出现崩溃的时候，WAL日志可以用于完整的恢复memtable中的数据，以保证数据库能恢复到原来的状态。在默认配置的情况下，RocksDB通过在每次写操作后对WAL调用flush来保证一致性。
* Level N分层
> SStable （Sorted String Table），有序字符串表，这个有序的字符串就是数据的 key。SStable 一共有七层（L0 到 L6）。下一层的总大小限制是上一层的 10 倍
> 首先，**level 0** 中 的任意一个 SSTable的内部，是没有重复数据的，但是不同的SSTable之间就是有重复数据了。因为它就是内存memtable就地写得来的(写的过程中自然会对 当前这次写 中重复的数据做处理)。因此查询时，在 0 层中要对所有 SST 文件逐个查找。
其次，**level 1 ~ level N**，是由上一层的多个SSTable和本层SSTable的数据 取 **交集**之后，再压缩合并成一个SSTable的（逐层compact），挤出“水分”（重复的 key）然后分裂成多个 SST 文件。因此在 1 层之下，所有的 SST 文件键范围各不相交，且有序。这种组织方式，可以让我们在层内查找时，进行二分查找（O(log(N)) 复杂度），而非线性查找（O(N) 复杂度）。
> Level1-LevelN中，每一层是没有冗余数据的，但是层与层之间是有冗余数据的。
# RocksDB
![image.png](http://image.huawei.com/tiny-lts/v1/images/cdce2a7be3c0647168640a1a370aa1a7_773x369.png@900-0-90-f.png)
### rocksDB在磁盘中生成的文件
![image.png](https://user-images.githubusercontent.com/87458342/132679048-d2495d9b-9e94-413f-9e78-107a3d79978a.png)
- xxx.log：wal日志文件
- xxx.sst：数据文件
- CURRENT：是一个特殊的文件，用于声明最新的manifest日志文件
- IDENTITY：id
- LOCK：无内容，open时创建，表示一个db在一个进程中只能被open一次，多线程共用此实例
- LOG：统计日志
- MANIFEST：指一个独立的日志文件，它包含RocksDB的状态快照/版本
- OPTIONS：配置信息
### MANIFEST
![image.png](http://image.huawei.com/tiny-lts/v1/images/9123e06fb169db5ee3c496b425d9f069_1012x411.png@900-0-90-f.png)
Rocksdb对文件系统以及存储介质保持不可预知的态度。**文件系统操作不是原子的，并且在系统错误的时候容易出现不一致**。即使打开了日志系统，文件系统还是不能在一个不合法的重启中保持一致。POSIX文件系统不支持原子化的批量操作。因此，无法依赖RocksDB的数据存储文件中的元数据文件来构建RocksDB重启前的最后的状态。
RocksDB有一个内建的机制来处理这些POSIX文件系统的限制，这个机制就是保存一个名为MANIFEST的RocksDB状态变化的事务日志文件。**MANIFEST文件用于在重启的时候，恢复RocksDB到最后一个一致的一致性状态**。
- MANIFEST 指通过一个事务日志，来追踪RocksDB状态迁移的系统
- Manifest日志 指一个独立的日志文件，它包含RocksDB的状态快照/版本
- CURRENT 指最后的Manifest日志
RocksDB 称 Manifest 文件记录了 DB 状态变化的事务性日志，也就是说它记录了所有改变 DB 状态的操作。 RocksDB 的函数 **VersionSet::LogAndApply** 是对 Manifest 文件的更新操作，所以**可以通过定位这个函数出现的位置来跟踪 Manifest 的记录内容**。 Manifest 文件作为事务性日志文件，**只要数据库有变化，Manifest都会记录**。其内容 size 超过设定值后会被 VersionSet::WriteSnapShot 重写。**RocksDB 进程 Crash 后 Reboot 的过程中，会首先读取 Manifest 文件在内存中重建 LSM 树，然后根据 WAL 日志文件恢复 memtable 内容**
### SST结构
![image.png](http://image.huawei.com/tiny-lts/v1/images/936a0f199988bce33511414e4d668773_855x517.png@900-0-90-f.png)
SST是以block为基本单元的，读取数据的时候会从footer开始。
- footer指向Index Block和Meta Index Block
- Index Block记录了Data Block的索引，通过一个key可以快速定位到key是在哪个data block里，然后再从data block读数据
- Meta Index Block记录的是Meta Block的索引，Meta Block存储了元数据相关的东西，有很多种，图中的一种是Filter Block(布隆过滤器)
#### bloom filter
![image.png](http://image.huawei.com/tiny-lts/v1/images/fbe12c99afb9d14a64d521302c7a1732_844x449.png@900-0-90-f.png)
主要作用是：快速判断一个key是否在sst中
用一串比特位构建布隆过滤器，将受到的key用多个不同的哈希函数，映射到多个bit位上，那么下次key过来了，发现映射到的bit位上都是1，就说明key存在，假如有0，那么key就不存在。
当然会存在一些情况，key其实不存在，只是碰巧映射到的bit位上都是1（被其他的key占了），但结果布隆过滤器告诉我们key是存在的。
不过也不影响，因为就算通过了过滤器，也只是多了一次读取的动作，大不了读不到再返回数据不存在。
### Block Cache
![image.png](http://image.huawei.com/tiny-lts/v1/images/143b84c96f610b90ac1a7c2eec8ba0b7_738x384.png@900-0-90-f.png)
是对系统page cache，进行解压后的block，每次读取会先从block-cache找，再从系统page cache，再从磁盘中找
### Column Family
在RocksDB3.0，增加了Column Families的支持。可以理解为时rocksDB存储空间的一个逻辑分区，如果类比到关系型数据库中，列族可以看做是表的概念。
![image.png](http://image.huawei.com/tiny-lts/v1/images/76e44d40efb95681aacda150a5adef31_950x362.png@900-0-90-f.png)
- 每个column family，都是一个完整的单独的lsm-tree，只不过会共用一个wal来保持多个column操作的原子性
- RocksDB的每个键值对都与唯一一个列族（column family）结合。如果没有指定Column Family，键值对将会结合到“default” 列族。
- 列族提供了一种从逻辑上给数据库分片的方法。他的一些有趣的特性包括：
- 支持跨列族原子写。意味着你可以原子执行Write({cf1, key1, value1}, {cf2, key2, value2})。 跨列族的一致性视图。 允许对不同的列族进行不同的配置 即时添加／删除列族。两个操作都是非常快的。
- 列族的主要实现思想是他们共享一个WAL日志，但是不共享memtable和table文件。通过共享WAL文件，我们实现了酷酷的原子写。通过隔离memtable和table文件，我们可以独立配置每个列族并且快速删除它们。
- 每当一个单独的列族刷盘，我们创建一个新的WAL文件。所有列族的所有新的写入都会去到新的WAL文件。但是，我们还不能删除旧的WAL，因为他还有一些对其他列族有用的数据。我们只能在所有的列族都把这个WAL里的数据刷盘了，才能删除这个WAL文件。这带来了一些有趣的实现细节以及一些有趣的调优需求。确保你的所有列族都会有规律地刷盘。另外，看一ZQptions::max_total_wal_size，通过配置他，过期的列族能自动被刷盘。
### flush
有三种场景会导致memtable落盘被触发：
- Memtable的大小在一次写入后超过write_buffer_size。
- 所有列族中的memtable大小超过db_write_buffer_size了，或者write_buffer_manager要求落盘。在这种场景，最大的memtable会被落盘。
- WAL文件的总大小超过max_total_wal_size。在这个场景，有着最老数据的memtable会被落盘，这样才允许携带有跟这个memtable相关数据的WAL文件被删除。
- 就结果来说，memtable可能还没写满就落盘了。这是为什么生成的SST文件小于对应的memtable大小。压缩是另一个导致SST文件变小的原因，因为memtable里的数据是没有压缩的。

### 写流程
![image.png](http://image.huawei.com/tiny-lts/v1/images/bb202e6c8de801b28e94064ce84ef429_541x341.png@900-0-90-f.png)
1. 获取一个单调递增的int64 sequence number，主要是为所有写入的kv做排序
2. 预处理：判断memtable、wal是否满了，是否需要更换，判断是否需要流控
3. 将写入操作顺序写入WAL日志中，接下来把数据写到 memtable中（采用SkipList结构实现），写入后推进sequence number
4. MemTable达到一定大小后，将这个 memtable 切换为不可更改的 immutable memtable，并新开一个 memtable 接收新的写入请求
5. 这个 immutable memtable进行持久化到磁盘，成为L0 层的 SSTable 文件
6. 每一层的所有文件总大小是有限制的，每下一层大十倍。一旦某一层的总大小超过阈值了，就选择一个文件和下一层的文件合并。

注意： 所有下一层被影响到的文件都会参与 Compaction。合并之后，保证 L1 到 L6 层的每一层的数据都是在 key 上全局有序的。而 L0 层是可以有重叠的

### 写流程的约束
- 日志文件用于崩溃恢复
- 每个MemTable及SST文件中的Key都是有序的（字符顺序的升序）
- 日志文件中的Key是无序的
- **删除操作是标记删除，是插入操作的一种，真正的删除要在Compaction的时候实现**
- 无更新实现，记录更新通过插入一条新记录实现

### TiDB对于写流程的优化
首先RocksDB是支持多线程写入的
![image.png](http://image.huawei.com/tiny-lts/v1/images/21e1df73256fde8b09e90a5e776d711d_861x537.png@900-0-90-f.png)
图中，每一个writer就是一个线程，write queue会把这些writer串成一个链，不过这里实际上每次只有一个writer在写，writer1写完了会notify下一个来写。这样本质还是单线程。引申出write group的概念
![image.png](http://image.huawei.com/tiny-lts/v1/images/258d04240589940aa06859efb7e58d4b_873x545.png@900-0-90-f.png)
将多个writer合并成一个batch，由leader负责将整个group的写入进行完成。
#### 额外的优化：
![image.png](http://image.huawei.com/tiny-lts/v1/images/55a8579586f0b9ff99c21fbb844c1249_874x528.png@900-0-90-f.png)
wal的写入是需要有序性的，但是memtable的写入是不需要有序性的。所以对于wal的写入，由write group leader完成。但是对于memtable的写入，是可以让每个writer自己来负责完成的，以提高并发。当最后一个memtable writer完成之后，由它执行清理操作并通知下一个write group leader
#### 再进一步的优化：
流水化处理
![image.png](http://image.huawei.com/tiny-lts/v1/images/55d97f028c490652bc074a5cad79bab4_1034x460.png@900-0-90-f.png)
当一个write group在写memtable的时候，也可以让另一个write group同时写wal，把IO利用上。

### 大致的读流程
- 先查memtable，再查 immutable memtable，然后查 L0 层的所有文件，最后一层一层往下查。
![image.png](http://image.huawei.com/tiny-lts/v1/images/f11d9ef52ba573efeaad474dce4a88ea_755x603.png@900-0-90-f.png)


- 在 memtable->immutable_memtable->sstFiles 这些环节中，读取 SST Files 是最复杂的。
- 在 RocksDB 中，默认的 SST 是 BlockBasedTable，从字面意思上看，我们可以知道其核心是“Block”，在 kv store 中，数据的最小单位就是 kv，一个 kv，小则几个字节，大则数十数百KB，更多的是在几十到几百字节这个范围。而 Block，一般是 4K, 8K, 16K ...，一个 Block 会包含数十到数百个 kv。
- 但是，由于Block 的存储形态，一般是压缩的，这样，读取 SST File 的时候，我们只能按 Block 读，因为只有解压后才能定位具体的每条 kv，但是显然我们不能每次读取都去解压一个 Block，这就需要 BlockCache……
- 所以主要有两条路
- - **从内存读**:MemTable->Immutable MemTable
- - **从持久化设备读**:首先通过 table cache获取到文件的元数据如布隆过滤器以及数据块索引, 然后读取 block cache, block cache中缓存了SST的数据块,如果命中那就直接读取成功,否则便需要从SST中读取数据块并插入到block cache
- RocksDB 帮我们处理了这其中所有肮脏的细节，而从使用者来看，一切都很简洁
> 题外：[RocksDB读优化 - Indexing SST](https://zhuanlan.zhihu.com/p/556113577)
### 由Snapshot来提供一致性视图
一个快照会捕获在创建的时间点的DB的一致性视图。快照在DB重启之后将消失。一个读取过程的iterator会对应一个snapshot
![image.png](http://image.huawei.com/tiny-lts/v1/images/ea28ce35034acb77b6bf0da816e978d1_655x325.png@900-0-90-f.png)
- 只要发生写入，sequence number就会+1。在底层，每个sequence number都会存在于key当中，ukey就是tikv写入的key，在ukey后面追加上sequence number，再加上type(delete or put)。
- superversion，是当前lsm-tree，memtable版本的一个快照，防止读数据的时候，正在发生compaction，导致要去读到的文件被compaction删掉了。
#### version机制的实现
实际上是 **引用计数**
![image.png](http://image.huawei.com/tiny-lts/v1/images/66bd070e67fd3e9315cf40c3ba8878e9_400x268.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/9f799b7f6856cbc88b28dc2cfdcc474d_509x408.png@900-0-90-f.png)
假如v2不被任何snapshot需要了，那么文件4的引用计数为0了，就可以被物理删除了
![image.png](http://image.huawei.com/tiny-lts/v1/images/335daaf6a9a524e52816057fff1c2e8d_725x409.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/1fcc50848f0740a087410fd5ba873e3f_730x262.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/c50d8c4931177b5d579da36b8c1fcfb5_727x262.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/de51255a1c86b782cd1d4d17d30e54e7_669x258.png@900-0-90-f.png)

### point-lookup 点查
![image.png](http://image.huawei.com/tiny-lts/v1/images/b80283f0d083b5bdec4d8c2e3a85c847_913x455.png@900-0-90-f.png)
> 每个SST都有布隆过滤器
> 需要注意的是在L0层，是需要遍历L0层所有的sst，至到找不到才会到下一层，由于L1层之后，每一次的range是必然不同的，那么可以用二分查找法。

### range-lookup 范围查询
![image.png](http://image.huawei.com/tiny-lts/v1/images/d9b7138d62e9a7eb9a4fc2b1d914adcf_963x316.png@900-0-90-f.png)
> 在获取snapshot和block cache后，会生成一个iterator，这个iterator会对所有查询覆盖的每一层的sst都创建一个iterator，最后通过一个小根堆将这些iterator合并成nested iterators。
> start之后，会让小根堆中的所有iterator都进行一次scan，找到最小的key，然后将key放到小根堆里，小根堆最上面的就是最小的key，依次类推
> 布隆过滤器在范围查询中不试用，针对范围查询设计了 prefix bloom，对每一层要scan的key的prefix做了bloom filter

### Compaction
LSM-Tree 能将离散的随机写请求都转换成批量的顺序写请求（WAL + Compaction），以此提高写性能。但也带来了一些问题：

读放大（Read Amplification）。LSM-Tree 的读操作需要从新到旧（从上到下）一层一层查找，直到找到想要的数据。这个过程可能需要不止一次 I/O。特别是 range query 的情况，影响很明显。
空间放大（Space Amplification）。因为所有的写入都是顺序写（append-only）的，不是 in-place update ，所以过期数据不会马上被清理掉。
写放大。实际写入 HDD/SSD 的数据大小和程序要求写入数据大小之比。正常情况下，HDD/SSD 观察到的写入数据多于上层程序写入的数据。
RocksDB 和 LevelDB 通过后台的 compaction 来减少读放大（减少 SST 文件数量）和空间放大（清理过期数据），但也因此带来了写放大（Write Amplification）的问题。

**写放大、读放大、空间放大，三者就像 CAP 定理一样，需要做好权衡和取舍。**
压缩算法有很多种，RocksDB也支持很多种，比较经典的是如下这两个
Tiered Compaction vs Leveled Compaction
**当 Level 0 刷到 Level 1，让 Level 1 的 SST 文件达到设定的阈值，就需要进行 compaction。**
![image.png](https://user-images.githubusercontent.com/87458342/132666300-89414a96-987b-4d59-94cb-773107499560.png)
- 对于 Tiered 来说，我们会将所有的 Level 1 的文件 merge 成一个 Level 2 SST 放在 Level 2。也就是说，compaction 其实就是将上层的所有小的 SST merge 成下层一个更大的 SST 的过程。
- 而对于 Leveled 来说，不同 Level 里面的 SST 大小都是一致的，Level 1 里面的 SST 会跟 Level 2 一起进行 merge 操作，最终在 Level 2 形成一个有序的 SST，而各个 SST 不会重叠。

### File ingetsion
> RocksDB向用户提供了一系列API用于创建及导入SST文件。在你需要快速读取数据但是数据的生成是离线的时候，这非常有用。
可以通过SstFileWriter这个对象来直接写sst，然后再将sst通过Ingest导入到RocksDB中。

在TiKV中可以用来做副本迁移：scan Tikv1生成sst，通过网络将sst传输到Tikv2，然后通过ingest将数据写入rocksDB里，由如下步骤
![image.png](http://image.huawei.com/tiny-lts/v1/images/24623aca2b48e8f9a43bbc779df19ab2_534x178.png@900-0-90-f.png)

## RocksDB如何提供 并发 读写能力
通用的有几种数据结构--- 红黑树 OlogN，跳表OlogN，B+树 O(m*logN)
假设用红黑树，那么对数据做增删操作的话，就涉及到对树重新着色或者旋转，那么在多线程的环境下，就需要对整个树结构加锁，锁的力度过大。

**所以跳表更适合多线程并发读写**
![image.png](http://image.huawei.com/tiny-lts/v1/images/c6f8fa345af1b76a780917dc3f3d8afe_962x382.png@900-0-90-f.png)

	假如要插入元素17，其实只需要对要插入的节点、和高度加锁。高度只影响读
	插入时，先跳到对应的位置，先随机成熟，再建立节点之间的关系。图中随机了2层，那么先建立12、17、19的联系，再建立9、17、25的联系。
	实际代码中还用了很多办法，为了能够在多线程环境钟不出错的情况下，尽可能的提升效率
![image.png](http://image.huawei.com/tiny-lts/v1/images/354f7733c44f5cc8eae010e6d943fa90_696x167.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/c793b217e5e32f719ef8d9c6e78c0a1f_651x122.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/8942bcb8eeafb2b53427a71d0b226122_730x354.png@900-0-90-f.png)

	内存模型relaxed和release的区别 -->
	如果用release：那么不允许指令重排，那么上面的for循环代码会被优化到下面，那么插入17会导致17元素的位置会立即生成一个有高度的索引节点，那么可以提升查询效率
	如果用relaxed：for循环代码会被优化到下面去，导致不会立马多出一个索引节点，但是数据是正确的，并发能力得到了提升

------------
RocksDB替换Redis：Pika
解决重启加载数据慢的问题。redis是内存数据库，比如数据到50G以上，redis重启从磁盘加载数据到内存中就需要大量的时间。
RocksDB替换innoDB：MyRocks
解决存储效率的问题。举例子，mysql存一份数据是150G，但是用rocksDB存储相同的数据占用空间只有50G，因为磁盘顺序读写，填充率会很高，而且B+是为了追求读效率而牺牲了写效率，空间换时间。
TiDB用rocksDB做持久化，增加节点就不需要像mysql做分库分表那样费力了。
# TiDB中的RocksDB及优化（初步了解）
#### TiDB应用RocksDB的接口：
![image.png](http://image.huawei.com/tiny-lts/v1/images/e409e66dddae11771169a95393942098_576x210.png@900-0-90-f.png)
主要用的接口有write(batch)，file ingestion(副本搬迁，将整个region的数据打包成SST，搬迁到另一个region)
#### TiKV中的RocksDB：
![image.png](http://image.huawei.com/tiny-lts/v1/images/58c850a1d2c023acd90310a4cbed71a1_366x343.png@900-0-90-f.png)
- Coprocessor和KV Api负责与TiDB交互
- RocksDB在Raft层下，借用Raft保证一个region的多个副本的数据是一致的
![image.png](http://image.huawei.com/tiny-lts/v1/images/70061f9e99f1ec9c1298e286afe852b4_694x228.png@900-0-90-f.png)
实际上，单个TiKV会启动两个RocksDB实例，一个是raft log DB（存储raft log）一个是data kv DB（当raft log 进行apply的时候，将apply的数据apply进kv db中）
## Titan
> LSM树 存在 写放大的问题，一方面是由于key-value格式数据中的value过大，那么可以将key和value分离，value保存到一个单独的文件，key保存的是一个handle，这个handle指向对应的数据。对于小value，直接存到sst中
> 但是也会有弊端，因为多引入了一次IO，不过额外的损耗是在预期范围内的。
![image.png](http://image.huawei.com/tiny-lts/v1/images/08885f73fc08b3a55da814f34b069c5d_755x225.png@900-0-90-f.png)
### Titan的架构
![image.png](http://image.huawei.com/tiny-lts/v1/images/93bb4c38c39fad1ecf99f7403a22bd6a_877x497.png@900-0-90-f.png)
> 在memtable flush阶段和compatction阶段，将大value写入到blob里面，同时blob也有单独的GC负责清理失效key对应的value
#### Titan GC算法
![image.png](http://image.huawei.com/tiny-lts/v1/images/2b2c421124cc483b611a553ad8944495_856x222.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/f910f07a7c6bf56333109991dd9feb56_878x323.png@900-0-90-f.png)
![image.png](http://image.huawei.com/tiny-lts/v1/images/1efb747cae7ef6f7d7c4de4acce4bc18_756x430.png@900-0-90-f.png)
## Compaction Guard
> 由于LSM写放大，可能会占用大量磁盘IO，导致tidb前台写入性能很低。Compaction Guard用来解决这一问题

- 写放大的主要原因是 SST中数据的range是不可控的，可能在一次compaction中，有一些range是不需要重新写的，但是由于range overlap，SST之间的range中有部分数据是重叠的，导致在compaction的时候还需要对这一部分数据重新写。
- 将SST 按照range进行切割，划分边界，那么假如只对一个region进行热点写入的话，compaction也就只发生在这个region，其他region是不受影响的。所以可以减少写放大、
![image.png](http://image.huawei.com/tiny-lts/v1/images/97f4b736c4dcf4a679ecf70b9f1d41fc_997x413.png@900-0-90-f.png)
## Compaction Filter
> Tikv 上层的GC会进行很多delete操作，但是对于rocksDB来说，实际还是一些key-value值的插入操作，直到delete刷到了最底层的时候，才可以把对应的记录删除掉。删掉之后会产生tombstones。
> 但是对于一个SST来说，如果它内部是有很多tombstone的话，对于范围查找，就会浪费很多不必要的扫描（过滤掉无用的key）。
![image.png](http://image.huawei.com/tiny-lts/v1/images/304ff93389fc5c1716d35cc0345a4273_926x120.png@900-0-90-f.png)
- 使用Compaction Filter改变了tikv事务GC的逻辑，不再需要tikv在上层通过调rocksDB的delete接口来删除数据了。
- 而是注入到rocksDB内部，当rocksDB进行compaction的时候，就顺带将无效的事务版本的数据给清楚掉。
## Titan level merge
> Titan解决了写放大，但是影响了范围查找的效率，主要是由于blob文件不是排序过的，需要random IO查找
> 读和写是种tradeoff
> 在进行compaction生成新SST的时候，同时对blob file进行mergeSort，使得blob是严格有序的，就变成sequence io了。
![image.png](http://image.huawei.com/tiny-lts/v1/images/4f7e57ec060c46c275945d5d4d4f65e8_954x427.png@900-0-90-f.png)

