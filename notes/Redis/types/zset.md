与java的treeSet在功能上类似，但底层数据结构差别很大。
SortedSet中的每个元素都有一个score属性，可以基于score属性对元素排序.
特征：
- 可排序
- 元素不重复
- 查询速度快

skiplist编码的有序集合对象底层实现是跳跃表和字典两种；<br/>
每个跳跃表节点都保存一个集合元素，并按分值从小到大排列；节点的object属性保存了元素的成员，score属性保存分值；<br/>
字典的每个键值对保存一个集合元素，字典的键保存元素的成员，字典的值保存分值。<br/>
![image](https://user-images.githubusercontent.com/87458342/132522391-e134841e-3548-4f54-8c49-bb3bc4d9492b.png)

* 为何skiplist编码要同时使用跳跃表和字典实现？
跳跃表优点是有序，但是查询分值复杂度为O(logn)；字典查询分值复杂度为O(1) ，但是无序，所以结合连个结构的有点进行实现。<br/>
虽然采用两个结构但是集合的元素成员和分值是共享的，两种结构通过指针指向同一地址，不会浪费内存。
 
* 有序集合编码转换：
有序集合对象使用ziplist编码需要满足两个条件：一是所有元素长度小于64字节；二是元素个数小于128个；不满足任意一条件将使用skiplist编码。<br/>
以上两个条件可以在Redis配置文件中修改zset-max-ziplist-entries选项和zset-max-ziplist-value选项。
 
* 应用场景：
排行榜，取TopN操作。带权重的消息队列。

### 常见commands
- ZADD key score member：添加一个或多个元素到sorted set，**如果已存在则更新其score值**
- ZREM key member：删除zset中指定的元素
- ZSCORE key member：获取zset中指定元素的score
- ZRANK key member：获取zset中指定元素的排名（从0开始）
- ZCARD key：获取zset中元素的个数
- ZCOUNT key min max：统计score值在给定范围内的元素个数
- ZINCRBY key increment member：让zset中指定的元素的score自增指定步长值
- ZRANGE key min max：按照score排序后，获取**指定排名范围内**的元素
- ZRANGEBYSCORE key min max：按照score排序后，获取**指定score范围内**的元素
- ZDIFF、ZINTER、ZUNION：求差集、交集、并集
> 所有的排名默认是升序，如果要降序，需要在Z后面加上REV：ZREVRANK
