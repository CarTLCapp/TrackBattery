/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package views.formdata;

import com.avaje.ebean.Model;

/**
 * Input many lines at once
 */
public class InputLines extends Model {

    public String [] lines;

    public String line;

    public InputLines() {
    }

    public InputLines(String input) {
        if (input == null) {
            line = "";
        } else {
            line = input;
        }
        lines = line.split("\\n");
    }

    public String [] getLines() {
        if (line != null) {
            lines = line.split("\\n");
        }
        return lines;
    }

}

