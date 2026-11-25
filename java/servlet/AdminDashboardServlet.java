package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AdminDashboardServlet")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Check whether the user is logged in as ADMIN
        if (session == null
                || session.getAttribute("user_id") == null
                || !"ADMIN".equals(session.getAttribute("role"))) {

            response.sendRedirect("index.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Admin Dashboard</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");

            out.println("<style>");
            out.println("body { background:#eff6ff; font-family:Arial,sans-serif; }");
            out.println(".dashboard { max-width:700px; margin:50px auto; padding:30px; text-align:center; }");
            out.println(".menu { display:flex; flex-direction:column; gap:15px; margin-top:30px; }");
            out.println(".menu a { display:block; padding:15px; background:#1e40af; color:white; text-decoration:none; border-radius:8px; font-size:17px; }");
            out.println(".menu a:hover { background:#1e3a8a; }");
            out.println(".logout { background:#64748b !important; }");
            out.println("</style>");

            out.println("</head>");
            out.println("<body>");

            out.println("<div class='dashboard'>");

            out.println("<h1>Campus Lost & Found</h1>");
            out.println("<h2>Admin Dashboard</h2>");
            out.println("<p>Manage reported items and student claims.</p>");

            out.println("<div class='menu'>");

            out.println("<a href='ViewItemsServlet'>View All Items</a>");
            out.println("<a href='ViewClaimsServlet'>View Claims</a>");
            out.println("<a href='ManageClaimsServlet'>Approve / Reject Claims</a>");
            out.println("<a href='LogoutServlet'>Logout</a>");

            out.println("</div>");

            out.println("</div>");

            out.println("</body>");
            out.println("</html>");
        }
    }
}