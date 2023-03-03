hosts文件中附加

7.183.36.181 flt.yq.l.com

7.183.36.181 antenna.yq.l.com

然后conf文件中增加一个server，name是antenna.yq.huawei.com
```
   server {
        listen       80;
        server_name  localhost;


        location / {
            root   html;
            index  index.html index.htm;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

    }

    server {
      listen 80;
      server_name antenna.yq.l.com;

      location / {
         root /usr1/web/www;
         index index.html;
      }
      error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
      }
    }
```
浏览器访问http://antenna.yq.l.com，就会转向 /usr1/web/www/index.html

> server_name可以用正则匹配符，或者用空格隔开多个name
