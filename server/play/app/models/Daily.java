/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package models;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Comparable;
import java.lang.System;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import modules.TimeHelper;
/**
 * For each sub-project list the number of entries installed on the indicated date.
 *
 * The count is from 6am on the day requested, until 6am the following day, relative to the local time,
 * not by the time as adjusted by another time zone.
 */
public class Daily {

    private static final int NUMBER_DAYS = 30;
    private static final String DATE_FORMAT = "MM/dd/yy";
    private static final int ADJUST_HOURS = 6;

    private long mStartTime;
    private long mEndTime;
    private Date mDate;
    private TimeHelper timeHelper = new TimeHelper();

    public class ProjectCount implements Comparable<ProjectCount> {

        public String name;
        public int count;

        public ProjectCount(Project project) {
            name = project.name;
            count = Entry.countEntriesForProjectWithinRange(project.id, mStartTime, mEndTime);
        }

        @Override
        public int compareTo(ProjectCount o)
        {
            return name.compareTo(o.name);
        }
    }

    public class RootCount implements Comparable<RootCount> {

        public String name;
        public ArrayList<ProjectCount> projects = new ArrayList<ProjectCount>();
        private int mCount;
        private boolean isBlank;

        public RootCount(RootProject root) {
            if (root == null) {
                name = "NONE";
                isBlank = true;
            } else {
                name = root.name;
            }
            mCount = 0;
            for (Project project : Project.listSubProjects(name)) {
                ProjectCount count = new ProjectCount(project);
                if (count.count > 0) {
                    mCount += count.count;
                    projects.add(count);
                }
            }
            Collections.sort(projects);
        }

        @Override
        public int compareTo(RootCount o)
        {
            if (isBlank) {
                return 1;
            } else if (o.isBlank) {
                return -1;
            }
            return name.compareTo(o.name);
        }
    }

    private class Counts {

        private ArrayList<RootCount> mRoots = new ArrayList<RootCount>();

        public Counts() {
            for (RootProject root : RootProject.list()) {
                RootCount count = new RootCount(root);
                if (count.mCount > 0) {
                    mRoots.add(count);
                }
            }
            Collections.sort(mRoots);
            RootCount blank = new RootCount(null);
            if (blank.mCount > 0) {
                mRoots.add(blank);
            }
        }
    }

    public class DateChoice {
        public String text;
        public long date;

        DateChoice(long time, SimpleDateFormat sdf) {
            date = time;
            text = sdf.format(time);
        }

        public String getSelected() {
            if (date >= mStartTime && date <= mEndTime) {
                return "selected";
            }
            return "";
        }
    }

    private Counts mCounts;

    public Daily() {
        mDate = new Date();
        init();
    }

    public void resetTo(long date) {
        if (date == 0) {
            mDate = new Date();
        } else {
            mDate = new Date(date);
        }
        init();
    }

    private void init() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        calendar.set(Calendar.HOUR, ADJUST_HOURS);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        mStartTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        mEndTime = calendar.getTimeInMillis();
        mCounts = new Counts();
    }

    public List<RootCount> getRootProjects() {
        return mCounts.mRoots;
    }

    public long getDate() {
        return mDate.getTime();
    }

    public List<DateChoice> getDates() {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        ArrayList<DateChoice> dates = new ArrayList<DateChoice>();

        // Compute first day
        calendar.add(Calendar.DAY_OF_YEAR, NUMBER_DAYS/2);
        for (int day = 0; day < NUMBER_DAYS; day++) {
            if (calendar.getTimeInMillis() <= now) {
                break;
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        // Now add the days
        while (dates.size() < NUMBER_DAYS) {
            dates.add(new DateChoice(calendar.getTimeInMillis(), sdf));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return dates;
    }

}