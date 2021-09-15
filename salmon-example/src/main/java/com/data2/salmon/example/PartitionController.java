package com.data2.salmon.example;

import com.data2.salmon.core.engine.enums.DataBase;
import com.data2.salmon.core.engine.except.SalmonException;
import com.data2.salmon.core.engine.inter.Mapper;
import com.data2.salmon.core.engine.inter.Salmon;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * @author data2
 * @description
 * @date 2020/7/9 下午4:22
 */
@RestController
public class PartitionController {
    @Autowired(required = false)
    @Qualifier("partitionDao")
    @Mapper(file = "mapper", database = DataBase.partition, name = "partitionDao")
    public Salmon salmon;

    @RequestMapping("/partitioninsert")
    public Object partitioninsert() throws SalmonException, SQLException {
        Map map = Maps.newConcurrentMap();
        map.put("id", "123");
        map.put("name", "王磊");
        Object res = salmon.insert("partitioninsert").execute(map);
        return res;
    }

    @RequestMapping("/partitionquery")
    public Object partitionquery() throws SalmonException, SQLException {
        Object res = salmon.select("partitionquery").execute("123");
        return res;
    }

    @RequestMapping("/partitionquery2")
    public Object partitionquery2() throws SalmonException, SQLException {
        Object res = salmon.select("partitionquery2").execute(Collections.singletonMap("id", "123"));
        return res;
    }
}
