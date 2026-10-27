import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Random;
import javax.net.ssl.*;
 
public class App {
 
    // ===== DEMO ONLY: intentional issues for SonarQube. Do not use in real code. =====
 
    // SECURITY (Vulnerability): hard-coded credentials
    private static final String DB_PASSWORD = "admin123";
    private static final String DB_USER = "root";
    private static final String API_KEY = "AKIAIOSFODNN7EXAMPLE";
 
    // MAINTAINABILITY: public mutable static field
    public static String lastUser = "";
 
    // MAINTAINABILITY: unused private field
    private int unusedCounter = 0;
 
    // SECURITY: SQL built by joining strings (SQL injection pattern)
    static ResultSet findUser(String name) throws Exception {
        // SECURITY HOTSPOT: cleartext JDBC URL, hard-coded password
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/app", DB_USER, DB_PASSWORD);
        Statement st = conn.createStatement();
        // RELIABILITY: connection and statement are never closed (resource leak)
        return st.executeQuery("SELECT * FROM users WHERE name = '" + name + "'");
    }
 
    // SECURITY HOTSPOT: weak hash algorithm (MD5)
    static byte[] weakHash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(input.getBytes());
    }
 
    // SECURITY HOTSPOT: predictable random number for a token
    static int makeToken() {
        Random r = new Random();
        return r.nextInt();
    }
 
    // SECURITY: TLS certificate checks switched off
    static void trustEveryone() throws Exception {
        TrustManager[] trustAll = new TrustManager[] {
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] c, String a) { }
                public void checkServerTrusted(X509Certificate[] c, String a) { }
                public X509Certificate[] getAcceptedIssuers() { return null; }
            }
        };
        SSLContext ctx = SSLContext.getInstance("SSL");
        ctx.init(null, trustAll, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
    }
 
    // SECURITY HOTSPOT: running OS command with user input
    static void runCommand(String userInput) throws Exception {
        Runtime.getRuntime().exec("ping " + userInput);
    }
 
    // SECURITY HOTSPOT: cleartext http URL
    static final String CALLBACK_URL = "http://example.com/callback";
 
    // RELIABILITY (Bugs)
    static boolean buggyChecks(String a, String b) {
        int x = 10;
        if (x == x) {              // same value on both sides
            return a == b;         // string compared with ==
        }
        int zero = 0;
        return (10 / zero) > 1;    // division by zero
    }
 
    // MAINTAINABILITY (Code smells): empty catch, generic Exception, printStackTrace, System.out
    static void smells(String path) {
        try {
            FileInputStream in = new FileInputStream(path);   // never closed
            in.read();
        } catch (Exception e) {
        }
        try {
            new FileReader("/tmp/x").read();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed");
        }
        // int old = computeOld();   <- commented-out code
    }
 
    // MAINTAINABILITY: too complex, many nested ifs and magic numbers
    static String complex(int a, int b, int c, int d) {
        if (a > 1) {
            if (b > 2) {
                if (c > 3) {
                    if (d > 4) {
                        if (a + b > 100) {
                            if (c + d > 200) {
                                return "very deep";
                            } else if (a * b > 300) {
                                return "deep";
                            }
                        }
                    }
                }
            }
        }
        return "none";
    }
 
    // DUPLICATION: two almost identical blocks (hurts the duplication metric)
    static String reportOne(int v) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report start\n");
        sb.append("Value is: ").append(v).append("\n");
        sb.append("Value doubled: ").append(v * 2).append("\n");
        sb.append("Value tripled: ").append(v * 3).append("\n");
        sb.append("Value squared: ").append(v * v).append("\n");
        sb.append("Report end\n");
        return sb.toString();
    }
 
    static String reportTwo(int v) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report start\n");
        sb.append("Value is: ").append(v).append("\n");
        sb.append("Value doubled: ").append(v * 2).append("\n");
        sb.append("Value tripled: ").append(v * 3).append("\n");
        sb.append("Value squared: ").append(v * v).append("\n");
        sb.append("Report end\n");
        return sb.toString();
    }
 
    // ===== Original app (unchanged) =====
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
        server.createContext("/", exchange -> {
            byte[] page = "<h1>Hello from Java Frontend!</h1>".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, page.length);
            exchange.getResponseBody().write(page);
            exchange.close();
        });
        server.start();
    }
}