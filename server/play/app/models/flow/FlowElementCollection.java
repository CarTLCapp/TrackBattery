/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.validation.*;
import modules.AmazonHelper;

import java.io.File;
import models.Note;

import play.db.ebean.Transactional;
/**
 * A list of flow elements for a flow managed by Ebean
 */
@Entity
public class FlowElementCollection extends Model implements Comparable<FlowElementCollection> {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long flow_id;

    @Constraints.Required
    public Long flow_element_id;

    public static Finder<Long, FlowElementCollection> find = new Finder<Long, FlowElementCollection>(FlowElementCollection.class);

    public static List<FlowElementCollection> list() {
        return find.all();
    }

    public static FlowElementCollection get(long id) {
        return find.byId(id);
    }

    public static int getNumElements(long flow_id) {
        return find.where().eq("flow_id", flow_id).findRowCount();
    }

    public static int getSubFlowCount(long flow_id) {
        int count = 0;
        List<FlowElement> elements = findElementsByFlowId(flow_id);
        for (FlowElement element : elements) {
            if (element.getPromptType() == PromptType.SUB_FLOW_DIVIDER) {
                count++;
            }
        }
        if (count > 0) {
            if (elements.get(0).getPromptType() != PromptType.SUB_FLOW_DIVIDER) {
                count++;
            }
        }
        return count;
    }

    public static List<FlowElementCollection> findByFlowId(long flow_id) {
        return find.where()
                .eq("flow_id", flow_id)
                .findList();
    }

    public static List<FlowElement> findByFlowIdAndLineNumber(long flow_id, int line_number) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        List<FlowElement> list = new ArrayList<FlowElement>();
        for (FlowElementCollection item : items) {
            FlowElement element = item.getFlowElement();
            if (element.line_num == line_number) {
                list.add(item.getFlowElement());
            }
        }
        return list;
    }

    public static List<FlowElement> findElementsByFlowId(long flow_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        List<FlowElement> list = new ArrayList<FlowElement>();
        for (FlowElementCollection item : items) {
            list.add(item.getFlowElement());
        }
        Collections.sort(list);
        if (list.size() > 0) {
            list.get(0).isFirst = true;
            list.get(list.size()-1).isLast = true;
        }
        return list;
    }

    @Transactional
    public static void renumber(long flow_id, long newly_moved_id) {
        FlowElement newly_moved = FlowElement.get(newly_moved_id);
        List<FlowElement> hasLineNumber = findByFlowIdAndLineNumber(flow_id, newly_moved.line_num);
        removeFromList(hasLineNumber, newly_moved_id);
        ArrayList<FlowElement> elements = new ArrayList<FlowElement>(findElementsByFlowId(flow_id));
        for (FlowElement element : hasLineNumber) {
            removeFromList(elements, element.id);
        }
        Collections.sort(elements);
        int line_num = 1;
        for (FlowElement element : elements) {
            element.line_num = line_num++;
            if (element.id == newly_moved_id) {
                for (FlowElement place_below : hasLineNumber) {
                    place_below.line_num = line_num++;
                    place_below.update();
                }
            }
            element.update();
        }
    }

    private static void removeFromList(List<FlowElement> list, long element_id) {
        for (FlowElement element : list) {
            if (element.id == element_id) {
                list.remove(element);
                break;
            }
        }
    }

    @Transactional
    public static void deleteByFlowId(long flow_id) {
        List<FlowElementCollection> items = find.where()
                .eq("flow_id", flow_id)
                .findList();
        for (FlowElementCollection item : items) {
            FlowNoteCollection.deleteByFlowElementId(item.flow_element_id);
            FlowElement element = item.getFlowElement();
            item.delete();
            element.delete();
        }
    }

    public static void deleteByFlowElementId(long flow_element_id) {
        List<FlowElementCollection> items = find.where()
                .eq("flow_element_id", flow_element_id)
                .findList();
        for (FlowElementCollection item : items) {
            item.delete();
        }
    }

    public static boolean hasFlowElement(long flow_id, long flow_element_id) {
        return find.where()
                .eq("flow_id", flow_id)
                .eq("flow_element_id", flow_element_id)
                .findRowCount() > 0;
    }

    public static void create(long flow_id, long flow_element_id) {
        FlowElementCollection flowElementCollection = new FlowElementCollection();
        flowElementCollection.flow_id = flow_id;
        flowElementCollection.flow_element_id = flow_element_id;
        flowElementCollection.save();
    }

    public static int getNextLineNumber(long flow_id) {
        List<FlowElement> items = findElementsByFlowId(flow_id);
        int last_line_number = 0;
        for (FlowElement item : items) {
            if (item.line_num > last_line_number) {
                last_line_number = item.line_num;
            }
        }
        return last_line_number + 1;
    }

    public FlowElement getFlowElement() {
        return FlowElement.get(flow_element_id);
    }

    @Transactional
    public static boolean moveUp(long flow_id, long flow_element_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        Collections.sort(items);
        int element_pos = -1;
        int pos = 0;
        for (FlowElementCollection element : items) {
            if (element.id == flow_element_id) {
                element_pos = pos;
                break;
            }
            pos++;
        }
        if (element_pos < 0) {
            return false;
        }
        if (element_pos <= 0) {
            return false;
        }
        FlowElementCollection moving_element_holder = items.get(element_pos);
        FlowElement moving_element = moving_element_holder.getFlowElement();
        FlowElementCollection previous_element_holder = items.get(element_pos-1);
        FlowElement previous_element = previous_element_holder.getFlowElement();
        int saved_line_number = moving_element.line_num;
        moving_element.line_num = previous_element.line_num;
        previous_element.line_num = saved_line_number;
        moving_element.update();
        previous_element.update();
        return true;
    }

    @Transactional
    public static boolean moveDown(long flow_id, long flow_element_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        Collections.sort(items);
        int element_pos = -1;
        int pos = 0;
        for (FlowElementCollection element : items) {
            if (element.id == flow_element_id) {
                element_pos = pos;
                break;
            }
            pos++;
        }
        if (element_pos < 0) {
            return false;
        }
        if (element_pos >= items.size()-1) {
            return false;
        }
        FlowElementCollection moving_element_holder = items.get(element_pos);
        FlowElement moving_element = moving_element_holder.getFlowElement();
        FlowElementCollection next_element_holder = items.get(element_pos+1);
        FlowElement next_element = next_element_holder.getFlowElement();
        int saved_line_number = moving_element.line_num;
        moving_element.line_num = next_element.line_num;
        next_element.line_num = saved_line_number;
        moving_element.update();
        next_element.update();
        return true;
    }

    @Override
    public int compareTo(FlowElementCollection item) {
        return getFlowElement().line_num - item.getFlowElement().line_num;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FlowElementCollection) {
            return equals((FlowElementCollection) other);
        }
        return super.equals(other);
    }

    public boolean equals(FlowElementCollection other) {
        return  getFlowElement().line_num == other. getFlowElement().line_num;
    }
}

