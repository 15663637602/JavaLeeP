## 伪静态配置
1. 是为了使网站更容易被搜索引擎收录，因为搜索引擎在抓取网页时，会对html、htm页面非常友好，“认为”页面是静态不变的，更加稳定安全，更有利于用户；
2. 是网址写成html、htm看上去更加正规规范，这点当然是针对用户而言，进一步对于推广网站的时候，网址简洁也有利于用户记忆。
3. 万变不离其宗，网站的群体是用户，用户访问网站的渠道主要是通过搜索引擎，因此使用URL重写能让更多用户访问我们的网站

-----
> rewrite是实现URL重写的关键指令，根据regex的内容，重定向到replacement，结尾是flag标记
rewrite <regex> <replacement> [flag];


#### 举个例子，假如对一个页面做分页展示
需要输入的url是 xxx/index.jsp?pageNum=2
```
在conf中配置

location / {
  # rewrite表示 将用户输入的url 按照第一列规则进行匹配，转到第二列的url。
  # ^$ 表示开启正则表达式，第二列中的$1，标识捕获第一列中的第一个正则表达式匹配出来的结果。如果是$2，就是匹配第二个正则表达式的结果
  rewrite ^/([0-9]+).html$  /index.jsp?pageNum=$1 break;
  proxy_pass http://192.168.44.140:8080
}

```

##### rewrite参数的标签段位置：server，location，if
##### flag标记：
- last：本条规则匹配完成后，继续向下匹配新的location uri规则
- break：本条规则匹配完成后，不再匹配后面的任何规则
- redirect：返回302临时重定向，浏览器地址会显示跳转后的URL地址
- permanent：返回301永久重定向，浏览器地址栏会显示跳转后的URL地址  
