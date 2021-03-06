/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

/**
 * Created because Ebean doesn't work in one annoying aspect:
 * When I try to join the Entry and Company tables so that I can do
 * proper ordering and so on, well I can get them joined. But I can't
 * insert new entries with the company_id field being filled in.
 */
public class WorkOrderList extends BaseList<WorkOrder> implements Comparator<WorkOrder> {

    protected Client client;
    protected Integer uploadId = null;

    public WorkOrderList() {
        super();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUploadId(Integer uploadId) { this.uploadId = uploadId; }

    public Integer getUploadId() {
        return uploadId;
    }

    public String getClientName() {
        if (client == null) {
            return "";
        }
        return client.name;
    }

    public String getProjectName() {
        List<WorkOrder> list = getList();
        if (list.size() > 0) {
            return list.get(0).getProjectLine();
        }
        return "";
    }

    protected ExpressionList<WorkOrder> getQuery() {
        ExpressionList<WorkOrder> query = WorkOrder.find.where();
        if (client != null && !client.is_admin) {
            query = query.eq("client_id", client.id);
        }
        if (uploadId != null && uploadId > 0) {
            query = query.eq("upload_id", uploadId);
        }
        return query;
    }

    @Override
    protected List<WorkOrder> getOrderedList() {
        ExpressionList<WorkOrder> query = getQuery();
        return query.orderBy(getOrderBy()).findList();
    }

    @Override
    protected List<WorkOrder> getRawList() {
        return getQuery().findList();
    }

    @Override
    protected void sort(List<WorkOrder> list) {
        list.sort(this);
    }

    @Override
    protected long getProjectId(WorkOrder entry) {
        return entry.project_id;
    }

    @Override
    protected String getCompanyName(WorkOrder entry) { return entry.getCompanyName(); }

    public void computeFilters() {
        super.computeFilters(client);
    }

    public int compare(WorkOrder o1, WorkOrder o2) {
        int value;
        if (mNextParameters.sortBy == SortBy.TRUCK_NUMBER) {
            value = o1.getTruckNumber().compareTo(o2.getTruckNumber());
        } else if (mNextParameters.sortBy == SortBy.TRUCK_LINE) {
            value = o1.getTruckLine().compareTo(o2.getTruckLine());
        } else if (mNextParameters.sortBy == SortBy.PROJECT) {
            value = o1.getProjectLine().compareTo(o2.getProjectLine());
        } else if (mNextParameters.sortBy == SortBy.CLIENT) {
            Client l1 = Client.get(o1.client_id);
            Client l2 = Client.get(o2.client_id);
            if (l1 != null && l2 != null && l1.name != null && l2.name != null) {
                value = l1.name.compareTo(l2.name);
            } else if (l1 != null && l1.name != null) {
                value = -1;
            } else if (l2 != null && l2.name != null) {
                value = 1;
            } else {
                value = 0;
            }
        } else {
            Company c1 = Company.get(o1.company_id);
            Company c2 = Company.get(o2.company_id);
            switch (mNextParameters.sortBy) {
                case COMPANY:
                    String c1name = c1.getName();
                    String c2name = c2.getName();
                    if (c1name != null && c2name != null) {
                        value = c1name.compareTo(c2name);
                    } else if (c1name != null) {
                        value = -1;
                    } else {
                        value = 1;
                    }
                    break;
                case STATE:
                    if (c1.state != null && c2.state != null) {
                        value = c1.state.compareTo(c2.state);
                    } else if (c1.state != null) {
                        value = -1;
                    } else {
                        value = 1;
                    }
                    break;
                case CITY:
                    if (c1.city != null && c2.city != null) {
                        value = c1.city.compareTo(c2.city);
                    } else if (c1.city != null) {
                        value = -1;
                    } else {
                        value = 1;
                    }
                    break;
                case STREET:
                    if (c1.street != null && c2.street != null) {
                        value = c1.street.compareTo(c2.street);
                    } else if (c1.street != null) {
                        value = -1;
                    } else {
                        value = 1;
                    }
                    break;
                case ZIPCODE:
                    if (c1.zipcode != null && c2.zipcode != null) {
                        value = c1.zipcode.compareTo(c2.zipcode);
                    } else if (c1.zipcode != null) {
                        value = -1;
                    } else {
                        value = 1;
                    }
                    break;
                default:
                    value = 0;
                    break;
            }
        }
        if (mNextParameters.order == Order.DESC) {
            value *= -1;
        }
        return value;
    }
}
