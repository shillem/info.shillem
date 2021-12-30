package info.shillem.sql.util;

import info.shillem.sql.util.SelectQuery.LWhere;

public class WhereGroup extends AWhere {

    private final LWhere wheres;

    public WhereGroup() {
        wheres = new LWhere();
    }

    @Override
    public String output() {
        return wheres.isEmpty()
                ? ""
                : "(".concat(wheres.output()).concat(")");
    }

    @Override
    protected void link(SelectQuery select) {
        wheres.link(select);
    }

    public LWhere wheres() {
        return wheres;
    }

}
