/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerPack;

import LibPack.MessageDetails;
import LibPack.StudentDetails;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PostMessageServlet extends HttpServlet {

    public static String inpComment = "";
    public static String delim = "\t\n\r\f .,;'()[]{}\"";
    public static Vector<String> processedTokens, vctFoulWord;
    public static int polarity = -1;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        MessageDetails obj = null;

        try {
            ObjectInputStream in = new ObjectInputStream(request.getInputStream());
            // receive and deserialize the object, note the cast
            obj = (MessageDetails) in.readObject();
            in.close();
        } catch (Exception e) {
        }

        vctFoulWord = new Vector<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(PathSettings.path + "foulWords.txt"));
            String temp;
            while ((temp = br.readLine()) != null) {
                if (!(temp.equals(""))) {
                    vctFoulWord.add(temp);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int resp = 0;
        polarity = -1;
        inpComment = obj.messageText;
        CheckFoulContents.pre_Process();
        if (polarity == 1) {
            resp = 2;
        }

        if (resp != 2) {
            Connection con = null;
            Statement stmt;
            String connection = "jdbc:mysql://localhost:3306/portaldb";
            String user = "root";
            String password = "root";

            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                con = DriverManager.getConnection(connection, user, password);
                //SSSystem.out.println("Database Connection OK");
            } catch (Exception e) {
                System.out.println("Error opening database : " + e);
            }

            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String toDate = dateFormat.format(date);
                stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                String query = "insert into chat values(null,'" + obj.messageText + "','" + toDate + "','" + obj.senderName + "'," + obj.classId + ")";
                int rs = stmt.executeUpdate(query);
                if (rs > 0) {
                    resp = 1;
                }
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
            out.writeObject(resp);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
