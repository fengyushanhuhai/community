package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 起到标识作用
@Target(ElementType.METHOD)         // 方法标识
@Retention(RetentionPolicy.RUNTIME) // 运行时使用
public @interface LoginRequired {
}
