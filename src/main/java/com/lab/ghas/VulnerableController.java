package com.lab.ghas;

import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.io.File;

/**
 * INTENTIONALLY VULNERABLE CODE FOR GHAS DEMO
 * Demonstrates: SQL Injection, XSS, Path Traversal, Hardcoded Credentials
 * CodeQL will detect all these vulnerabilities
 */
@RestController
@RequestMapping("/api")
public class VulnerableController {
    
    // VULNERABILITY 1: Hardcoded Database Credentials
    // CodeQL Alert: Hardcoded credentials in source code
    // Secret Scanning Alert: Exposed password
    private static final String DB_URL = "jdbc:mysql://production-db.company.com:3306/users";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "Pr0duct!0n_P@ssw0rd_2024";
    
    /**
     * VULNERABILITY 2: SQL Injection
     * CodeQL will flag this as CWE-89: Improper Neutralization of Special Elements
     * Attack: /api/user?id=1' OR '1'='1
     */
    @GetMapping("/user")
    public String getUser(@RequestParam String id) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement stmt = conn.createStatement();
        
        // VULNERABLE: Direct string concatenation in SQL query
        String query = "SELECT * FROM users WHERE id = '" + id + "'";
        ResultSet rs = stmt.executeQuery(query);
        
        StringBuilder result = new StringBuilder();
        while (rs.next()) {
            result.append("User: ").append(rs.getString("username"))
                  .append(", Email: ").append(rs.getString("email")).append("\n");
        }
        
        rs.close();
        stmt.close();
        conn.close();
        
        return result.toString();
    }
    
    /**
     * VULNERABILITY 3: Cross-Site Scripting (XSS)
     * CodeQL will flag this as CWE-79: Improper Neutralization of Input
     * Attack: /api/welcome?name=<script>alert(document.cookie)</script>
     */
    @GetMapping("/welcome")
    public String welcome(@RequestParam String name) {
        // VULNERABLE: Unescaped user input in HTML response
        return "<html><body><h1>Welcome, " + name + "!</h1></body></html>";
    }
    
    /**
     * VULNERABILITY 4: Path Traversal
     * CodeQL will flag this as CWE-22: Improper Limitation of Path Name
     * Attack: /api/download?file=../../../../etc/passwd
     */
    @GetMapping("/download")
    public String downloadFile(@RequestParam String file) {
        try {
            // VULNERABLE: No validation of file path
            File targetFile = new File("/var/app/files/" + file);
            java.util.Scanner scanner = new java.util.Scanner(targetFile);
            StringBuilder content = new StringBuilder();
            
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            
            return content.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * VULNERABILITY 5: Command Injection
     * CodeQL will flag this as CWE-78: OS Command Injection
     * Attack: /api/ping?host=google.com;rm -rf /
     */
    @GetMapping("/ping")
    public String ping(@RequestParam String host) {
        try {
            // VULNERABLE: Unsanitized user input in system command
            Process process = Runtime.getRuntime().exec("ping -c 4 " + host);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            return output.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
