package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserDAO {

    // REGISTER USER
    public boolean registerUser(String fullName, String email,
                               String passwordHash) throws SQLException {

        String sql = "INSERT INTO users "
                   + "(full_name, email, password_hash, role) "
                   + "VALUES (?, ?, ?, 'STUDENT')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            return ps.executeUpdate() > 0;
        }
    }

    // LOGIN USER
    public boolean loginUser(String email, String password)
                            throws Exception {

        String sql = "SELECT password_hash FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String storedHash = rs.getString("password_hash");

                    String[] parts = storedHash.split(":");

                    byte[] salt = Base64.getDecoder()
                            .decode(parts[0]);

                    byte[] originalHash = Base64.getDecoder()
                            .decode(parts[1]);

                    PBEKeySpec spec = new PBEKeySpec(
                            password.toCharArray(),
                            salt,
                            65536,
                            256
                    );

                    SecretKeyFactory factory =
                            SecretKeyFactory.getInstance(
                                    "PBKDF2WithHmacSHA256"
                            );

                    byte[] enteredHash =
                            factory.generateSecret(spec).getEncoded();

                    return MessageDigest.isEqual(
                            originalHash,
                            enteredHash
                    );
                }
            }
        }

        return false;
    }

    // GET USER ID
    public int getUserIdByEmail(String email) throws SQLException {

        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }

        return -1;
    }
    // GET USER ROLE
public String getUserRole(String email) throws SQLException {

    String sql = "SELECT role FROM users WHERE email = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, email);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("role");
            }
        }
    }

    return null;
}
}