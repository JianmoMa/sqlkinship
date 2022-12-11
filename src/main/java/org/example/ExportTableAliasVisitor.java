package org.example;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

public class ExportTableAliasVisitor extends PGASTVisitorAdapter {
    private Map<String, SQLTableSource> aliasMap = new HashMap<String, SQLTableSource>();
    public boolean visit(SQLExprTableSource x) {
        String alias = x.getAlias();
        aliasMap.put(alias, x);
        return true;
    }

    public Map<String, SQLTableSource> getAliasMap() {
        return aliasMap;
    }
}