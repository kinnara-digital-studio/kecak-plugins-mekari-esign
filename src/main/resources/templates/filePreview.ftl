<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>PDF File Preview</title>
</head>
<body>
    <form action="/web/client/app/YOUR_APP_ID/YOUR_FORM_ID?_action=submit" method="post">
        <input type="hidden" name="filePath" value="path/to/your/uploaded/file.pdf">
        <button type="submit">Save File Path</button>
    </form>
    <a href="/path/to/upload/dir/yourfile.pdf" target="_blank">Preview File</a>
</body>
</html>
