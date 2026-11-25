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
import jakarta.servlet.http.HttpSession;

@WebServlet("/ViewClaimsServlet")
public class ViewClaimsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Check whether the user is an Admin
        if (session == null
                || session.getAttribute("user_id") == null
                || !"ADMIN".equals(session.getAttribute("role"))) {

            response.sendRedirect("index.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String sql = "SELECT c.claim_id, c.item_id, i.item_name, "
                   + "u.full_name, c.claim_description, "
                   + "c.claim_date, c.status "
                   + "FROM claims c "
                   + "JOIN items i ON c.item_id = i.item_id "
                   + "JOIN users u ON c.claimant_id = u.user_id "
                   + "ORDER BY c.claim_date DESC";

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>View Claims</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");

            out.println("<style>");
            out.println("body { background:#eff6ff; font-family:Arial,sans-serif; }");
            out.println(".container { max-width:1100px; margin:40px auto; padding:25px; background:white; border-radius:12px; }");
            out.println("table { width:100%; border-collapse:collapse; margin-top:20px; }");
            out.println("th, td { padding:12px; border:1px solid #ddd; text-align:left; }");
            out.println("th { background:#1e40af; color:white; }");
            out.println("tr:nth-child(even) { background:#f1f5f9; }");
            out.println(".back { display:inline-block; margin-top:20px; padding:10px 18px; background:#1e40af; color:white; text-decoration:none; border-radius:6px; }");
            out.println("</style>");

            out.println("</head>");
            out.println("<body>");

            out.println("<div class='container'>");
            out.println("<h1>Student Claims</h1>");

            out.println("<table>");
            out.println("<tr>");
            out.println("<th>Claim ID</th>");
            out.println("<th>Item ID</th>");
            out.println("<th>Item Name</th>");
            out.println("<th>Student Name</th>");
            out.println("<th>Description</th>");
            out.println("<th>Claim Date</th>");
            out.println("<th>Status</th>");
            out.println("</tr>");

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("claim_id") + "</td>");
                    out.println("<td>" + rs.getInt("item_id") + "</td>");
                    out.println("<td>" + rs.getString("item_name") + "</td>");
                    out.println("<td>" + rs.getString("full_name") + "</td>");
                    out.println("<td>" + rs.getString("claim_description") + "</td>");
                    out.println("<td>" + rs.getString("claim_date") + "</td>");
                    out.println("<td>" + rs.getString("status") + "</td>");
                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr>");
                    out.println("<td colspan='7'>No claims have been submitted yet.</td>");
                    out.println("</tr>");
                }

            } catch (Exception e) {
                out.println("<tr>");
                out.println("<td colspan='7'>Error loading claims: "
                        + e.getMessage() + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");

            out.println("<a class='back' href='AdminDashboardServlet'>Back to Dashboard</a>");

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}