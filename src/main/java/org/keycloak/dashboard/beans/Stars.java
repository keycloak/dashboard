package org.keycloak.dashboard.beans;

import org.keycloak.dashboard.util.DateUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Stars {

    private Date currentDate = new Date();
    private int year;
    private long countYearStart;
    private long countYearTarget;
    private long yearlyGrowth;
    private long startYearMonthlyAdded;
    private List<MonthEntry> months = new LinkedList<>();

    public Stars(int year, long yearlyGrowth, long startYearMonthlyAdded) throws IOException, ParseException {
        this.year = year;
        this.yearlyGrowth = yearlyGrowth;
        this.startYearMonthlyAdded = startYearMonthlyAdded;

        File file = new File("stars");
        BufferedReader br = new BufferedReader(new FileReader(file));
        List<Date> stars = new LinkedList<>();
        for (String l = br.readLine(); l != null; l = br.readLine()) {
            Date date = DateUtil.fromJson(l.split(",")[1]);
            stars.add(date);
        }

        Date yearStart = getMonthStart(year, 1);

        countYearStart = stars.stream().filter(d -> d.before(yearStart)).count();
        countYearTarget = countYearStart + yearlyGrowth;

        double monthlyAverage = (((double) (countYearTarget - countYearStart)) / 12) - startYearMonthlyAdded;

        double c = countYearStart;

        for (int i = 1; i <= 12; i++) {
            Date monthStart = getMonthStart(year, i);
            Date monthEnd = getMonthStart(year, i + 1);

            double monthlyTarget = startYearMonthlyAdded + ((monthlyAverage) / 6.5) * i;

            c += monthlyTarget;

            MonthEntry monthEntry = new MonthEntry(monthStart, monthEnd);

            monthEntry.targetTotalStars = Math.round(c);
            monthEntry.targetAddedStars = Math.round(monthlyTarget);
            monthEntry.addedStars = stars.stream().filter(d -> d.before(monthEnd) && d.after(monthStart)).count();
            monthEntry.totalStars = stars.stream().filter(d -> d.before(monthEnd)).count();

            months.add(monthEntry);
        }
    }

    public int getYear() {
        return year;
    }

    public List<MonthEntry> getMonths() {
        return months;
    }

    private Date getMonthStart(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 1, 1, 1);
        return calendar.getTime();
    }

    public class MonthEntry {

        private Date monthStart;
        private Date monthEnd;

        private int lapsed = -1;
        private long addedStars;
        private long targetAddedStars;

        private long totalStars;
        private long targetTotalStars;

        public MonthEntry(Date monthStart, Date monthEnd) {
            this.monthStart = monthStart;
            this.monthEnd = monthEnd;
        }

        public String getMonth() {
            return DateUtil.monthString(monthStart);
        }

        public long getAddedStars() {
            return addedStars;
        }

        public long getTargetAddedStars() {
            return targetAddedStars;
        }

        public long getTotalStars() {
            return totalStars;
        }

        public long getTargetTotalStars() {
            return targetTotalStars;
        }

        public String getAddedStyles() {
            int lapsed = getLapsed();
            if (lapsed == 0) {
                return "";
            }

            long target = targetAddedStars * lapsed / 100;
            return addedStars >= target ? "success" : "error";
        }

        public String getCountStyles() {
            int lapsed = getLapsed();
            if (lapsed == 0) {
                return "";
            }

            long target = (targetTotalStars - targetAddedStars) + (targetAddedStars * lapsed / 100);
            return totalStars >= target ? "success" : "error";
        }

        public int getLapsed() {
            if (lapsed == -1) {
                if (monthEnd.before(currentDate)) {
                    lapsed = 100;
                } else if (monthStart.after(currentDate)) {
                    lapsed = 0;
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);
                    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    lapsed = calendar.get(Calendar.DAY_OF_MONTH) * 100 / daysInMonth;
                }
            }
            return lapsed;
        }

    }

}
