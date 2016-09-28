# Mule on Android application

[![Logo](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/mule_common.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/mule_common.png)

Application for Android platform to work in ED2K(eDonkey2000) networks. Based on ED2K library - see description below.

## Links

* [Google Play](https://play.google.com/store/apps/details?id=org.dkf.jmule&hl=en)
* [Amazon](https://www.amazon.com/DKF-software-Mule-on-Android/dp/B01LYN526Q/ref=sr_1_1?s=mobile-apps&ie=UTF8&qid=1475048708&sr=1-1)

## Implemented Features

* Search files on servers
* Download files

## Features in plans

* Internationalization
* KAD support
* Playing file during downloading

## Screenshots

Transfers             |  Search           |  Servers
:-------------------------:|:-------------------------: |:-------------------------:
[![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-21-51.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-21-51.png) | [![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-21-57.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-21-57.png) | [![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-22-02.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-22-02.png)

Settings             |  Servers connected           |  Servers core stopped
:-------------------------:|:-------------------------: |:-------------------------:
[![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-22-14.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-22-14.png) | [![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-22-24.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-26-20-22-24.png) | [![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-11-50.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-11-50.png)

Settings connect core            |  Search core stopped          |  Menu
:-------------------------:|:-------------------------: |:-------------------------:
[![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-12-00.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-12-00.png) | [![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-12-11.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-12-11.png) | [![S1](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-12-21.png)](https://raw.githubusercontent.com/a-pavlov/jed2k/master/android/docs/Screenshot_2016-09-28-12-12-21.png)

# Java library for ED2K(eDonkey) networks

[![Build status](https://travis-ci.org/a-pavlov/jed2k.svg?branch=master)](https://travis-ci.org/a-pavlov/jed2k.svg?branch=master)

## Why Java?

Main goal - native application for Android platform. Lighter, faster, more user friendly and convenient than current [Mule for Android](https://play.google.com/store/apps/details?id=org.dkfsoft.AndroidMuleFree&hl=en).

## Techniques

* Async network I/O using Java NIO
* Async disk I/O operations emulation via one single thread executor service per session
* Project structure inspired by [libed2k](https://github.com/qmule/libed2k)

## Implemented features
* Packets parsing engine
* Alerts system
* Exception system with one type of exception and error code for each problem
* Search on servers(with all parameter types), search related, search more
* Downloading parts of files
* Downloading compressed parts of files(not recommended as default!)
* Connections policy
* Naive piece picker optimized to download fist and last pieces first for preview feature
* Naive piece manager - online pieces hash calculation and hash verification during downloading

## What next

* Stable code, fixing bugs, increase performance
* Advandced piece picker and piece manager
* KAD support

## Building Maven/Gradle
Few dependencies in pom.xml - Mockito for unit tests and slf4j logging facade.

1. clone project
2. cd jed2k && mvn package

If you prefer Gradle - use gradle build on second step.

## Testing
You can use simple console downloader class org.jed2k.Conn. Before usage you have to set incoming directory as first parameter.
Do not use double quotes in commands below - there are for mark parameters.
Commands:

* connect to server, default port 4661: connect "server_address_or_ip" [port]
* search on server: search "search_phrase"
* search on server: search "search_phrase" dataSize "limit_in_mb"
* save search results: save
* load search results: restore
* show search results: print
* create transfer: load "hash" "size" "filepath"
* create transfer: load "emule_link"
* create transfer: load "number of search"
* delete transfer: delete "hash"
* exit application: quit

Special case - trial session - fixed sources addresses. Setup -Dsession.trial=true, -Dsession.peers=a.b.c.d:port,....

## Help
If you know Java/C++, Android or Java for Android, use eMule or simply would like help project - welcome :smile:
