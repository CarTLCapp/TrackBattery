package com.cartlc.tracker.data;

/**
 * Created by dug on 5/16/17.
 */

public class DataNote {

    public enum Type {
        NUMERIC,
        ALPHANUMERIC,
        NUMERIC_WITH_SPACES,
        MULTILINE,
        TEXT;

        static Type from(int ord) {
            for (Type value : values()) {
                if (value.ordinal() == ord) {
                    return value;
                }
            }
            return Type.TEXT;
        }
    }

    public long   id;
    public String name;
    public String value;
    public Type   type;

    public DataNote() {

    }


    public DataNote(String name) {
        this.name = name;
    }

    public DataNote(String name, Type type) {
        this.name = name;
        this.type = type;
    }

}
