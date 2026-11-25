package servlet;

import db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ClaimServlet")
public class ClaimServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int claimantId = (Integer) session.getAttribute("user_id");

        String itemIdText = request.getParameter("item_id");
        String claimDescription = request.getParameter("claim_description");

        try {
            int itemId = Integer.parseInt(itemIdText);

            try (Connection con = DBConnection.getConnection()) {

                // Check whether the item exists
                String checkSQL = "SELECT item_id FROM items WHERE item_id = ?";

                try (PreparedStatement checkPS = con.prepareStatement(checkSQL)) {

                    checkPS.setInt(1, itemId);

                    try (ResultSet rs = checkPS.executeQuery()) {

                        if (!rs.next()) {
                            response.setContentType("text/html;charset=UTF-8");
                            response.getWriter().println(
                                "<h2>Item ID does not exist.</h2>"
                                + "<a href='claimItem.html'>Try Again</a>"
                            );
                            return;
                        }
                    }
                }

                // Insert the claim
                String sql = "INSERT INTO claims "
                           + "(item_id, claimant_id, claim_description) "
                           + "VALUES (?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, itemId);
                    ps.setInt(2, claimantId);
                    ps.setString(3, claimDescription);

                    int result = ps.executeUpdate();

                    if (result > 0) {
                        response.setContentType("text/html;charset=UTF-8");

                        response.getWriter().println(
                            "<html><body style='font-family:Arial;text-align:center;'>"
                            + "<h2>Claim Submitted Successfully!</h2>"
                            + "<p>Your claim is now pending review.</p>"
                            + "<a href='DashboardServlet'>Back to Dashboard</a>"
                            + "</body></html>"
                        );
                    }
                }

            }

        } catch (NumberFormatException e) {

            response.getWriter().println("Invalid Item ID.");

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                "<h2>Unable to submit claim.</h2>"
                + "<p>Please try again.</p>"
                + "<a href='claimItem.html'>Back to Claim Form</a>"
            );
        }
    }
}