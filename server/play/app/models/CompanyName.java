/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

import modules.DataErrorException;

/**
 * Company entity managed by Ebean
 */
@Entity
public class CompanyName extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public boolean disabled;

    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long, CompanyName> find = new Finder<Long, CompanyName>(CompanyName.class);

    public static List<CompanyName> list() {
        return find.where()
                .eq("disabled", false)
                .orderBy("name ASC")
                .findList();
    }

    public static ArrayList<String> listNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (CompanyName companyName : list()) {
            names.add(companyName.name);
        }
        return names;
    }

    public static ArrayList<String> listNamesWithBlank() {
        ArrayList<String> names = listNames();
        names.add(0, "");
        return names;
    }

    public String idString() {
        return "C" + id;
    }

    public static String get(long id) {
        if (id > 0) {
            CompanyName company = find.byId(id);
            if (company != null) {
                return company.name;
            }
        }
        return null;
    }

    public static long save(String name) {
        List<CompanyName> list = find.where().eq("name", name).findList();
        if (list.size() > 0) {
            return list.get(0).id;
        }
        CompanyName company = new CompanyName();
        company.name = name;
        company.save();
        return company.id;
    }

    public static void save(List<String> names) {
        for (String companyName : names) {
            save(companyName);
        }
    }

    public static ArrayList<String> getNames() {
        List<CompanyName> list;
        list = find.where()
                .eq("disabled", false)
                .orderBy("name asc")
                .findList();
        ArrayList<String> names = new ArrayList<String>();
        for (CompanyName company : list) {
            names.add(company.name);
        }
        return names;
    }

    public static List<String> getNamesWithBlank() {
        ArrayList<String> names = getNames();
        names.add(0, "");
        return names;
    }

    public static void delete(long id) {
        find.ref(id).delete();
    }

}

