### 这里有一些注解，作用是：在不破坏原有代码结构的情况下，为项目灵活地增加一些切面功能。目前包含：
* perm系列：简单的接口级权限控制
* cache系列：方法调用结果的自动缓存管理
* log系列：日志存储

### 详细介绍
* perm系列

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
        
* cache系列
        

### 如何引入：

1. 依赖AspectJ和Spring core，所以你可能需要先引入这些依赖，如果你用SpringBoot，可以在pom.xml中增加：

        <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <version>2.0.2.RELEASE</version>
        </dependency>
        
2. 最简单的方式是：将some-anotations的几个包直接拷贝到你的项目下。当然也可以直接引入整个工程。注意要确保这些类在spring的组件扫描范围内。
