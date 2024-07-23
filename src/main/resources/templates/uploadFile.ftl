<!DOCTYPE html>
<html lang = "en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Digital Signature on PDF</title>
    <style>
        body {
font-family: Arial, sans-serif;
margin: 20px;
}
.form-cell {
margin-bottom: 10px;
}
.navigation {
margin: 10px 0;
}
.form-group {
margin-bottom: 15px;
position: relative;
z-index: 10;
background-color: white;
padding: 10px;
border-radius: 5px;
}
label {
display: block;
margin-bottom: 5px;
}
.form-control {
width: 100%;
padding: 8px;
box-sizing: border-box;
border: 1px solid #ccc;
border-radius: 4px;
}
#pdfContainer {
max-width: 100%;
overflow-y: auto;
white-space: nowrap;
position: relative;
}
.pdfPage {
display: none;
margin-bottom: 10px;
position: relative;
}
.pdfPage.active {
display: block;
}
.pdfPage canvas {
border: 1px solid black;
display: block;
margin: 0 auto;
}
.pageNumber {
text-align: center;
margin-top: 5px;
}
.signature {
position: absolute;
border: 2px dashed blue;
cursor: move;
width: 150px;
height: 80px;
background-color: rgba(0, 0, 255, 0.1);
padding: 5px;
box-sizing: border-box;
display: flex;
align-items: center;
justify-content: center;
text-align: center;
color: blue;
font-size: 14px;
}
</style>
</head>
<body>

<h2>Upload and Add Digital Signature on PDF</h2>
<input type = "file" id="fileInput" accept="application/pdf" />
    <div id="pdfContainer"></div>

    <div class="navigation">
        <button onclick="navigatePage('prev')">Previous</button>
        <input type="text" id="pageNumInput" placeholder="Page" />
        <button onclick="goToPage()">Go</button>
        <button onclick="navigatePage('next')">Next</button>
    </div>

    <!-- Dropdown for Signer -->
    <div class="form-group">
        <label for="signerDropdown">Signer</label>
        <select id="signerDropdown" class="form-control" required></select>
    </div>

    <!-- Dropdown for Signature Type -->
    <div class="form-group">
        <label for="signatureTypeDropdown">Signature Type</label>
        <select id="signatureTypeDropdown" class="form-control" required>
            <option value="initial">Initial</option>
            <option value="signature">Signature</option>
            <option value="stamp">Stamp</option>
        </select>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.9.359/pdf.min.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function () {
function getSigners() {
return [
{ "value": "signer1", "label": "Signer 1" },
                    { "value": "signer2", "label": "Signer 2" }
                ];
            }

            function populateSignerDropdown() {
const signers = getSigners();
const signerDropdown = document.getElementById("signerDropdown");

signers.forEach(signer => {
const option = document.createElement("option");
option.value = signer.value;
option.textContent = signer.label;
signerDropdown.appendChild(option);
});
            }

            populateSignerDropdown();

            let currentPage = 1;
            let numPages = 0;
            let signatureContainer = null;
            let offsetX, offsetY;

            document.getElementById('fileInput').addEventListener('change', function (event) {
const file = event.target.files[0];
if (file && file.type === 'application/pdf') {
const fileReader = new FileReader();
fileReader.onload = function () {
const arrayBuffer = this.result;
renderPDF(arrayBuffer);
};
                    fileReader.readAsArrayBuffer(file);
                } else {
alert('Please upload a valid PDF file.');
}
            });

            function renderPDF(data) {
pdfjsLib.getDocument({ data: data }).promise.then(function (pdf) {
numPages = pdf.numPages;
const pdfContainer = document.getElementById('pdfContainer');
pdfContainer.innerHTML = '';  // Clear previous content

for (let pageNum = 1; pageNum <= numPages; pageNum++) {
pdf.getPage(pageNum).then(function (page) {
const scale = 1.5;
const viewport = page.getViewport({ scale: scale });

                            const div = document.createElement('div');
                            div.classList.add('pdfPage');
                            if (pageNum === currentPage) {
div.classList.add('active');
}

                            const canvas = document.createElement('canvas');
                            const context = canvas.getContext('2d');
                            canvas.height = viewport.height;
                            canvas.width = viewport.width;
                            div.appendChild(canvas);
                            pdfContainer.appendChild(div);

                            const pageNumberDiv = document.createElement('div');
                            pageNumberDiv.classList.add('pageNumber');
                            pageNumberDiv.textContent = pageNum;
                            div.appendChild(pageNumberDiv);

                            const renderContext = {
canvasContext: context,
viewport: viewport
};

                            page.render(renderContext).promise.then(function () {
console.log('Page rendered successfully');
}).catch(function (err) {
console.error('Error rendering page', err);
});

                            div.addEventListener('click', function (e) {
const rect = div.getBoundingClientRect();
const x = e.clientX -rect.left;
const y = e.clientY -rect.top;

if (signatureContainer) {
signatureContainer.remove();
}
                                signatureContainer = createSignatureBox(x, y);
                                div.appendChild(signatureContainer);
                            });
                        });
                    }
                }).catch(function (err) {
console.error('Error loading PDF document', err);
});
            }

            function createSignatureBox(x, y) {
const signatureType = document.getElementById('signatureTypeDropdown').value;
const signerName = document.getElementById('signerDropdown').selectedOptions[0].text;

const div = document.createElement('div');
div.classList.add('signature');
div.style.left = x + 'px';
div.style.top = y + 'px';
div.textContent = `${signatureType}`;

                div.addEventListener('mousedown', function (e) {
e.preventDefault();
offsetX = e.clientX -div.getBoundingClientRect().left;
offsetY = e.clientY -div.getBoundingClientRect().top;

function onMouseMove(e) {
e.preventDefault();
const activePage = document.querySelector('.pdfPage.active');
const activePageRect = activePage.getBoundingClientRect();

const newLeft = e.clientX -offsetX -activePageRect.left;
const newTop = e.clientY - offsetY -activePageRect.top;

if (newLeft >= 0 && newLeft + div.offsetWidth <= activePageRect.width) {
div.style.left = newLeft + 'px';
}

                        if (newTop >= 0 && newTop + div.offsetHeight <= activePageRect.height) {
div.style.top = newTop + 'px';
}
                    }

                    function onMouseUp() {
document.removeEventListener('mousemove', onMouseMove);
document.removeEventListener('mouseup', onMouseUp);
}

                    document.addEventListener('mousemove', onMouseMove);
                    document.addEventListener('mouseup', onMouseUp);
                });

                return div;
            }

            function navigatePage(direction) {
const pages = document.querySelectorAll('.pdfPage');
if (direction === 'next' && currentPage < numPages) {
pages[currentPage -1].classList.remove('active');
currentPage++;
pages[currentPage -1].classList.add('active');
} else if (direction === 'prev' && currentPage > 1) {
pages[currentPage -1].classList.remove('active');
currentPage--;
pages[currentPage -1].classList.add('active');
}
                updatePageInput();
            }

            function goToPage() {
let inputPageNum = document.getElementById('pageNumInput').value;
inputPageNum = parseInt(inputPageNum);
if (inputPageNum && inputPageNum > 0 && inputPageNum <= numPages) {
const pages = document.querySelectorAll('.pdfPage');
pages[currentPage -1].classList.remove('active');
currentPage = inputPageNum;
pages[currentPage -1].classList.add('active');
updatePageInput();
} else {
alert('Invalid page number.Please enter a valid page number.');
}
            }

            function updatePageInput() {
document.getElementById('pageNumInput').value = currentPage;
}

            document.addEventListener('keydown', function (event) {
if (event.key === 'ArrowRight') {
navigatePage('next');
} else if (event.key === 'ArrowLeft') {
navigatePage('prev');
}
            });
        });
    </script>
</body>
</html>
