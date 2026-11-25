package servlet;

import db.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ViewItemsServlet")
public class ViewItemsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String sql = "SELECT item_id, item_name, category, description, "
                   + "location, item_date, report_type, status "
                   + "FROM items ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>View Items</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");

            out.println("<style>");
            out.println("body { background:#eff6ff; font-family:Arial; }");
            out.println(".page { max-width:1200px; margin:40px auto; padding:20px; }");
            out.println("table { width:100%; border-collapse:collapse; background:white; }");
            out.println("th,td { padding:12px; border:1px solid #ddd; text-align:left; }");
            out.println("th { background:#2563eb; color:white; }");
            out.println("tr:nth-child(even) { background:#f8fafc; }");
            out.println(".back { display:inline-block; margin-bottom:20px; "
                    + "padding:10px 15px; background:#2563eb; "
                    + "color:white; text-decoration:none; border-radius:6px; }");
            out.println("</style>");

            out.println("</head>");
            out.println("<body>");

            out.println("<div class='page'>");
            out.println("<h1>All Reported Items</h1>");
            out.println("<a class='back' href='DashboardServlet'>Back to Dashboard</a>");

            out.println("<table>");
            out.println("<tr>");
            out.println("<th>ID</th>");
            out.println("<th>Item Name</th>");
            out.println("<th>Category</th>");
            out.println("<th>Description</th>");
            out.println("<th>Location</th>");
            out.println("<th>Date</th>");
            out.println("<th>Type</th>");
            out.println("<th>Status</th>");
            out.println("</tr>");

            boolean found = false;

            while (rs.next()) {
                found = true;

                out.println("<tr>");
                out.println("<td>" + rs.getInt("item_id") + "</td>");
                out.println("<td>" + escapeHTML(rs.getString("item_name")) + "</td>");
                out.println("<td>" + escapeHTML(rs.getString("category")) + "</td>");
                out.println("<td>" + escapeHTML(rs.getString("description")) + "</td>");
                out.println("<td>" + escapeHTML(rs.getString("location")) + "</td>");
                out.println("<td>" + rs.getDate("item_date") + "</td>");
                out.println("<td>" + escapeHTML(rs.getString("report_type")) + "</td>");
                out.println("<td>" + escapeHTML(rs.getString("status")) + "</td>");
                out.println("</tr>");
            }

            if (!found) {
                out.println("<tr><td colspan='8'>No items reported yet.</td></tr>");
            }

            out.println("</table>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Unable to retrieve items.");
        }
    }

    private String escapeHTML(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}