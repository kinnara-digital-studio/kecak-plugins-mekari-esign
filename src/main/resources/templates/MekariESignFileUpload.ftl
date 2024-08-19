<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute("org.joget.apps.form.lib.FileUpload")?? || request.getAttribute("org.joget.plugin.enterprise.ImageUpload")??)>
        <link rel="stylesheet" href="${request.contextPath}/js/dropzone/dropzone.css" />
        <script type="text/javascript" src="${request.contextPath}/js/dropzone/dropzone.js"></script>
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
        <script src="${request.contextPath}/plugin/org.joget.apps.form.lib.FileUpload/js/jquery.fileupload.js"></script>
        <!-- Import pdf-lib -->
        <script src="https://unpkg.com/pdf-lib/dist/pdf-lib.min.js"></script>
        <script type="text/javascript">
            Dropzone.autoDiscover = false;
        </script>

        <style>
            .form-fileupload { width: 70%; }
            ul.form-fileupload-value { padding: 0; margin: 0; }
            ul.form-fileupload-value li { display: block; margin-bottom: 5px; }
            ul.form-fileupload-value li .remove { color: red; display: inline-block; margin: 0 30px; }
            ul.form-fileupload-value li a { display: inline-block; }
            .pdf-viewer { width: 100%; height: 500px; border: 1px solid #ccc; margin-top: 20px; }
            #embedContainer { width: 100%; height: 500px; position: relative; overflow: hidden; border: 2px solid #000000; margin-top: 20px; display: none; }
            #signaturePad { display: none; position: relative; }
            #dragBox { width: 100px; height: 50px; background-color: rgba(255, 0, 0, 0.3); border: 2px solid #000; position: absolute; cursor: move; text-align: center; line-height: 50px; font-weight: bold; }
        </style>
        </#if>

        <label class="label" for="${elementParamName!}" field-tooltip="${elementParamName!}">
                ${element.properties.label} <span class="form-cell-validator">${decoration}</span>
                <#if error??> <span class="form-error-message">${error}</span></#if>
            </label>
            <div id="form-fileupload_${elementParamName!}_${element.properties.elementUniqueKey!}" tabindex="0" class="form-fileupload <#if error??>form-error-cell</#if> <#if element.properties.readonly! == 'true'>readonly<#else>dropzone</#if>">
                <#if element.properties.readonly! != 'true'>
                    <div class="dz-message needsclick">
                        @@form.fileupload.dropFile@@
                    </div>
                    <input style="display: none" class="inputFile" name="${elementParamName!}" type="file" size="${element.properties.size!}" <#if error??>class="form-error-cell"</#if> <#if element.properties.multiple! == 'true'>multiple</#if>/>
                </#if>
                <ul class="form-fileupload-value">
                    <#if element.properties.readonly! != 'true'>
                        <li class="template" style="display: none;">
                            <span class="name" data-dz-name></span> <a class="remove" style="display: none">@@form.fileupload.remove@@</a>
                            <strong class="error text-danger" data-dz-errormessage></strong>
                            <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
                                <div class="progress-bar progress-bar-success" style="width: 0%;" data-dz-uploadprogress></div>
                            </div>
                            <input type="hidden" name="${elementParamName!}_path" value="" />
                        </li>
                    </#if>
                    <#if tempFilePaths??>
                        <#list tempFilePaths?keys as key>
                            <li>
                                <span class="name">${tempFilePaths[key]!?html}</span>
                                <#if element.properties.readonly! != 'true'>
                                    <a class="remove">@@form.fileupload.remove@@</a>
                                </#if>
                                <input type="hidden" name="${elementParamName!}_path" value="${key!?html}"/>
                            </li>
                        </#list>
                    </#if>
                    <#if filePaths??>
                        <#list filePaths?keys as key>
                            <li>
                                <a href="${request.contextPath}${key!?html}" target="_blank"><span class="name">${filePaths[key]!?html}</span></a>
                                <#if element.properties.readonly! != 'true'>
                                    <a class="remove">@@form.fileupload.remove@@</a>
                                </#if>
                                <input type="hidden" name="${elementParamName!}_path" value="${filePaths[key]!?html}"/>
                            </li>
                        </#list>
                    </#if>
                </ul>
            </div>

            <div id="embedContainer">
                <iframe id="pdfViewer" class="pdf-viewer" src="" type="application/pdf" width="100%" height="100%"></iframe>
            </div>
            <div id="signaturePad">
                <div id="dragBox">Sign Here</div>
            </div>

            <div id="downloadButton" style="margin-top: 20px;">
                <button type="button" id="GetFile">Download</button>
            </div>

            <#if element.properties.readonly! != 'true'>
                <script>
                    $(document).ready(function() {
                        var uploadedPdfBytes = null;

                        $('#form-fileupload_${elementParamName!}_${element.properties.elementUniqueKey!}').fileUploadField({
                            url: "${element.serviceUrl!}",
                            paramName: "${elementParamName!}",
                            multiple: "${element.properties.multiple!}",
                            maxSize: "${element.properties.maxSize!}",
                            maxSizeMsg: "${element.properties.maxSizeMsg!}",
                            fileType: "${element.properties.fileType!}",
                            fileTypeMsg: "${element.properties.fileTypeMsg!}",
                            padding: "${element.properties.padding!}",
                            removeFile: "${element.properties.removeFile!}",
                            resizeWidth: "${element.properties.resizeWidth!}",
                            resizeHeight: "${element.properties.resizeHeight!}",
                            resizeQuality: "${element.properties.resizeQuality!}",
                            resizeMethod: "${element.properties.resizeMethod!}",
                        });

                        function updatePdfViewer() {
                            var pdfPath = $('input[name="${elementParamName!}_path"]').val();
                            if (pdfPath) {
                                if (!pdfPath.startsWith('/')) {
                                pdfPath = "${request.contextPath}/web/json/app/${appId}/${appVersion}/plugin/${className}/service?_nonce=${nonce}&_caller=${className}&_path=" + pdfPath;
                            }
                            $('#pdfViewer').attr('src', pdfPath);
                            $('#embedContainer').show();
                            $('#signaturePad').show();

                            // Fetch the PDF bytes
                            fetch(pdfPath).then(res => res.arrayBuffer()).then(data => {
                                uploadedPdfBytes = data;
                            });
                            } else {
                                $('#pdfViewer').attr('src', '');
                                $('#embedContainer').hide();
                                $('#signaturePad').hide();
                                uploadedPdfBytes = null;
                            }
                        }

                        // Observe changes to the file input
                        var observer = new MutationObserver(function(mutations) {
                            mutations.forEach(function(mutation) {
                                if (mutation.type === 'childList') {
                                    updatePdfViewer();
                                }
                            });
                        });

                        observer.observe($('#form-fileupload_${elementParamName!}_${element.properties.elementUniqueKey!}')[0], { childList: true, subtree: true });

                        // Initial check
                        updatePdfViewer();

                        // Make the signature pad draggable
                        var $dragBox = $('#dragBox');
                        $dragBox.draggable({
                            containment: "#embedContainer"
                        });

                        // Handle download button click
                        $('#GetFile').on('click', async function () {
                            if (!uploadedPdfBytes) {
                                alert('Silakan unggah PDF terlebih dahulu.');
                                return;
                            }

                            const pdfDoc = await PDFLib.PDFDocument.load(uploadedPdfBytes);
                            const pages = pdfDoc.getPages();
                            const firstPage = pages[0];

                            // Dapatkan posisi kotak tanda tangan relatif terhadap PDF
                            var pdfViewerOffset = $('#pdfViewer').offset();
                            var dragBoxOffset = $('#dragBox').offset();

                            var posX = dragBoxOffset.left - pdfViewerOffset.left;
                            var posY = dragBoxOffset.top - pdfViewerOffset.top;

                            // Konversi posisi ke skala PDF
                            var pdfWidth = $('#pdfViewer').width();
                            var pdfHeight = $('#pdfViewer').height();

                            var pageWidth = firstPage.getWidth();
                            var pageHeight = firstPage.getHeight();

                            var scaledX = (posX / pdfWidth) * pageWidth;
                            var scaledY = pageHeight - ((posY / pdfHeight) * pageHeight) - 50; // 50 adalah tinggi kotak tanda tangan

                            // Tambahkan kotak tanda tangan ke PDF
                            firstPage.drawText('Signed Here', {
                                x: scaledX,
                                y: scaledY,
                                size: 12,
                                color: PDFLib.rgb(1, 0, 0),
                                borderColor: PDFLib.rgb(0, 0, 0),
                                borderWidth: 1
                            });

                            const pdfBytes = await pdfDoc.save();

                            // Buat blob dan unduh PDF
                            const blob = new Blob([pdfBytes], { type: 'application/pdf' });
                            const url = URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            a.href = url;
                            a.download = 'signed_document.pdf';
                            document.body.appendChild(a);
                            a.click();
                            document.body.removeChild(a);
                            URL.revokeObjectURL(url);
                        });
                    });
                </script>
        </#if>
</div>
