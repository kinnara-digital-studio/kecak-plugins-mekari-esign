<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute("org.joget.apps.form.lib.FileUpload")?? || request.getAttribute("org.joget.plugin.enterprise.ImageUpload")??)>
        <link rel="stylesheet" href="${request.contextPath}/js/dropzone/dropzone.css" />
        <script type="text/javascript" src="${request.contextPath}/js/dropzone/dropzone.js"></script>
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
        <script src="${request.contextPath}/plugin/org.joget.apps.form.lib.FileUpload/js/jquery.fileupload.js"></script>
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
#dragBox { width: 100px; height: 50px; background-color: rgba(0, 0, 0, 0.1); border: 2px solid #000; position: absolute; cursor: move; }
</style>
</#if>

<label class="label" for="${elementParamName!}" field-tooltip="${elementParamName!}">${element.properties.label} <span class="form-cell-validator">${decoration}</span><#if error??> <span class="form-error-message">${error}</span></#if></label>
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
        <embed class="pdfViewer pdf-viewer" src="" type="application/pdf" width="100%" height="100%">
    </div>
    <div id="signaturePad">
        <div id="dragBox">Sign Here</div>
    </div>

    <#if element.properties.readonly! != 'true'>
        <script>
            $(document).ready(function() {
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
                        $('.pdfViewer').attr('src', pdfPath);
                        $('#embedContainer').show();
                        $('#signaturePad').show();
                    } else {
$('.pdfViewer').attr('src', '');
$('#embedContainer').hide();
$('#signaturePad').hide();
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
containment: "#embedContainer",
stop: function(event, ui) {
$('input[name="positionX"]').val(ui.position.left);
$('input[name="positionY"]').val(ui.position.top);
}
                });
            });
        </script>
    </#if>
</div>
