# 查看swap是否已关闭
swapon --show
 
# 格式化
parted -s -a optimal /dev/vdb mklabel gpt -- mkpart primary ext4 1 -1
mkfs.ext4 /dev/vdb1
lsblk -f
# 挂盘
mkdir /tidb
echo '/dev/vdb1 /tidb ext4 defaults,nodelalloc,noatime 0 2' >> /etc/fstab
mount -a
# 添加tidb用户
useradd -u 1000 -g 100 -m tidb -s /bin/bash
echo 'tidb:tidb' | chpasswd
chown -R tidb:users /tidb
echo 'tidb ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers
# 更新sources.list
cp /etc/apt/sources.list /etc/apt/sources.list.bak
echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic main multiverse restricted universe' > /etc/apt/sources.list
echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-backports main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-proposed main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-security main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-updates main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-backports main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-proposed main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-security main multiverse restricted universe' >> /etc/apt/sources.list
echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-updates main multiverse restricted universe' >> /etc/apt/sources.list
# 安装ntp服务
apt-get install -y ntp
# 配置华为时钟服务
echo 'server szxntp01-in.huawei.com' >> /etc/ntp.conf
echo 'server szxntp02-in.huawei.com' >> /etc/ntp.conf
systemctl enable ntp
systemctl restart ntp
 
ntpq -p
 
# 安装expect
apt-get install expect

准备批量免密登录脚本，这里挫了一点，后面再改
add_auth.sh
#!/bin/bash

line=$(cat nodes_passwd.conf|wc -l)

for ((i=1;i<=$line;i++))
do
tmp_row=$(sed -n "${i}p" nodes_passwd.conf)
ip=$(echo $tmp_row|awk '{print $1}')
echo $ip
password=$(echo $tmp_row|awk '{print $2}')
./do.exp "$ip" "$password"
done

do.exp
#!/usr/bin/expect
set timeout 2
set ip [lindex $argv 0]
set password [lindex $argv 1]

spawn ssh-copy-id -i /root/.ssh/id_rsa.pub -o StrictHostKeyChecking=no root@$ip
expect "assword:"
send "$password\n";

expect eof
exit

add_auth_tidb.sh
#!/bin/bash

line=$(cat nodes_passwd.conf|wc -l)

for ((i=1;i<=$line;i++))
do
tmp_row=$(sed -n "${i}p" nodes_passwd_tidb.conf)
ip=$(echo $tmp_row|awk '{print $1}')
echo $ip
password=$(echo $tmp_row|awk '{print $2}')
./do_tidb.exp "$ip" "$password"
done

do_tidb.exp
#!/usr/bin/expect
set timeout 2
set ip [lindex $argv 0]
set password [lindex $argv 1]

spawn ssh-copy-id -i /home/tidb/.ssh/id_rsa.pub -o StrictHostKeyChecking=no tidb@$ip
expect "assword:"
send "$password\n";

expect eof
exit

# 编辑节点ip列表
vim /home/tidb/nodes.conf
chown tidb:users /home/tidb/nodes.conf
# 编辑节点ip和密码列表
vim /home/tidb/nodes_passwd.conf
chown tidb:users /home/tidb/nodes_passwd.conf
vim /home/tidb/nodes_passwd_tidb.conf
chown tidb:users /home/tidb/nodes_passwd_tidb.conf
# 免密登录
cd /home/tidb && bash add_auth.sh
# ssh-keygen -t rsa -P ""
# for ip in $(cat /home/tidb/nodes.conf);do ssh-copy-id -i /root/.ssh/id_rsa.pub -o StrictHostKeyChecking=no root@$ip;done
# 手动输一下密码
# 批量ssh执行脚本
echo '#!/bin/bash' >> /home/tidb/runcmd.sh
echo 'maxjobs=0' >> /home/tidb/runcmd.sh
echo "awk '{print $1}' /home/tidbnodes.conf|xargs -i -P \$maxjobs ssh -o StrictHostKeyChecking=no {} \$@" >> /home/tidb/runcmd.sh
chown tidb:users /home/tidb/runcmd.sh
 
# 远程节点操作
sh /home/tidb/runcmd.sh parted -s -a optimal /dev/vdb mklabel gpt -- mkpart primary ext4 1 -1
sh /home/tidb/runcmd.sh mkfs.ext4 /dev/vdb1
sh /home/tidb/runcmd.sh lsblk -f
sh /home/tidb/runcmd.sh mkdir /tidb
sh /home/tidb/runcmd.sh "echo '/dev/vdb1 /tidb ext4 defaults,nodelalloc,noatime 0 2'>> /etc/fstab"
sh /home/tidb/runcmd.sh mount -a
sh /home/tidb/runcmd.sh useradd -u 1000 -g 100 -m tidb -s /bin/bash
sh /home/tidb/runcmd.sh "echo 'tidb:tidb'|chpasswd"
sh /home/tidb/runcmd.sh chown -R tidb:users /tidb
sh /home/tidb/runcmd.sh "echo 'tidb ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers"
sh /home/tidb/runcmd.sh cp /etc/apt/sources.list /etc/apt/sources.list.bak
sh /home/tidb/runcmd.sh rm /etc/apt/sources.list
sh /home/tidb/runcmd.sh "echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic main multiverse restricted universe' > /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-backports main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-proposed main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-security main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb http://mirrors.tools.huawei.com/ubuntu/ bionic-updates main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-backports main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-proposed main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-security main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh "echo 'deb-src http://mirrors.tools.huawei.com/ubuntu/ bionic-updates main multiverse restricted universe' >> /etc/apt/sources.list"
sh /home/tidb/runcmd.sh apt-get update
sh /home/tidb/runcmd.sh apt-get install -y ntp
sh /home/tidb/runcmd.sh "echo 'server szxntp01-in.huawei.com' >>/etc/ntp.conf"
sh /home/tidb/runcmd.sh "echo 'server szxntp02-in.huawei.com' >>/etc/ntp.conf"
sh /home/tidb/runcmd.sh systemctl enable ntp
sh /home/tidb/runcmd.sh systemctl restart ntp
sh /home/tidb/runcmd.sh ntpq -p
 
su - tidb
ssh-keygen -t rsa -P ""
cd /home/tidb && bash add_auth_tidb.sh
 
 
 
将安装包上传到中控机
chown -R tidb:users /home/tidb/
cd /home/tidb/tidb_pkg/
tar xzvf tidb-community-server-v6.1.0-linux-amd64.tar.gz
sh tidb-community-server-v6.1.0-linux-amd64/local_install.sh
source /home/tidb/.bashrc
 
tar xf tidb-community-toolkit-v6.1.0-linux-amd64.tar.gz
ls -ld tidb-community-server-v6.1.0-linux-amd64 tidb-community-toolkit-v6.1.0-linux-amd64
cd tidb-community-server-v6.1.0-linux-amd64
cp -rp keys ~/.tiup/
tiup mirror merge ../tidb-community-toolkit-v6.1.0-linux-amd64
cd /home/tidb/
tiup cluster template > topology.yaml
 
vim topology.yaml
 
# # Global variables are applied to all deployments and used as the default value of
# # the deployments if a specific deployment value is missing.
global:
 user: "tidb"
 ssh_port: 22
 deploy_dir: "/tidb/tidb-deploy"
 data_dir: "/tidb/tidb-data"
 log_dir: "/tidb/tidb-deploy/log"
 
monitored:
 node_exporter_port: 9100
 blackbox_exporter_port: 9115
 
server_configs:
 tidb:
   log.slow-threshold: 300
 tikv: 
   readpool.storage.use-unified-pool: false
   readpool.coprocessor.use-unified-pool: true
 pd:
   replication.max-replicas: 3
   replication.location-labels: ["zone", "host"]
   replication.enable-placement-rules: true
 
pd_servers:
 - host:
 - host:
 - host:
 
tidb_servers:
 - host:
 - host:
 - host:
 
tikv_servers:
 - host:
   config:
     server.labels:
       zone: az1
       host: tikv1
 - host:
   config:
     server.labels:
       zone: az1
       host: tikv2
 - host:
   config:
     server.labels:
       zone: az1
       host: tikv3
 
monitoring_servers:
 - host:
 
grafana_servers:
 - host:
 
alertmanager_servers:
 - host:
 
 
cp /home/tidb/.ssh/id_rsa.pub /home/tidb/.ssh/authorized_keys
 
tiup cluster deploy tidb-test v6.1.0 ./topology.yaml --user tidb
 
tiup cluster start tidb-test --init
 记录一下密码
 
sudo apt install mysql-client
 
mysql -h -P 4000 -u root -p
