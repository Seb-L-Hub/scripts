#to improve : create tmp directories for src

#!/bin/bash
sudo mkdir -p /data/tmp
sudo chmod 777 /data/tmp/
sudo mkdir -p /data/tmp/opt
sudo chmod 777 /data/tmp/opt


#destination
#!/bin/bash
sudo screen rsync -av slesourd@frosssfrm1v1104.devops.in.idemia.com:/data/tmp/opt/ /data/tmp/test
