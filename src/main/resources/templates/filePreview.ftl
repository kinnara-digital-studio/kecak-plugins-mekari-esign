<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>File Preview</title>
    <style>
        /* styles.css */
        body {
font-family: Arial, sans-serif;
margin: 0;
padding: 0;
display: flex;
justify-content: center;
align-items: center;
height: 100vh;
background-color: #f5f5f5;
}

.container {
text-align: center;
}

input[type="file"] {
margin: 20px 0;
}

        #previewContainer {
margin-top: 20px;
position: relative;
width: 80vw;
height: 80vh;
}

        #pdfPreview {
width: 100%;
height: 100%;
}
    </style>
</head>
<body>
    <div class="container">
        <h1>File Preview</h1>
        <input type="file" id="fileInput" accept=".pdf">
        <div id="previewContainer">
            <iframe id="pdfPreview" src="" frameborder="0"></iframe>
        </div>
    </div>

    <script>
        // scripts.js
        document.addEventListener('DOMContentLoaded', () => {
const fileInput = document.getElementById('fileInput');
const pdfPreview = document.getElementById('pdfPreview');

fileInput.addEventListener('change', (event) => {
const file = event.target.files[0];

if (file && file.type === 'application/pdf') {
const reader = new FileReader();

reader.onload = (e) => {
pdfPreview.src = e.target.result;
};

                    reader.readAsDataURL(file);
                } else {
alert('Please upload a valid PDF file.');
}
            });
        });
    </script>
</body>
</html>
