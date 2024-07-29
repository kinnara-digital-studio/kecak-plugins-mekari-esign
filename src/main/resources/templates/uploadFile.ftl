<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>PDF File Upload</title>
</head>
<body>
    <form action="/web/client/app/YOUR_APP_ID/YOUR_FORM_ID?_action=submit" method="post" enctype="multipart/form-data">
        <label for="pdfFile">Upload PDF:</label>
        <input type="file" id="pdfFile" name="pdfFile" accept="application/pdf">
        <button type="submit">Upload</button>
    </form>
</body>
</html>
