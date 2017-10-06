package servlets;

import accounts.UserDAO;
import accounts.UserProfile;
import utils.FSUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        resp.setContentType("text/html;charset=utf-8");
        if(session.getAttribute("user") == null){
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            HashMap<String, Object> data = new HashMap<>();
            data.put("hasErrors", false);
            resp.getWriter().print(FSUtils.getInstance().getFile(FSUtils.signIn, data));
        }else{
            resp.sendRedirect("/date");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html;charset=utf-8");
        List<String> err = new ArrayList<>();
        UserDAO userDAO = (UserDAO)req.getServletContext().getAttribute("UserDAO");
        if(req.getParameter("login") != null && req.getParameter("password") != null){
            String login = req.getParameter("login");
            String password = req.getParameter("password");
            if(login.length() < 2 || login.length() >= 20) err.add("логин должен быть меньше 20 и больше 2-х символолв!");
            if(password.length() < 2 || login.length() >= 255) err.add("пароль должен быть больше 2-х символов!");
            try {
                UserProfile user = userDAO.getUserByName(login);
                if(user != null)
                    if(user.getPassword().equals(password)){
                        req.getSession().setAttribute("user", user);
                        resp.sendRedirect("/date");
                        return;
                    }else
                        err.add("пользователь с таким именем уже существует!");
            } catch (SQLException e) {
                req.getRequestDispatcher("error500").forward(req, resp);
            }
            if(err.size() == 0){
                try {
                    UserProfile user = new UserProfile(login, password);
                    userDAO.createUser(user);
                    req.getSession().setAttribute("user", user);
                } catch (Exception e) {
                    req.getRequestDispatcher("error500").forward(req, resp);
                }
                resp.sendRedirect("/date");
                return;
            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("errors", err);
            data.put("hasErrors", true);
            resp.getWriter().print(FSUtils.getInstance().getFile(FSUtils.signIn, data));
        }else{
            resp.getWriter().print(FSUtils.getInstance().getFile(FSUtils.signIn, new HashMap<String, Object>(){
                {put("hasErrors", false);}
            }));
        }
    }
}
