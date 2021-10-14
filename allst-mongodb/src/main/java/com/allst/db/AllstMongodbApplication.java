package com.allst.db;

import com.allst.db.repository.LagouRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AllstMongodbApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(AllstMongodbApplication.class, args);
        LagouRepository lagouRepository = applicationContext.getBean(LagouRepository.class);
        /**
         * 访问成功后数据为：
         * [Lagou{id='6160492be6cb68583856e7d8', name='kangkang', birthday=Thu Oct 01 08:00:00 CST 2020, salary=15000.0, city='beijing'},
         * Lagou{id='61604e97245f7a8dc8ee908b', name='xiaohu', birthday=Tue Jun 11 08:00:00 CST 2019, salary=25000.0, city='chongqing'},
         * Lagou{id='61604e97245f7a8dc8ee908c', name='kangshuai', birthday=Sun Nov 11 08:00:00 CST 2018, salary=15000.0, city='xian'},
         * Lagou{id='61644b9cd5324e69b8a726be', name='xiaohuzi', birthday=Tue Mar 07 08:00:00 CST 2000, salary=45000.0, city='guangzhou'}]
         */
        System.out.println(lagouRepository.findAll());
        /**
         * [Lagou{id='6160492be6cb68583856e7d8', name='kangkang', birthday=Thu Oct 01 08:00:00 CST 2020, salary=15000.0, city='beijing'}]
         */
        System.out.println(lagouRepository.findByNameEquals("kangkang"));
    }

}
