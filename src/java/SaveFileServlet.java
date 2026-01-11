/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Abhiram
 */

import com.dropbox.core.DbxException;
import java.io.*;
import java.util.Base64;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/SaveFileServlet")
@MultipartConfig
public class SaveFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            System.out.println("Servlet hit");

            // 1️⃣ Read Dropbox token from web.xml
            String ACCESS_TOKEN = getServletContext().getInitParameter("DROPBOX_ACCESS_TOKEN");
            System.out.println("Dropbox token length: " + (ACCESS_TOKEN == null ? "null" : ACCESS_TOKEN.length()));

            if (ACCESS_TOKEN == null || ACCESS_TOKEN.isEmpty()) {
                out.print("Error: Dropbox token not configured");
                return;
            }

            // 2️⃣ Read parameters from request
            String patientId = request.getParameter("patientId");
            String visitId = request.getParameter("visitId");
            String fileName = request.getParameter("fileName");
            String base64 = request.getParameter("base64");

            System.out.println("patientId=" + patientId);
            System.out.println("visitId=" + visitId);
            System.out.println("fileName=" + fileName);
            System.out.println("Base64 length=" + (base64 == null ? "null" : base64.length()));

            if (patientId == null || visitId == null || fileName == null || base64 == null) {
                out.print("Error: Missing parameters");
                return;
            }

            // 3️⃣ Remove Base64 header if exists
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }

            byte[] fileBytes = Base64.getDecoder().decode(base64);

            // 4️⃣ Create Dropbox client
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox-patient-app").build();
            DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

            // 5️⃣ Set Dropbox path (patientId/visitId/fileName)
            String dropboxPath = "/" + patientId + "/" + visitId + "/" + fileName;
           
            boolean uploadedToDropbox = false;
            // 6️⃣ Upload file to Dropbox
         
            try (InputStream in = new ByteArrayInputStream(fileBytes)) {
                client.files().uploadBuilder(dropboxPath)
                      .withMode(WriteMode.OVERWRITE)
                      .uploadAndFinish(in);
                uploadedToDropbox = true;
            
        } catch (DbxException | IOException dropboxEx) {
                // Dropbox failed, print stacktrace
                dropboxEx.printStackTrace();
                uploadedToDropbox = false;
            // -----------------------------
            // Save file locally as fallback
            // -----------------------------
            if (!uploadedToDropbox) {

            String basePath =
                getServletContext().getInitParameter("LOCAL_UPLOAD_PATH");

            File baseDir = new File(basePath, patientId + "/" + visitId);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }

            File localFile = new File(baseDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                fos.write(fileBytes);
            }

            out.print("Dropbox unavailable. File saved locally.");
          }
            // -----------------------------
            // If Dropbox worked
            // -----------------------------
            if (uploadedToDropbox) {
                out.print("File uploaded successfully to Dropbox");
            }  
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            out.print("Error: " + e.getMessage());
        } finally {
            out.close();
        }
    }
}
//SaveFileServlet