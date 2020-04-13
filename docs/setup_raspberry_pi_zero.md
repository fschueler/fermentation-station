# Setup Raspberry Pi Zero

The raspberry pi zero will run the sensor software in the fermantation chamber.

## Instructions

What you need:
- Raspberry Pi Zero
- Micro SD Card
- Micro USB power

### Download & Install Raspbian Image

1. Download image from xxx

2. Insert and find your SD card

```bash
felix@taco:~$ sudo lsblk -p
NAME                                MAJ:MIN RM   SIZE RO TYPE  MOUNTPOINT
/dev/mmcblk0                        179:0    0  28,9G  0 disk  
├─/dev/mmcblk0p1                    179:1    0    16M  0 part  
└─/dev/mmcblk0p2                    179:2    0  28,9G  0 part  
/dev/sda                              8:0    0   477G  0 disk  
├─/dev/sda2                           8:2    0     1K  0 part  
├─/dev/sda5                           8:5    0 476,5G  0 part  
```

3. Flash image onto SD card

```bash
felix@taco:~$ unzip -p Downloads/2020-02-13-raspbian-buster-lite.zip | sudo dd of=/dev/mmcblk0 bs=4M status=progress conv=fsync
1846804480 bytes (1,8 GB, 1,7 GiB) copied, 53,0006 s, 34,8 MB/s 
0+25461 records in
0+25461 records out
1849688064 bytes (1,8 GB, 1,7 GiB) copied, 85,8606 s, 21,5 MB/s
```

### Setup SSH access

Following instructions here: https://raspberrytips.com/pi-zero-setup-without-keyboard/

1. Remove and re-insert your SD card

2. Go into the boot partition of your SD card.
It might open automatically when you insert your SD card. If it doesn't use `lsblk -p` again to find the mountpoint.

3. Create the file `ssh` (no extension, no content): `touch ssh`

### Setup Wifi

1. Again go to the boot partition of your raspberry pi

2. Create a file `wpa_supplicant.conf`

3. The content of the file is the wifi configuration for your home wifi, for example:

```
country=DE
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

network={
  ssid="YOUR_WIFI_NAME"
  scan_ssid=1
  psk="YOUR_WIFI_PASSWORD"
  key_mgmt=WPA-PSK
}
```

That's it, you should be able to eject your SD card now, insert it into your raspberry pi and connect to is using SSH.

### Connect using SSH

1. Find your own network IP address:

```
felix@taco:~$ ip addr show
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: enp0s31f6: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc pfifo_fast state DOWN group default qlen 1000
    link/ether 30:3b:6f:a7:8a:bf brd ff:ff:ff:ff:ff:ff
3: wlp4s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 1000
    link/ether b6:33:14:bf:22:76 brd ff:ff:ff:ff:ff:ff
    inet 192.169.168.21/24 brd 192.169.168.255 scope global dynamic wlp4s0
       valid_lft 856014sec preferred_lft 856014sec
    inet6 ff10::354a:cd66:3ae7:bb00/64 scope link 
       valid_lft forever preferred_lft forever
```

In this case, the IP address we are looking for is the one for the wifi adapter (wlp4s0). It is `192.169.168.21/24`

2. Scan the IP range for all devices in the network:

```
felix@taco:~$ nmap -sn 192.169.168.0/24

Starting Nmap 7.01 ( https://nmap.org ) at 2020-04-13 14:58 CEST
Nmap scan report for fritz.box (192.169.168.1)
Host is up (0.0026s latency).
Nmap scan report for taco.fritz.box (192.169.168.21)
Host is up (0.000053s latency).
Nmap scan report for 192.169.168.54
Host is up (0.0075s latency).
Nmap scan report for raspberrypi.fritz.box (192.169.168.58)
Host is up (0.026s latency).
Nmap done: 256 IP addresses (4 hosts up) scanned in 2.36 seconds

```

The IP for our raspberry pi is `192.169.168.58`

3. Connect to the raspberry pi using ssh and the default login/password combiantion (pi/raspberry):

```
felix@taco:~$ ssh pi@192.169.168.58
pi@192.169.168.58's password: 
Linux raspberrypi 4.19.97+ #1294 Thu Jan 30 13:10:54 GMT 2020 armv6l
```

You should now have access to the raspberry pi!
