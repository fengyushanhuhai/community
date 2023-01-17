package com.nowcoder.community.Service;

import com.nowcoder.community.Dao.AlphaDao;
import com.nowcoder.community.Dao.MybatisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AlphaService {



    @Autowired
    @Qualifier("alphaHibernate")
    private AlphaDao alphaDao;

    public String useDao(){
        return alphaDao.select();
    }
}
