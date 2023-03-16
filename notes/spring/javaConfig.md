- 通过xml将bean交给spring管理
- 通过JavaConfig将bean交给spring管理
- 通过xml + @Component注解 将bean交给spring管理？
- 通过JavaConfig + @Component注解 将bean交给spring管理？
- xml + SpringTest
--------
> 以前通常使用Spring都会使用xml对Spring进行配置，但大量的xml会变得复杂。
> 随spring 3.0的发布，Spring IO团队之间开始摆脱XML配置文件
> 在开发过程中大量使用“约定优先配置（convention over configuration）”的思想。
> 1.0: xml，2.0：注解，3.0：JavaConfig，4.0：自动配置（基于JavaConfig）
> Spring 3起，JavaConfig功能已在Spring核心模块。
> 它允许开发者将bean的定义和Spring的配置编写到java类中，是xml方式的一种替代解决方案，也叫Annotation配置。


## Ioc配置 控制反转，交给spring控制
#### 新建一个spring工程
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.9</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.huawei.simx</groupId>
    <artifactId>yq-test</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>yq-test</name>
    <description>yq-test</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```
### 1. 怎么通过xml将bean交给spring管理？

#### 新建一个bean类
```
package com.yq.test.yqtest.ioc;

/**
 * @author l00522851
 * @date 2023/3/14 21:53
 * @description SomeBean.java
 * @since 2023/3/14
 */
public class SomeBean {

}
```

#### 在resource目录，新建一个xml配置applicationContext.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean class="com.yq.test.yqtest.ioc.SomeBean">
        
    </bean>
</beans>
```
#### 新建一个测试类，做测试
```
package com.yq.test.yqtest.ioc;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author l00522851
 * @date 2023/3/14 21:59
 * @description App.java
 * @since 2023/3/14
 */
public class App {
    @Test
    public void test() {
        // Spring容器对象，BeanFactory ApplicationContext
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        SomeBean bean = ctx.getBean(SomeBean.class);
        System.out.println(bean);
    }
}
```
### 2. JavaConfig的方式
#### 新建一个bean类
```
package com.yq.test.yqtest.ioc;

/**
 * @author l00522851
 * @date 2023/3/14 21:53
 * @description SomeBean.java
 * @since 2023/3/14
 */
public class OtherBean {

}
```

#### 新建一个配置类
> @Configuration: 这个注解贴在类上表示这个类是一个配置类
> 这样将xml配置转移到了java代码中，相当于ApplicationContext.xml
> @Bean: 相当于bean标签，spring会把贴了bean注解方法的返回值交给spring容器去管理
```
package com.yq.test.yqtest.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author l00522851
 * @date 2023/3/14 22:05
 * @description JavaConfig.java
 * @since 2023/3/14
 */
@Configuration
public class JavaConfig {

    @Bean
    public SomeBean someBean() {
        return new SomeBean();
    }
}

```

#### 测试
```
    @Test
    public void testBean() {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(JavaConfig.class);
        SomeBean bean = ctx.getBean(SomeBean.class);
        System.out.println(bean);
    }
```

### 3.扫描器的方式
> @Component
```
package com.yq.test.yqtest.ioc02;

import org.springframework.stereotype.Component;

/**
 * @author l00522851
 * @date 2023/3/14 21:53
 * @description SomeBean.java
 * @since 2023/3/14
 */
@Component
public class SomeBean {

}
```
```
package com.yq.test.yqtest.ioc02;

import org.springframework.stereotype.Component;

/**
 * @author l00522851
 * @date 2023/3/14 21:53
 * @description SomeBean.java
 * @since 2023/3/14
 */
@Component
public class OtherBean {

}
```
#### 使用xml的方式 + component scan
> <context:component-scan base-package=""></context:component-scan>
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">
    <context:component-scan base-package="com.yq.test.yqtest.ioc02"></context:component-scan>
</beans>
```
```
    @Test
    public void test1() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        SomeBean someBean = ctx.getBean(SomeBean.class);
        OtherBean otherBean = ctx.getBean(OtherBean.class);
        System.out.println(someBean);
        System.out.println(otherBean);
    }
```
#### 使用javaConfig的方式
> @ComponentScan + @Configuration
> @ComponentScan 会扫描指定目录下的类，如果类贴了spring注解，就会把它交给spring容器管理
> **如果没有写明指定的目录，那么表示要扫描配置类所在的包及其子包**
```
package com.yq.test.yqtest.ioc02;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author l00522851
 * @date 2023/3/14 22:18
 * @description JavaConfig.java
 * @since 2023/3/14
 */
@Configuration
@ComponentScan(basePackages = "com.yq.test.yqtest.ioc02")
public class JavaConfig {

}
```

```
    @Test
    public void test2() {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(JavaConfig.class);
        OtherBean bean = ctx.getBean(OtherBean.class);
        System.out.println(bean);
    }
```
### xml + SpringTest
```
// 在测试类的 @RunWith下
// 添加注解
@ContextConfiguration("classpath:applicationContext.xml")
// 结合@Resource / @Autowire 去使用bean
```
### JavaConfig + SpringTest
```
// 在测试类上添加注解 指定@Configuration的配置类
@ContextConfiguration(classes = JavaConfig.class)
```
