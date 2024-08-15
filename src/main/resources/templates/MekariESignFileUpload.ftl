<div class="form-cell" ${elementMetaData!}>
    <#if !(request.getAttribute("org.joget.apps.form.lib.FileUpload")?? || request.getAttribute("org.joget.plugin.enterprise.ImageUpload")??)>
        <link rel="stylesheet" href="${request.contextPath}/js/dropzone/dropzone.css" />
        <script type="text/javascript" src="${request.contextPath}/js/dropzone/dropzone.js"></script>
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
        <script src="${request.contextPath}/plugin/org.joget.apps.form.lib.FileUpload/js/jquery.fileupload.js"></script>
        <script type="text/javascript">
            // Matikan auto-discover pada Dropzone
            Dropzone.autoDiscover = false;
        </script>

        <style>
.form-fileupload { width: 70%; }
ul.form-fileupload-value { padding: 0; margin: 0; }
ul.form-fileupload-value li { display: block; margin-bottom: 5px; }
ul.form-fileupload-value li .remove { color: red; display: inline-block; margin: 0 30px; }
ul.form-fileupload-value li a { display: inline-block; }
.pdf-viewer { width: 100%; height: 500px; border: 1px solid #ccc; margin-top: 20px; }
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
                    <embed class="pdfViewer pdf-viewer" src="" type="application/pdf" />
                    <div id="embedContainer" style="width: 200px; height: 400px; position: relative; overflow: hidden; border: 2px solid #000000;">
                        <embed style="width: 200px; height: 400px;" />
                        <div id="draggableDiv" style="width: 50px; height: 50px; background-color: #f1f1f1; position: absolute; cursor: move; top: 0; left: 0;">
                            Drag me
                        </div>
                    </div>
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

    <#if element.properties.readonly! != 'true'>
        <script>
            $(document).ready(function() {
console.log($);
if ($.fn.fileUploadField) {
console.log("fileUploadField tersedia");
} else {
console.log("fileUploadField tidak ditemukan");
}

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

                // Initialize draggableDiv after the file upload field is set up
                $("#draggableDiv").draggable({
containment: "parent",
drag: function(event, ui) {
var container = $(this).parent();
var containerWidth = container.width();
var containerHeight = container.height();
var elementWidth = $(this).width();
var elementHeight = $(this).height();

// Constrain horizontal movement
if (ui.position.left < 0) {
ui.position.left = 0;
} else if (ui.position.left + elementWidth > containerWidth) {
ui.position.left = containerWidth - elementWidth;
}

                        // Constrain vertical movement
                        if (ui.position.top < 0) {
ui.position.top = 0;
} else if (ui.position.top + elementHeight > containerHeight) {
ui.position.top = containerHeight - elementHeight;
}
                    }
                });

                // Update PDF viewer dynamically
                function updatePdfViewer(inputElement) {
var pdfViewer = inputElement.siblings('.pdfViewer');
if (pdfViewer.length > 0) {
var inputValue = inputElement.val();
if (inputValue) {
if (!inputValue.startsWith('/')) {
inputValue = "${request.contextPath}/web/json/app/${appId}/${appVersion}/plugin/${className}/service?_nonce=${nonce}&_caller=${className}&_path=" + inputValue;
}
                            pdfViewer.attr('src', inputValue);
                        } else {
pdfViewer.attr('src', '');
}
                    }
                }

                // Create observer to listen for changes
                $('input[name="${elementParamName!}_path"]').each(function() {
var input = $(this)[0];
var observer = new MutationObserver(function(mutations) {
mutations.forEach(function(mutation) {
if (mutation.type === 'attributes' && mutation.attributeName === 'value') {
updatePdfViewer($(input));
}
                        });
                    });

                    observer.observe(input, { attributes: true, attributeFilter: ['value'] });

                    // Initial update
                    updatePdfViewer($(this));
                });

                // Handle dynamically added inputs
                var containerObserver = new MutationObserver(function(mutations) {
mutations.forEach(function(mutation) {
if (mutation.type === 'childList') {
$(mutation.addedNodes).find('input[name="${elementParamName!}_path"]').each(function() {
var input = $(this)[0];
var observer = new MutationObserver(function(mutations) {
mutations.forEach(function(mutation) {
if (mutation.type === 'attributes' && mutation.attributeName === 'value') {
updatePdfViewer($(input));
}
                                        });
                                    });

                                observer.observe(input, { attributes: true, attributeFilter: ['value'] });

                                // Initial update for new input
                                updatePdfViewer($(this));
                            });
                        }
                    });
                });

                containerObserver.observe($('#form-fileupload_${elementParamName!}_${element.properties.elementUniqueKey!}')[0], { childList: true, subtree: true });
            });
        </script>
    </#if>
</div>
