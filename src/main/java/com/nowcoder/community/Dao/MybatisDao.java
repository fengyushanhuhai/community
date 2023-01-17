package com.nowcoder.community.Dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary        // 优先获取该bean
public class MybatisDao implements AlphaDao {
    @Override
    public String select() {

        return "Mybatis";
    }
}
