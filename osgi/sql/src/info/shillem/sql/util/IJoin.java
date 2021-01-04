package info.shillem.sql.util;

public interface IJoin {

    public enum Type {
        INNER_JOIN,
        LEFT_JOIN;
    }

    String output(Schema schema);
    
}
