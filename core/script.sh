!/bin/sh

dd if=/dev/zero of=my_fs bs=1024 count=50000
losetup -d /dev/loop0
sudo losetup /dev/loop0 /home/apavlov/dev/my_fs
sudo mkfs -t ext3 -m 1 -v /dev/loop0
sudo mount -t ext3 /dev/loop0 /mnt/fs
sudo chmod a+rwx /mnt/fs/


