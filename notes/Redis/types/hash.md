也叫散列，其value是一个无序字典
**可以将对象中的每个字段独立存储，可以针对单个字段做CRUD**

哈希对象的编码可以是ziplist和hashtable之一。
 
* 1. ziplist编码
ziplist编码的哈希对象底层实现是压缩列表，在ziplist编码的哈希对象中，key-value键值对是以紧密相连的方式放入压缩链表的，先把key放入表尾，再放入value；键值对总是向表尾添加。<br/>
![image](https://user-images.githubusercontent.com/87458342/132521588-ec193f11-3ee4-41b2-b9a4-7f30405d4977.png)

* 2. hashtable编码
hashtable编码的哈希对象底层实现是字典，哈希对象中的每个key-value对都使用一个字典键值对来保存。<br/>
字典键值对即是，字典的键和值都是字符串对象，字典的键保存key-value的key，字典的值保存key-value的value。<br/>
![image](https://user-images.githubusercontent.com/87458342/132521715-b3cc011a-1f38-473b-821c-ee66364d82a5.png)

* 哈希对象编码转换：
**哈希对象使用ziplist编码需要满足两个条件：一是所有键值对的键和值的字符串长度都小于64字节；二是键值对数量小于512个；不满足任意一个都使用hashtable编码。**<br/>
以上两个条件可以在Redis配置文件中修改**hash-max-ziplist-value选项和hash-max-ziplist-entries**选项。
 
* 应用场景：
存储、读取、修改对象属性，比如：用户（姓名、性别、爱好），文章（标题、发布时间、作者、内容）

### 常见commands
- HSET key field value：添加或者修改hash类型key 的field的值
- HGET key field：获取一个hash类型key的field的值
- HMSET：批量添加多个hash类型key的field的值
- HMGET：批量获取
- HGETALL：获取指定hash类型key 的所有field和value
- HKEYS：获取key的所有fields
- HVALS：获取指定key 的所有value
- HINCRBY：让一个hash类型key 的字段值自增并指定步长
- HSETNX：在hash类型的key中添加一个field值，前提是这个key不存在，否则不执行
