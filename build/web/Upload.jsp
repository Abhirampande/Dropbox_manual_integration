<%-- 
    Document   : Upload
    Created on : Jan 11, 2026, 6:57:07 PM
    Author     : Abhiram
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html>
<head>
    <title>Patient Report Upload</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- jQuery -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>

<script>
    const contextPath = "<%= request.getContextPath() %>";
</script>

<body class="bg-light">

<div class="container mt-5">
    <div class="card shadow">
        <div class="card-header bg-primary text-white">
            Upload Patient Report / Image
        </div>
        <div class="card-body">
            <div class="mb-3">
                <label>Patient ID</label>
                <input type="text" id="patientId" class="form-control">
            </div>

            <div class="mb-3">
                <label>Visit ID</label>
                <input type="text" id="visitId" class="form-control">
            </div>

            <div class="mb-3">
                <label>Select File</label>
                <input type="file" id="fileInput" class="form-control">
            </div>

            <button class="btn btn-success" type="button" onclick="uploadFile()">Upload</button>
        </div>
    </div>
</div>

<script>
function uploadFile() {
    debugger;
       console.log("uploadFile() called");
    var patientId = $("#patientId").val();
    var visitId = $("#visitId").val();
    var file = $("#fileInput")[0].files[0];

    if (!patientId || !visitId || !file) {
        alert("All fields are required");
        return;
    }

    var reader = new FileReader();

    reader.onload = function(e) {
        let base64Data = e.target.result;

        // FormData is required for large Base64
        var formData = new FormData();
        formData.append("patientId", patientId);
        formData.append("visitId", visitId);
        formData.append("fileName", file.name);
        formData.append("base64", base64Data);

        $.ajax({
            url: contextPath + "/SaveFileServlet",
            type: "POST",
            data: formData,
            processData: false,   // Required for FormData
            contentType: false,   // Required for FormData
            success: function(response) {
                debugger;
                alert(response.trim());
            },
            error: function() {
                alert("Upload failed");
            }
        });
    };

    reader.readAsDataURL(file);
}
</script>

</body>
</html> 