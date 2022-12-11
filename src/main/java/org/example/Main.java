package org.example;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String dbType = JdbcConstants.POSTGRESQL; // JdbcConstants.MYSQL或者JdbcConstants.POSTGRESQL
        String sql = "insert into new_table(id_new,name_new)  " +
                " select id,name from mytable a where a.id = 3; " +
                " insert into new_table_1(id_1,name_1) " +
                " select dt_1.id_new,dt_1.name_new from new_table dt_1 left join mytable a on a.id=dt_1.id;" +
                " create table temp_table_3 as " +
                " (select dt_1.id code,dt_1.name code_name from new_table_1 dt_1 left join new_table a on a.id=dt_1.id);" +
                " insert into result_dt(code,code_name) " +
                " select code,name from  temp_table_3 " +
                " union " +
                " select id code,name from mytable a where a.id = 4;";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);

        String database="default";

        for (SQLStatement stmt : stmtList) {
            SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(JdbcConstants.POSTGRESQL);
            if (stmt instanceof SQLUseStatement) {
                database = ((SQLUseStatement) stmt).getDatabase().getSimpleName().toUpperCase();
            }
            stmt.accept(statVisitor);
            System.out.println(SQLUtils.toPGString(stmt));

            Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
            Collection<TableStat.Column> columns= statVisitor.getColumns();
            if (tables != null) {
                final String db = database;
                tables.forEach((tableName, stat) -> {
                    if (stat.getCreateCount() > 0 || stat.getInsertCount() > 0) {
                        String to = tableName.getName().toLowerCase();
                        System.out.println("to: "+to);
                        columns.stream().filter(dc-> Objects.equals(dc.getTable().toLowerCase(),tableName.getName().toLowerCase())).forEach(
                                dc->{
                                    System.out.println("to: "+dc.getTable().toLowerCase()+"."+dc.getName().toLowerCase());
                                }
                        );
                    } else if (stat.getSelectCount() > 0) {
                        String from = tableName.getName().toLowerCase();
                        System.out.println("from: "+from);
                        columns.stream().filter(dc-> Objects.equals(dc.getTable().toLowerCase(),tableName.getName().toLowerCase())&&dc.isSelect()).forEach(
                                dc->{
                                    System.out.println("select : "+ dc.getTable().toLowerCase()+"."+dc.getName().toLowerCase());
                                }
                        );
                    }
                });
            }
        }
    }
}