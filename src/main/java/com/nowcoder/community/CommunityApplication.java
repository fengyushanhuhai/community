package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication // 该注解所标识的类表示是一个配置文件
public class CommunityApplication {

	public static void main(String[] args) {
		// 自动创建了spring容器，spring容器会自动扫描配置类所在的包以及子包中的bean
		// 并且bean上必须有@controller(开发处理请求的组件)、@Service(开发业务组件)、@Component(开发的类任何地方都能用)、@Repository(开发数据库访问的组件)
		// 这四个注解其中之一的bean都可以被扫描到
		// 四个注解都是由 @Component所实现的，只不过应用场景不一样
		SpringApplication.run(CommunityApplication.class, args);
	}

}
