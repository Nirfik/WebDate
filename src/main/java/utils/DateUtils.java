package utils;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {

    public static int dateDiff(Date date1, Date date2, Connection connection) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("select datediff(?, ?)")){
            preparedStatement.setDate(1, date1);
            preparedStatement.setDate(2, date2);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) + 1;
        }
    }

    public static void addNewDate(Date date, Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()){
            statement.execute("insert into holidays values('"+date.toString()+"')");
        }
    }

    public static List<String> getAllDates(Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()){
            List<String> list = new LinkedList<>();
            ResultSet resultSet = statement.executeQuery("select * from holidays order by date");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM");
            while(resultSet.next()){
                String format = dateFormat.format(new java.util.Date(resultSet.getDate(1).getTime()));
                list.add(format);
            }
            return list;
        }
    }

    public static int date2Number(Date date, Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery("select datediff('" + date.toString() + "', '1900-01-01')");
            resultSet.next();
            return resultSet.getInt(1) + 1;
        }

    }

    public static int getHolidaysByDateRange(Date dateBegin, Date dateEnd, Connection connection) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("select count(date) from holidays where date >= ? and date <= ?")){
            preparedStatement.setDate(1, dateBegin);
            preparedStatement.setDate(2, dateEnd);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public static Date getDateByName(String name){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            java.util.Date parse = dateFormat.parse(name);
            return date2SQLDate(parse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static int getAllHolidays(Connection connection) throws SQLException {
        Date dateBegin = getDateByName("01.01.1900");
        Date dateEnd = getDateByName("01.01.1901");
        return getHolidaysByDateRange(dateBegin, dateEnd, connection);
    }

    public static Date date2SQLDate(java.util.Date date){
        return new Date(date.getTime());
    }

    public static int getOutWork(int numBegin, int numEnd){
        int days = numEnd - numBegin + 1;
        int outWork = days / 7 * 2;
        if(days % 7 == 0) return outWork;
        if(numBegin % 7 == 0) return outWork + 1;
        if(numBegin % 7 + (days - 1) % 7 < 6) return outWork;
        if(numBegin % 7 + (days - 1) % 7 == 6) return outWork + 1;
        return outWork + 2;
    }

    public static int[] getHolidays(Date dateBegin, Date dateEnd, Connection connection) throws SQLException {
        int holidays = 0;
        int numBegin = date2Number(dateBegin, connection);
        int numEnd = date2Number(dateEnd, connection);
        if(numBegin > numEnd)
            return getHolidays(dateEnd, dateBegin, connection);
        int outWork = getOutWork(numBegin, numEnd);
        Calendar calendarBegin = new GregorianCalendar();
        calendarBegin.setTime(dateBegin);
        Calendar calendarEnd = new GregorianCalendar();
        calendarEnd.setTime(dateEnd);
        int yearDiff = calendarEnd.get(Calendar.YEAR) - calendarBegin.get(Calendar.YEAR) + 1;
        holidays += yearDiff * getAllHolidays(connection);
        holidays -= getHolidaysByDateRange(getDateByName(calendarBegin.get(Calendar.DATE) + "." +
                calendarBegin.get(Calendar.MONTH) + ".1900"), getDateByName("01.01.1901"), connection);
        holidays -= getHolidaysByDateRange(getDateByName("01.01.1900"), getDateByName(calendarEnd.get(Calendar.DATE) + "." +
        calendarEnd.get(Calendar.MONTH) + ".1900"), connection);
        return new int[]{numEnd - numBegin + 1, holidays, outWork};
    }
}
