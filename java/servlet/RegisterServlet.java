package servlet;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private String hashPassword(String password) throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                65536,
                256
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("full_name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            String passwordHash = hashPassword(password);

            UserDAO dao = new UserDAO();

            boolean success = dao.registerUser(
                    fullName, email, passwordHash
            );

            if (success) {
                response.sendRedirect("index.html");
            } else {
                response.getWriter().println(
                        "Registration failed. Email may already exist."
                );
            }

        } catch (Exception e) {

    response.setContentType("text/html;charset=UTF-8");

    response.getWriter().println(
        "<h2>Registration Error</h2>"
        + "<p>" + e.getMessage() + "</p>"
    );

    e.printStackTrace();
}
    }
}