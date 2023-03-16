> 一个项目中可能有多个spring的配置文件，配置不同的组件，如springMvc.xml -》 applicationContext.xml -》 mybatis.xml等等
# 传统xml的方式
#### 在mvc.xml中导入application.xml
```
<!-- 在mvc.xml中导入application.xml -->
<import resource="classpath:applicationContext.xml" />
```
#### 在web.xml中配置
```
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:spring-mybatis.xml</param-value>
  </context-param>
  <servlet>
    <servlet-name>spring</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
```
# JavaConfig的方式 - @Import
> 假如存在多个配置类，可以用@Import在主配置类中关联分支配置类

```
// 主配置类
@Configuration
@Import(OtherConfig.class)
public class MainConfig {
  ...
}
// 分支配置类
@Configuration
public class OtherConfig {
  ...
}
```
