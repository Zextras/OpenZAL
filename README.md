ZAL - The Zimbra Abstraction Layer for Zextras Suite (ZxSuite).
===
<http://openzal.org/>

ZAL is distributed under the terms of GNU General Public License version 2 <http://www.gnu.org/licenses/gpl-2.0.html>.

### Download ###

Latest ZAL is downloadable from <git@github.com:ZeXtras/OpenZAL.git>
You can clone ZAL repository using:
        
>`git clone https://zextras.github.io/OpenZAL/`

### Runtime Dependencies ###

   Zimbra is all you need ;)

### Simple build ###

>`$ ./build zal-all`
All dependencies will be downloaded automatically and jars will be created in dist/{VERSION}/zal.jar

### Build Targets ###
```
$ ./build
  ZAL - Version 2.5.0

  help                                    Show this help message                  
  zal-all                                 build zal for all zimbra versions       
  zal-common                              build zal for most commons zimbra versions
  zal-dev-current-source                  build zal against current zimbra source in dev mode (zimbra jar must be located in ../zm-zcs-lib and ../zm-mailbox)
  zal-dev-current-binary                  build zal against current zimbra binary in dev mode (zimbra be installed in /opt/zimbra)
  zal-dev-last                            build zal against last zimbra version in dev mode
  zal-{zimbra-version}                    build zal against specified zimbra version in dev mode
  compatibility-check                     check zal Java API Compliance against all zal versions
  fast-compatibility-check                check zal Java API Compliance only against previous zal version
  clean                                   clean up temporary                      
```
**zal-dev-current-binary**:

OpenZAL can be place anywhere, and zimbra must be installed into /opt/zimbra.
Then run:
> `./build zal-dev-current-binary`

**zal-dev-current-source**:

Simply place OpenZAL into same directory of zm-build and follow guide <https://github.com/Zimbra/zm-build/wiki/installer-build> to 
build Zimbra. Then run:
>`./build zal-dev-current-source`

**Adding a zimbra version**:
For each zimbra version you want to build ZAL for:

* copy all file from "/opt/zimbra/lib/jars/" in "zimbra/${VERSION}/jars/"
* copy from "/opt/zimbra/common/jetty_home/lib/"  
jetty-continuation-x.x.x.y.jar
jetty-http-x.x.x.y.jar
jetty-io-x.x.x.y.jar
jetty-rewrite-x.x.x.y.jar
jetty-security-x.x.x.y.jar
jetty-server-x.x.x.y.jar
jetty-servlet-x.x.x.y.jar
jetty-servlets-x.x.x.y.jar
jetty-util-x.x.x.y.jar
in "zimbra/${VERSION}/jars/"
* copy jtnef-x.x.x.jar from "jetty_base/common/lib" in "zimbra/${VERSION}/jars/" 


