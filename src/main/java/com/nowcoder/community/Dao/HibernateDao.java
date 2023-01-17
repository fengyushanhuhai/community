package com.nowcoder.community.Dao;

import org.springframework.stereotype.Repository;

@Repository("alphaHibernate")       // 用于spring 扫描访问数据库的组件,并且可以给bean起一个名字
public class HibernateDao implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}
