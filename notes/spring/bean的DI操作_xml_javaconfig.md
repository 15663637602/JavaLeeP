> 将otherBean注入到someBean中

```
public class SomeBean {
    private OtherBean otherBean;

    public SomeBean(OtherBean otherBean) {
        this.otherBean = otherBean;
    }
    // 需要有默认构造方法，否则会报错
    public SomeBean() {
    }

    public OtherBean getOtherBean() {
        return otherBean;
    }
    // 需要有setBean方法
    public void setOtherBean(OtherBean otherBean) {
        this.otherBean = otherBean;
    }
}
```
# xml 的方式
```
    <bean class="com.yq.test.yqtest.ioc.SomeBean">
        <property name="otherBean" ref="otherBean"/>
    </bean>

    <bean id="otherBean" class="com.yq.test.yqtest.ioc.OtherBean">

    </bean>
```
# javaconfig方式
## 直接在参数中进行注入，然后调用set方法进行设置
```
    // 前提是otherBean已在spring容器中
    @Bean
    public SomeBean someBean(OtherBean otherBean) {
        SomeBean someBean = new SomeBean();
        someBean.setOtherBean(otherBean);
        return someBean;
    }

    @Bean
    public OtherBean otherBean() {
        return new OtherBean();
    }
```
## 直接调用方法的方式，如下
```
    @Bean
    public SomeBean someBean() {
        SomeBean someBean = new SomeBean();
        someBean.setOtherBean(otherBean());
        return someBean;
    }

    @Bean
    public OtherBean otherBean() {
        return new OtherBean();
    }
```
