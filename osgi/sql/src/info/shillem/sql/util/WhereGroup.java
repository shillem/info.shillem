package info.shillem.sql.util;

import info.shillem.sql.util.SelectQuery.LWhere;

public class WhereGroup implements IWhere {

    private final LWhere wheres;

    public WhereGroup() {
        wheres = new LWhere();
    }

    @Override
    public String output(Schema schema) {
        return wheres.isEmpty()
                ? ""
                : "(".concat(wheres.output(schema)).concat(")");
    }

    public LWhere wheres() {
        return wheres;
    }

}
