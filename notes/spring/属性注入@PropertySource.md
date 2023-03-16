> 配置数据库连接池的时候
# 传统XML方式
```
<!-- 添加数据库连接的配置文件路径，这个配置文件中的值会被放到spring环境对象中，后续可以用${}取值 -->
<context: property-placeholder location="classpath:db.properties" system-properties-mode="NEVER"/>


  <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
		<property name="driverClassName" value="${hd.mysql.driver}" />
		<property name="url" value="${hd.mysql.url}" />
		<property name="username" value="${hd.mysql.username}" />
		<property name="password" value="${hd.mysql.password}" />
		<!-- 初始化连接大小 -->
		<property name="initialSize" value="${hd.mysql.initialSize}"></property>
		<!-- 连接池最大数量 -->
		<property name="maxActive" value="${hd.mysql.maxActive}"></property>
		<!-- 连接池最大空闲 -->
		<property name="maxIdle" value="${hd.mysql.maxIdle}"></property>
		<!-- 连接池最小空闲 -->
		<property name="minIdle" value="${hd.mysql.minIdle}"></property>
		<!-- 获取连接最大等待时间 -->
		<property name="maxWait" value="${hd.mysql.maxWait}"></property>
		<property name="validationQuery" value="SELECT 1" />
		<property name="testOnBorrow" value="true" />
	</bean>
```

# 配置类的方式
### 方式1：@PropertySource + @Value
```
@Configuration
@PropertySource("classpath:db.properties")
public class AnnotationConfig {
  // 在其他的类中也可以用@Value去获取值
  @Value("${jdbc.url}")
  private String url;
}
```
### 方式2：直接注入Spring的环境对象，Environment
```
@Configuration
@PropertySource("classpath:db.properties")
public class JavaConfig {
  @Autowired
  private Environment environment;
  
  @Bean
  public MyDataSource myDataSource() {
    MyDataSource myDataSource = new MyDataSource();
    myDataSource.setPasswd(environment.getProperty("jdbc.password"));
    return myDataSource;
  }
}
```
