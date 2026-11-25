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

@WebServlet("/ManageClaimsServlet")
public class ManageClaimsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("user_id") == null
                || !"ADMIN".equals(session.getAttribute("role"))) {

            response.sendRedirect("index.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String sql = "SELECT c.claim_id, c.item_id, i.item_name, "
                   + "u.full_name, c.claim_description, c.claim_date "
                   + "FROM claims c "
                   + "JOIN items i ON c.item_id = i.item_id "
                   + "JOIN users u ON c.claimant_id = u.user_id "
                   + "WHERE c.status = 'PENDING' "
                   + "ORDER BY c.claim_date ASC";

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Manage Claims</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");

            out.println("<style>");
            out.println("body { background:#eff6ff; font-family:Arial,sans-serif; }");
            out.println(".container { max-width:1100px; margin:40px auto; padding:25px; background:white; border-radius:12px; }");
            out.println("table { width:100%; border-collapse:collapse; margin-top:20px; }");
            out.println("th,td { padding:12px; border:1px solid #ddd; text-align:left; }");
            out.println("th { background:#1e40af; color:white; }");
            out.println("tr:nth-child(even) { background:#f1f5f9; }");
            out.println(".approve { background:#16a34a; color:white; border:none; padding:8px 12px; border-radius:5px; cursor:pointer; }");
            out.println(".reject { background:#dc2626; color:white; border:none; padding:8px 12px; border-radius:5px; cursor:pointer; }");
            out.println(".back { display:inline-block; margin-top:20px; padding:10px 18px; background:#1e40af; color:white; text-decoration:none; border-radius:6px; }");
            out.println("</style>");

            out.println("</head><body>");
            out.println("<div class='container'>");
            out.println("<h1>Manage Claims</h1>");
            out.println("<h3>Pending Claims</h3>");

            String message = request.getParameter("message");

            if ("approved".equals(message)) {
                out.println("<p style='color:green;'>Claim approved successfully.</p>");
            } else if ("rejected".equals(message)) {
                out.println("<p style='color:green;'>Claim rejected successfully.</p>");
            } else if ("error".equals(message)) {
                out.println("<p style='color:red;'>Unable to process the claim.</p>");
            }

            out.println("<table>");
            out.println("<tr>");
            out.println("<th>Claim ID</th>");
            out.println("<th>Item ID</th>");
            out.println("<th>Item Name</th>");
            out.println("<th>Student</th>");
            out.println("<th>Description</th>");
            out.println("<th>Date</th>");
            out.println("<th>Action</th>");
            out.println("</tr>");

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    int claimId = rs.getInt("claim_id");

                    out.println("<tr>");
                    out.println("<td>" + claimId + "</td>");
                    out.println("<td>" + rs.getInt("item_id") + "</td>");
                    out.println("<td>" + escape(rs.getString("item_name")) + "</td>");
                    out.println("<td>" + escape(rs.getString("full_name")) + "</td>");
                    out.println("<td>" + escape(rs.getString("claim_description")) + "</td>");
                    out.println("<td>" + rs.getString("claim_date") + "</td>");

                    out.println("<td>");

                    out.println("<form method='post' action='ManageClaimsServlet' style='display:inline;'>");
                    out.println("<input type='hidden' name='claimId' value='" + claimId + "'>");
                    out.println("<input type='hidden' name='action' value='approve'>");
                    out.println("<button class='approve' type='submit'>Approve</button>");
                    out.println("</form>");

                    out.println("<form method='post' action='ManageClaimsServlet' style='display:inline;margin-left:5px;'>");
                    out.println("<input type='hidden' name='claimId' value='" + claimId + "'>");
                    out.println("<input type='hidden' name='action' value='reject'>");
                    out.println("<button class='reject' type='submit'>Reject</button>");
                    out.println("</form>");

                    out.println("</td>");
                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='7'>No pending claims.</td></tr>");
                }

            } catch (Exception e) {
                out.println("<tr><td colspan='7'>Error loading claims.</td></tr>");
                e.printStackTrace();
            }

            out.println("</table>");
            out.println("<a class='back' href='AdminDashboardServlet'>Back to Dashboard</a>");
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("user_id") == null
                || !"ADMIN".equals(session.getAttribute("role"))) {

            response.sendRedirect("index.html");
            return;
        }

        String action = request.getParameter("action");
        String claimIdText = request.getParameter("claimId");

        if (claimIdText == null
                || (!"approve".equals(action) && !"reject".equals(action))) {

            response.sendRedirect("ManageClaimsServlet?message=error");
            return;
        }

        int claimId;
        int adminId = (Integer) session.getAttribute("user_id");

        try {
            claimId = Integer.parseInt(claimIdText);
        } catch (NumberFormatException e) {
            response.sendRedirect("ManageClaimsServlet?message=error");
            return;
        }

        String result = "error";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // Find the pending claim and lock it.
                String selectSQL = "SELECT item_id FROM claims "
                                 + "WHERE claim_id = ? AND status = 'PENDING' "
                                 + "FOR UPDATE";

                int itemId = -1;

                try (PreparedStatement ps = con.prepareStatement(selectSQL)) {
                    ps.setInt(1, claimId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            itemId = rs.getInt("item_id");
                        }
                    }
                }

                if (itemId == -1) {
                    con.rollback();
                    response.sendRedirect("ManageClaimsServlet?message=error");
                    return;
                }

                String newStatus = "approve".equals(action)
                        ? "APPROVED" : "REJECTED";

                // Update the claim.
                String updateClaimSQL =
                        "UPDATE claims SET status = ?, reviewed_by = ?, "
                      + "reviewed_at = CURRENT_TIMESTAMP "
                      + "WHERE claim_id = ? AND status = 'PENDING'";

                try (PreparedStatement ps = con.prepareStatement(updateClaimSQL)) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, adminId);
                    ps.setInt(3, claimId);
                    ps.executeUpdate();
                }

                if ("approve".equals(action)) {

                    // Mark the item as claimed.
                    String updateItemSQL =
                            "UPDATE items SET status = 'CLAIMED' "
                          + "WHERE item_id = ?";

                    try (PreparedStatement ps = con.prepareStatement(updateItemSQL)) {
                        ps.setInt(1, itemId);
                        ps.executeUpdate();
                    }

                    // Reject other pending claims for the same item.
                    String rejectOthersSQL =
                            "UPDATE claims SET status = 'REJECTED', "
                          + "reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP "
                          + "WHERE item_id = ? AND claim_id <> ? "
                          + "AND status = 'PENDING'";

                    try (PreparedStatement ps = con.prepareStatement(rejectOthersSQL)) {
                        ps.setInt(1, adminId);
                        ps.setInt(2, itemId);
                        ps.setInt(3, claimId);
                        ps.executeUpdate();
                    }

                    result = "approved";

                } else {
                    result = "rejected";
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                result = "error";

            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = "error";
        }

        response.sendRedirect("ManageClaimsServlet?message=" + result);
    }

    // Prevent HTML characters in database text from being interpreted as HTML.
    private String escape(String value) {
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