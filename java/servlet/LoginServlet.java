package servlet;

import dao.UserDAO;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO dao = new UserDAO();

        try {
            boolean valid = dao.loginUser(email, password);

            if (valid) {

                int userId = dao.getUserIdByEmail(email);
                String role = dao.getUserRole(email);

                HttpSession session = request.getSession();

                session.setAttribute("email", email);
                session.setAttribute("user_id", userId);
                session.setAttribute("role", role);

                if ("ADMIN".equalsIgnoreCase(role)) {
                    response.sendRedirect("AdminDashboardServlet");
                } else {
                    response.sendRedirect("DashboardServlet");
                }

            } else {
                response.sendRedirect("index.html?error=invalid");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("index.html?error=server");
        }
    }
}