package com.allst.mysql;

import com.allst.mysql.entity.Position;
import com.allst.mysql.entity.PositionDetail;
import com.allst.mysql.repository.PositionDetailRepository;
import com.allst.mysql.repository.PositionRepository;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author June
 * @since 2021年09月
 */
@SpringBootTest(classes = AllstMysqlApplication.class)
public class ShardingTransactionTests {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionDetailRepository positionDetailRepository;

    /**
     * 在不加入事务注解的情况下， 出现异常依然是会执行成功的
     *
     * TransactionTypeHolder.set(TransactionType.XA)编码与注解@ShardingTransactionType(TransactionType.XA)的效果一致
     *
     * TransactionTypeHolder.set(TransactionType.BASE)编码与注解@ShardingTransactionType(TransactionType.BASE)的效果一致
     */
    @Test
    @Transactional
    public void testTX() {
        TransactionTypeHolder.set(TransactionType.BASE);
        for (int i = 0; i < 5; i++) {
            Position position = new Position();
            position.setCity("xian");
            position.setName("kangkang:" + i);
            position.setSalary("65129.0");
            positionRepository.save(position);
            if (i == 4) {
                throw new RuntimeException("make a exception by person...");
            }
            PositionDetail positionDetail = new PositionDetail();
            positionDetail.setPid(position.getId());
            positionDetail.setDescription("this is " + i + " list position detail record. TX");
            positionDetailRepository.save(positionDetail);
        }
    }
}
