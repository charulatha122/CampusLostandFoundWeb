package servlet;

import db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AddItemServlet")
public class AddItemServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");

        String itemName = request.getParameter("item_name");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String location = request.getParameter("location");
        String itemDate = request.getParameter("item_date");
        String reportType = request.getParameter("report_type");

        String sql = "INSERT INTO items "
                   + "(user_id, item_name, category, description, "
                   + "location, item_date, report_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, itemName);
            ps.setString(3, category);
            ps.setString(4, description);
            ps.setString(5, location);
            ps.setDate(6, Date.valueOf(itemDate));
            ps.setString(7, reportType);

            int result = ps.executeUpdate();

            if (result > 0) {
                response.sendRedirect("DashboardServlet");
            } else {
                response.getWriter().println("Item was not saved.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<h2>Error while reporting item</h2>");
            response.getWriter().println("<p>Please check the entered details.</p>");
        }
    }
}