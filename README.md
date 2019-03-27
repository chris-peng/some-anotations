### 这里提供一些注解，可以在不破坏原有代码结构的情况下，为项目灵活地增加一些切面功能。目前包含：
* perm系列：简单的接口级权限控制
* cache系列：方法调用结果的自动缓存管理
* log系列：用于记录操作日志

**请注意：类的内部方法调用不会被自动代理，所以这些注解只能作用在每个类的第一层方法调用上。**

### 详细介绍
* **perm系列**

  用来快速实现接口（方法）级的权限控制。可单独使用，也可作为其他安全框架的补充。
  
  最简注解形式：
  
    ```java
        @RequirePermission("addUser")
        public User addUser(){ ... }    //拥有"addUser"权限才允许调用该方法
    ```
        
  更复杂的注解形式：
  
    ```java
        @RequirePermission({"createUser", "updateUser"})
        public void userMgr(){ ... }    //同时拥有"createUser"和"updateUser"权限才允许调用该方法
        
        @RequireAnyPermission({@RequirePermission("createUser"), @RequirePermission("updateUser")})
        public void userMgr(...)    //拥有"createUser"和"updateUser"其中一项权限即可调用该方法

        @RequireAnyPermission({@RequirePermission({"createUser", "deleteUser"}), @RequirePermission("updateUser")})
        public void userMgr(...)    //同时拥有"createUser"和"deleteUser"权限，或者拥有"updateUser"权限即可调用该方法
        
        //也可注解整个类
        @RequirePermission("userMgr")
        public class UserController{ ... }    //该类所有方法都需要拥有"userMgr"权限才可调用
    ```
        
  如何鉴权：
        你需要实现top.lcmatrix.util.permano.IPermissionChecker接口，示例：
        
        @Service
        public class MyPermissionChecker implements IPermissionChecker{
            @Override
            public boolean check(String permission) {
                HttpSession session = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
                User loginUser = (User) session.getAttribute("LOGIN_USER");
                if(loginUser == null){
                    return false;
                }
                if(loginUser.getPermissions() == null){
                    return false;
                }
                for(String p : loginUser.getPermissions()){
                    if(p.equals(permission)){
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onAccept() {
            }

            @Override
            public void onDeny() {
                throw new NoPermissionException();
            }
        }
        
* **cache系列**

  最简注解形式：
    ```java
      //该方法结果将被自动缓存，使用默认的全局缓存器（需要你自己实现，见后文）、缓存时间（10分钟）和key生成规则（方法签名+参数json序列化后的值）
      @Cache
      public Product getProduct(String id) { ... }
    ```
  更复杂的注解形式：
    ```java
      //使用指定的缓存器缓存结果，指定缓存时间为5分钟，指定key的生成规则（用%n$s代替被注解方法的第n（从1开始）个参数（json序列化后的值））
      @Cache(cacher=HashMapCacher.class, ttl=5 * 6000, keyFormatter="PRODUCT_CACHE_%1$s")
      public Product getProduct(String id) { ... }  
    ```
  缓存器，你可以实现多个缓存器，并根据情况自由选择：
  
  缓存器需要实现top.lcmatrix.util.cacheano.ICacher，示例：
  ```java
      @Component  //Component注解，使之可被自动注入
      @Primary    //Primary注解使之成为全局缓存器
      public class MyCacher implements ICacher{
      
        @Override
        public void set(String key, Object value, long ttl) {
          //添加到缓存
          ...
        }

        @Override
        public Object get(String key) {
          //从缓存获取
          ...
        }

        @Override
        public boolean isCached(String key) {
          //是否已被缓存
          ...
        }
      }
  ```
  some-anotations内部提供一个简单的内存缓存器HashMapCacher，其他比如redis缓存器请自己实现。
  
* **log系列**

  最简注解形式：
    ```java
      @LogModule("产品管理")  //将该类所有方法log的module字段指定为“产品管理”
      public class ProductService{
        //自动使用全局logger（需要自己实现，见后文）、“产品管理”作为module、方法名作为action、所有参数作为detail记录日志
        @Log
        public Product createProduct(...) { ... }
      }
    ```
  更复杂的注解形式：
    ```java
      //使用指定的logger记录日志，并指定module，指定action，指定detail的生成规则（用%n$s代替被注解方法的第n（从1开始）个参数（json序列化后的值））
      @Log(logger=LogServerLogger.class ,module="产品管理", action="添加产品", detail="产品名称：%1$s")
      public Product createProduct(...) { ... }
      
      @LogIgnore  //注解于方法参数，使用默认规则生成detail时将自动忽略该参数
    ```
  logger，你可以实现多个logger，并根据情况自由选择：
  
  logger需要实现top.lcmatrix.util.logano.ILogger，示例：
  ```java
      @Component  //Component注解，使之可被自动注入
      @Primary    //Primary注解使之成为全局logger
      public class SysLogger implements ILogger{
        private static Executor executor = Executors.newCachedThreadPool();   //建议异步保存日志

        @Override
        public void log(String module, String action, String detail) {
          executor.execute(new Runnable() {

            @Override
            public void run() {
              //保存日志，如保存到数据库、文件，或发送到日志服务器
              ...
            }
          });
        }

      }
  ```
  
### 如何引入：

1. 依赖AspectJ、Spring core和fastjson，所以你可能需要先引入这些依赖，如果你用SpringBoot，可以在pom.xml中增加：

        <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>1.2.44</version>
        </dependency>
        
2. 最简单的方式是：将some-anotations的几个包直接拷贝到你的项目下。当然也可以直接引入整个工程。注意要确保这些类在spring的组件扫描范围内。
