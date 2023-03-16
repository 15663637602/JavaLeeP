# xml方式的Bean属性
```
<bean id="xx" name="xx" class="com.yq.test.yqtest.ioc02.SomeBean" init-method="" destroy-method="" scope="">
</bean>
```

# 配置类方式的Bean属性
> @Bean注解中，就有以上bean的属性
> @Scope 注解用于声明作用域，默认是单例，每次getBean只会获取同一个bean对象，不会生成多个bean对象

