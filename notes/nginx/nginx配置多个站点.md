hosts文件中附加
7.183.36.181 flt.yq.huawei.com
7.183.36.181 antenna.yq.huawei.com
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
      server_name antenna.yq.huawei.com;

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
浏览器访问http://antenna.yq.huawei.com，就会转向 /usr1/web/www/index.html
