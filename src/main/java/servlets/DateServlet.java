package servlets;

import utils.DateUtils;
import utils.FSUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getSession(true).getAttribute("user") == null){
            resp.sendRedirect("/");
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html;charset=utf-8");
        Map<String, Object> data = (Map<String, Object>)req.getAttribute("data");
        if(data == null) data = new HashMap<>();
        data.putIfAbsent("hasResolve", false);
        data.putIfAbsent("hasErrors", false);
        Connection connection = (Connection) req.getServletContext().getAttribute("Connection");
        try {
            data.put("dates", DateUtils.getAllDates(connection));
        } catch (SQLException e) {
            req.getRequestDispatcher("error500").forward(req, resp);
        }
        resp.getWriter().print(FSUtils.getInstance().getFile(FSUtils.date, data));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getSession(true).getAttribute("user") == null){
            resp.sendRedirect("/");
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html;charset=utf-8");
        Map<String, Object> data = new HashMap<>();
        if(req.getParameter("newDate") != null){
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setLenient(false);
            try {
                String date = req.getParameter("newDate") + ".1900";
                Date newDate = dateFormat.parse(date);
                Connection connection = (Connection)req.getServletContext().getAttribute("Connection");
                DateUtils.addNewDate(DateUtils.date2SQLDate(newDate), connection);
                doGet(req, resp);
            } catch (ParseException e) {
                data.put("hasErrors", true);
                if(data.get("errors") != null){
                    ((List<String>)data.get("errors")).add("неправильный формат даты(dd.mm)");
                }else{
                    List<String> errors = new ArrayList<>();
                    errors.add("неправильный формат даты(dd.mm)");
                    data.put("errors", errors);
                }
                req.setAttribute("data", data);
                doGet(req, resp);
                return;
            } catch (SQLException e) {
                data.put("hasErrors", true);
                if(data.get("errors") != null){
                    ((List<String>)data.get("errors")).add("такая дата уже добавленна");
                }else{
                    List<String> errors = new ArrayList<>();
                    errors.add("такая дата уже добавленна");
                    data.put("errors", errors);
                }
                req.setAttribute("data", data);
                doGet(req, resp);
                return;
            }
        }
        if(req.getParameter("date1") != null && req.getParameter("date2") != null){
            String data1 = req.getParameter("date1");
            String data2 = req.getParameter("date2");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date dateBegin, dateEnd;
            try {
                simpleDateFormat.setLenient(false);
                dateBegin = simpleDateFormat.parse(data1);
                dateEnd = simpleDateFormat.parse(data2);

            } catch (ParseException e) {
                data.put("hasErrors", true);
                if(data.get("errors") != null){
                    ((List<String>)data.get("errors")).add("неправильный формат даты(dd.mm.yyyy)");
                }else{
                    List<String> errors = new ArrayList<>();
                    errors.add("неправильный формат даты(dd.mm.yyyy)");
                    data.put("errors", errors);
                }
                req.setAttribute("data", data);
                doGet(req, resp);
                return;
            }
            Connection connection = (Connection)req.getServletContext().getAttribute("Connection");
            try {
                int[] holidays;
                holidays = DateUtils.getHolidays(DateUtils.date2SQLDate(dateBegin), DateUtils.date2SQLDate(dateEnd), connection);
                data.put("hasResolve", true);
                data.put("allDays", holidays[0]);
                data.put("holidays", holidays[1]);
                data.put("outWork", holidays[2]);
                req.setAttribute("data", data);
                doGet(req, resp);
            } catch (SQLException e) {
                req.getRequestDispatcher("error500").forward(req, resp);
            }
        }
    }
}
