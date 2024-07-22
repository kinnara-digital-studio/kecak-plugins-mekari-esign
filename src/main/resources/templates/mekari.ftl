<!DOCTYPE html>
<html lang="en">

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
        }

        .resize-handle {
            position: absolute;
            width: 10px;
            height: 10px;
            background-color: blue;
            cursor: nwse-resize;
        }

        .resize-handle.br {
            right: 0;
            bottom: 0;
        }
    </style>
</head>

<body>
    <div class="form-cell" ${elementMetaData!}>
        <a href="${serverUrl}/auth?client_id=${clientId}&response_type=code&scope=esign&lang=id">
            <button>Login Mekari</button>
        </a>
    </div>

    <h2>Upload and Add Digital Signature on PDF</h2>
    <input type="file" id="fileInput" accept="application/pdf" />
    <div id="pdfContainer"></div>

    <div class="navigation">
        <button onclick="navigatePage('prev')">Previous</button>
        <input type="text" id="pageNumInput" placeholder="Page" />
        <button onclick="goToPage()">Go</button>
        <button onclick="navigatePage('next')">Next</button>
    </div>

    <!-- Dropdown untuk Signer -->
    <div class="form-group">
        <label for="signerDropdown">Signer</label>
        <select id="signerDropdown" class="form-control" required></select>
    </div>

    <div id="signatureContainer" class="signature">
        <div class="resize-handle br"></div>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.9.359/pdf.min.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            // Fungsi untuk mendapatkan daftar signers (bisa diambil dari API atau sumber data lainnya)
            fetch("https://sandbox.kecak.org/web/json/plugin/com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook/");

            function getSigners() {
                // Contoh data signers. Gantikan ini dengan data yang sesuai.
                return [
                    { "value": "signer1@example.com", "label": "Signer One (signer1@example.com)" },
                    { "value": "signer2@example.com", "label": "Signer Two (signer2@example.com)" }
                    // Tambahkan lebih banyak signers sesuai kebutuhan
                ];
            }

            // Mengisi dropdown dengan opsi signer
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

            var currentPage = 1; // Halaman yang sedang aktif
            var numPages = 0; // Jumlah total halaman PDF
            var signatureContainer = document.getElementById('signatureContainer');
            var offsetX, offsetY;
            var initialWidth = 150; // Lebar awal untuk tanda tangan
            var initialHeight = 80; // Tinggi awal untuk tanda tangan

            document.getElementById('fileInput').addEventListener('change', function (event) {
                var file = event.target.files[0];
                if (file && file.type === 'application/pdf') {
                    var fileReader = new FileReader();
                    fileReader.onload = function () {
                        var arrayBuffer = this.result;
                        renderPDF(arrayBuffer);
                    };
                    fileReader.readAsArrayBuffer(file);
                } else {
                    alert('Please upload a valid PDF file.');
                }
            });

            function renderPDF(data) {
                // Menguraikan konten PDF
                pdfjsLib.getDocument({ data: data }).promise.then(function (pdf) {
                    numPages = pdf.numPages; // Mengambil jumlah total halaman PDF
                    var pdfContainer = document.getElementById('pdfContainer');
                    for (let pageNum = 1; pageNum <= numPages; pageNum++) {
                        pdf.getPage(pageNum).then(function (page) {
                            var scale = 1.5;
                            var viewport = page.getViewport({ scale: scale });

                            // Membuat div untuk setiap halaman
                            var div = document.createElement('div');
                            div.classList.add('pdfPage');
                            if (pageNum === currentPage) {
                                div.classList.add('active'); // Tandai halaman aktif pertama
                            }
                            var canvas = document.createElement('canvas');
                            var context = canvas.getContext('2d');
                            canvas.height = viewport.height;
                            canvas.width = viewport.width;
                            canvas.style.border = '1px solid black';
                            div.appendChild(canvas);
                            pdfContainer.appendChild(div);

                            // Menampilkan nomor halaman
                            var pageNumberDiv = document.createElement('div');
                            pageNumberDiv.classList.add('pageNumber');
                            pageNumberDiv.textContent = pageNum; // Tampilkan nomor halaman
                            div.appendChild(pageNumberDiv);

                            // Opsi render
                            var renderContext = {
                                canvasContext: context,
                                viewport: viewport
                            };

                            // Merender halaman PDF ke canvas
                            page.render(renderContext).promise.then(function () {
                                console.log('Page rendered successfully');
                            }).catch(function (err) {
                                console.error('Error rendering page', err);
                            });
                        });
                    }
                }).catch(function (err) {
                    console.error('Error loading PDF document', err);
                });
            }

            // Fungsi untuk navigasi ke halaman selanjutnya atau sebelumnya
            function navigatePage(direction) {
                var pages = document.querySelectorAll('.pdfPage');
                if (direction === 'next' && currentPage < numPages) {
                    pages[currentPage - 1].classList.remove('active');
                    currentPage++;
                    pages[currentPage - 1].classList.add('active');
                } else if (direction === 'prev' && currentPage > 1) {
                    pages[currentPage - 1].classList.remove('active');
                    currentPage--;
                    pages[currentPage - 1].classList.add('active');
                }
                updatePageInput(); // Memperbarui nilai input nomor halaman
            }

            // Fungsi untuk navigasi langsung ke halaman tertentu
            function goToPage() {
                var inputPageNum = document.getElementById('pageNumInput').value;
                inputPageNum = parseInt(inputPageNum);
                if (inputPageNum && inputPageNum > 0 && inputPageNum <= numPages) {
                    var pages = document.querySelectorAll('.pdfPage');
                    pages[currentPage - 1].classList.remove('active');
                    currentPage = inputPageNum;
                    pages[currentPage - 1].classList.add('active');
                    updatePageInput(); // Memperbarui nilai input nomor halaman
                } else {
                    alert('Invalid page number. Please enter a valid page number.');
                }
            }

            // Fungsi untuk memperbarui nilai input nomor halaman
            function updatePageInput() {
                document.getElementById('pageNumInput').value = currentPage;
            }

            // Mendengarkan tombol panah untuk navigasi halaman
            document.addEventListener('keydown', function (event) {
                if (event.key === 'ArrowRight') {
                    navigatePage('next');
                } else if (event.key === 'ArrowLeft') {
                    navigatePage('prev');
                }
            });

            // Drag and Drop serta Zoom untuk tanda tangan
            signatureContainer.addEventListener('mousedown', function (e) {
                e.preventDefault();
                offsetX = e.clientX - signatureContainer.getBoundingClientRect().left;
                offsetY = e.clientY - signatureContainer.getBoundingClientRect().top;

                document.addEventListener('mousemove', onMouseMove);
                document.addEventListener('mouseup', onMouseUp);
            });

            function onMouseMove(e) {
                e.preventDefault();
                var activePage = document.querySelector('.pdfPage.active');
                var activePageRect = activePage.getBoundingClientRect();

                // Batasi gerakan tanda tangan agar tetap di dalam halaman PDF
                var newLeft = e.clientX - offsetX - activePageRect.left;
                var newTop = e.clientY - offsetY - activePageRect.top;

                if (newLeft >= 0 && newLeft + signatureContainer.offsetWidth <= activePageRect.width) {
                    signatureContainer.style.left = newLeft + 'px';
                }

                if (newTop >= 0 && newTop + signatureContainer.offsetHeight <= activePageRect.height) {
                    signatureContainer.style.top = newTop + 'px';
                }
            }

            function onMouseUp() {
                document.removeEventListener('mousemove', onMouseMove);
                document.removeEventListener('mouseup', onMouseUp);
            }

            // Fungsi untuk mengubah ukuran kotak signature
            var resizeHandle = document.querySelector('.resize-handle.br');
            resizeHandle.addEventListener('mousedown', function (e) {
                e.preventDefault();
                document.addEventListener('mousemove', onResize);
                document.addEventListener('mouseup', stopResize);
            });

            function onResize(e) {
                signatureContainer.style.width = (e.clientX - signatureContainer.getBoundingClientRect().left) + 'px';
                signatureContainer.style.height = (e.clientY - signatureContainer.getBoundingClientRect().top) + 'px';
            }

            function stopResize(e) {
                document.removeEventListener('mousemove', onResize);
                document.removeEventListener('mouseup', stopResize);
            }
        });
    </script>
</body>
</html>