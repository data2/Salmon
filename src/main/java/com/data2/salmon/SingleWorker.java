package com.data2.salmon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingleWorker extends QuickService {
    @Override
    public Object execute() {
        try {
            buildFactory.build(database, currSql.setSql(parseConfig.parse(file, currSql)), currParams);
            System.out.println(currSql);
            sqlArr.add(currSql);
            return currSql.exec();
        } catch (Exception e) {
            e.printStackTrace();
            exception = true;
        }
        return currSql.getSqlId();

    }


    @Autowired
    public BuildFactory buildFactory;
    @Autowired
    private ParseConfig parseConfig;
}
