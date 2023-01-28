- KEYS 查看符合pattern的所有key
> \> keys \*name\*
> \> keys a\*
> 模糊匹配查询效率低，可能会导致服务阻塞，如果不是主从部署，不要在pro环境用这个命令
- DEL 删除一个或多个指定的key
> \> del k1 k2 k3
- EXISTS 判断key是否存在
> \> exists name
- EXPIRE 给某个key设置有效期
> \> expire age 20
- TTL 查看一个key的剩余有效期，-1是永久有效，-2是key已被移除
> \> ttl age
