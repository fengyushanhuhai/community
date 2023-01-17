package com.nowcoder.community;

import com.nowcoder.community.Dao.AlphaDao;
import com.nowcoder.community.Service.AlphaService;
import com.nowcoder.community.controller.AlphaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)	// 在测试类中可以启动配置类
class CommunityApplicationTests implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	// 某一个类实现 ApplicationContext这个接口（继承于beanFactory这个顶层 就是spring容器）以及这个set方法，
	// 当程序运行时，那么就会自动将容器传入进来
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;	// 记录这个spring容器 applicationContext
	}

	@Test
	public void testApplicationContext(){	// 测试spring容器
		System.out.println(applicationContext);	// 可以证明当前容器是存在的
		// 依赖注入的好处 可以降低bean的耦合度
		AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);	// 通过类型获取bean(就是类)
		System.out.println(alphaDao.select());

		alphaDao = applicationContext.getBean("alphaHibernate", AlphaDao.class);
		System.out.println(alphaDao.select());

		AlphaController controller = applicationContext.getBean(AlphaController.class);
		System.out.println(controller.sayHello());

		AlphaService service = applicationContext.getBean(AlphaService.class);
		System.out.println(service);


	}

	@Test
	public void testConfig(){
		SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}
}
