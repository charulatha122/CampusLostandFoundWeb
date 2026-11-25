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

@WebServlet("/SearchItemsServlet")
public class SearchItemsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String keyword = request.getParameter("keyword");

        if (keyword == null) {
            keyword = "";
        }

        String sql = "SELECT item_id, item_name, category, description, "
                   + "location, item_date, report_type, status "
                   + "FROM items "
                   + "WHERE item_name LIKE ? "
                   + "OR category LIKE ? "
                   + "OR location LIKE ? "
                   + "ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             PrintWriter out = response.getWriter()) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {

                out.println("<!DOCTYPE html>");
                out.println("<html><head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<title>Search Items</title>");
                out.println("<link rel='stylesheet' href='css/style.css'>");

                out.println("<style>");
                out.println("body { background:#eff6ff; font-family:Arial; }");
                out.println(".page { max-width:1200px; margin:40px auto; padding:20px; }");
                out.println("table { width:100%; border-collapse:collapse; background:white; }");
                out.println("th,td { padding:12px; border:1px solid #ddd; text-align:left; }");
                out.println("th { background:#2563eb; color:white; }");
                out.println("tr:nth-child(even) { background:#f8fafc; }");
                out.println(".search-box { display:flex; gap:10px; margin:20px 0; }");
                out.println(".search-box input { flex:1; }");
                out.println(".search-box button { width:auto; margin:0; }");
                out.println("</style>");

                out.println("</head><body>");

                out.println("<div class='page'>");
                out.println("<h1>Search Items</h1>");

                out.println("<a href='DashboardServlet'>Back to Dashboard</a>");

                out.println("<form action='SearchItemsServlet' method='get' class='search-box'>");
                out.println("<input type='text' name='keyword' "
                        + "placeholder='Search by name, category or location' "
                        + "value='" + escapeHTML(keyword) + "'>");
                out.println("<button type='submit'>Search</button>");
                out.println("</form>");

                out.println("<table>");
                out.println("<tr>");
                out.println("<th>ID</th>");
                out.println("<th>Name</th>");
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
                    out.println("<tr><td colspan='8'>No matching items found.</td></tr>");
                }

                out.println("</table>");
                out.println("</div>");
                out.println("</body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Unable to search items.");
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