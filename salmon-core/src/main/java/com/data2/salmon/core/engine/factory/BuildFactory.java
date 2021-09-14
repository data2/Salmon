package com.data2.salmon.core.engine.factory;

import com.data2.salmon.core.common.util.ArrUtils;
import com.data2.salmon.core.engine.config.Parser;
import com.data2.salmon.core.engine.config.PatternMatcherUnit;
import com.data2.salmon.core.engine.config.TableConfig;
import com.data2.salmon.core.engine.domain.ExecuteSql;
import com.data2.salmon.core.engine.domain.Looker;
import com.data2.salmon.core.engine.enums.DataBase;
import com.data2.salmon.core.engine.enums.OperationKeys;
import com.data2.salmon.core.engine.enums.Pair;
import com.data2.salmon.core.engine.except.SalmonException;
import com.data2.salmon.core.engine.strategy.Router;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.SQLException;

/**
 * @author data2
 */
@Slf4j
@Component
public class BuildFactory {

    @Autowired
    public ConnectFactory connectFactory;

    @Autowired
    public Router router;

    /**
     * search for tab name.
     *
     * @param sql
     * @return
     */
    private String calcuTabnameFromSqlStr(String sql) {
        String[] res = PatternMatcherUnit.split(sql, "\\s+");
        boolean tag = false;
        for (String sub : res) {
            if (ArrUtils.inArray(sub.trim().toUpperCase(), OperationKeys.INTO.name(), OperationKeys.DELETE.name(), OperationKeys.UPDATE.name(), OperationKeys.FROM.name())) {
                tag = true;
                continue;
            }
            if (tag) {
                if (!StringUtils.isEmpty(sub.trim())) {
                    return sub;
                }
            }
        }

        return null;
    }

    /**
     * if partition
     * loading ruler.
     * calcu ruler for where data goes.
     *
     * @param dbase
     * @param currSql
     * @param params
     * @throws SalmonException
     */
    public Object build(DataBase dbase, ExecuteSql currSql, Object params) throws SalmonException, SQLException {
        currSql.setTableName(calcuTabnameFromSqlStr(currSql.getSql()));
        String dbId = null;
        if (dbase == DataBase.partition) {
            loadFromConfig(currSql);
            if (params == null) {
                log.error("partition need value not null");
            }
            dbId = Parser.parse(currSql.getRuler(), params);
        }
        return connectFactory.setSql(currSql).makeConnect(new Looker(dbase, dbId)).preparedStmt(params).exec();

    }

    /**
     * decorate sql.
     *
     * @param currSql
     */
    private void loadFromConfig(ExecuteSql currSql) {
        TableConfig tabRuler = router.config(currSql.getTableName());
        currSql.setRuler(tabRuler);
        currSql.setPartionKey(new Pair(tabRuler.getColumn(), null));
    }

}
