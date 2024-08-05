<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.0.3/js/bootstrap.min.js"></script>

<#if includeMetaData!>
<div class="form-cell" ${elementMetaData!}>
    <label class="label" style="position:absolute;top:10px;left:10px;">
        ${element.properties.label!}
        <span class="form-cell-validator">${decoration!}</span>
        <#if error??>
<span class="form-error-message">${error!}</span></#if>
    </label>
    <div style='border: 5px solid grey;height:100px;background-color:#EFF1F2;color:#C4C7CB;text-align:center;'>
        <span style='position:absolute;top:10px;left:270px;font-weight:bold;font-size:70px;'>PDF</span>
    </div>
</div>
<#else>
<#assign uniqueKey = element.properties.elementUniqueKey />

<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/pdfjs-dist/build/pdf.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/fabric/dist/fabric.min.js"></script>

<style>
.md-btn {
background: #2916c3;
border: none;
border-radius: 4px;
min-height: 31px;
min-width: 70px;
padding: 4px 16px;
text-align: center;
text-shadow: none;
text-transform: uppercase;
transition: all 280ms cubic-bezier(.4, 0, .2, 1);
color: #ffffff;
box-sizing: border-box;
cursor: pointer;
display: inline-block;
vertical-align: middle;
font: 500 14px/31px Roboto, sans-serif!important;
}

.wrapper {
position: relative;
height: 500px;
}

.wrapper canvas {
position: absolute;
top: 0;
left: 0;
}

.signature-box {
border: 2px dashed red;
position: absolute;
cursor: pointer;
}
</style>

<div class="form-cell">
    <div style="text-align:center;background-color: #5480fb;color: #e5e5ef;">
        <span class="md-btn" onclick="downloadPDF()">
            <i class="fa fa-download"></i> Download
        </span>
        <span class="md-btn prev">
            <i class="fa fa-angle-left"></i> Prev
        </span>
        ||
        <span class="md-btn next">
            Next <i class="fa fa-angle-right"></i>
        </span>
        &nbsp; &nbsp;
        <span>Page: <span class="page_num"></span> / <span class="page_count"></span></span>
    </div>
    <div id="document-container-${uniqueKey}" style="width:750px;height:600px;overflow-y:scroll;background: gray">
        <div class="wrapper">
            <canvas id="pdf-canvas-${uniqueKey}" width="100%" height="500px" style="display:block;"></canvas>
            <canvas id="signature-canvas-${uniqueKey}" width="100%" height="500px" style="pointer-events: none;"></canvas>
        </div>
        <div class="uk-panel uk-panel-box" id="signature-or-initial">
            <div class="item">
                <img src="" alt="Signature" data-type="1">
            </div>
        </div>
    </div>
    <input id="${elementParamName!}_${uniqueKey!}" name="${elementParamName!}" type="hidden" value="1;0;0;1;1">
</div>

<script>
    $(document).ready(function() {
var url = "${request.contextPath}${pdfFile!?html}";

pdfjsLib.GlobalWorkerOptions.workerSrc = "${request.contextPath}/plugin/${className}/node_modules/pdfjs-dist/build/pdf.worker.js";

var pdfDoc = null,
pageNum = 1,
pageRendering = false,
pageNumPending = null,
scale = 1,
pdfCanvas = document.getElementById('pdf-canvas-${uniqueKey}'),
            signatureCanvas = document.getElementById('signature-canvas-${uniqueKey}'),
            pdfCtx = pdfCanvas.getContext('2d'),
            signatureCtx = signatureCanvas.getContext('2d'),
            signatureBox = null;

        function renderPage(num) {
pageRendering = true;
pdfDoc.getPage(num).then(function(page) {
var viewport = page.getViewport({ scale: scale });
                pdfCanvas.height = viewport.height;
                pdfCanvas.width = viewport.width;

                var renderContext = {
canvasContext: pdfCtx,
viewport: viewport
};
                var renderTask = page.render(renderContext);

                renderTask.promise.then(function() {
pageRendering = false;
if (pageNumPending !== null) {
renderPage(pageNumPending);
pageNumPending = null;
}
                });
            });

            $('.page_num').html(num);
            onPageChange(num);
        }

        function onPageChange(page) {
var oldValue = $('#${elementParamName!}_${uniqueKey!}').val();
var split = oldValue.split(';');
var top = split[1];
var left = split[2];
var scaleY = split[3];
var scaleX = split[4];

var newValue = [page, top, left, scaleY, scaleX].join(';');
$('#${elementParamName!}_${uniqueKey!}').val(newValue);
}

        function queueRenderPage(num) {
if (pageRendering) {
pageNumPending = num;
} else {
renderPage(num);
}
        }

        function onPrevPage() {
if (pageNum <= 1) return;
pageNum--;
queueRenderPage(pageNum);
}
        $('.prev').on('click', onPrevPage);

        function onNextPage() {
if (pageNum >= pdfDoc.numPages) return;
pageNum++;
queueRenderPage(pageNum);
}
        $('.next').on('click', onNextPage);

        pdfjsLib.getDocument(url).promise.then(function(pdfDoc_) {
pdfDoc = pdfDoc_;
$('.page_count').html(pdfDoc.numPages);
renderPage(pageNum);
}).catch(console.error);

        function downloadPDF() {
window.open('${request.contextPath}${pdfFile!?html}?attachment=true');
        }

        // Add event listeners to the signature canvas
        signatureCanvas.addEventListener('mousedown', startDrawing);
        signatureCanvas.addEventListener('mouseup', stopDrawing);
        signatureCanvas.addEventListener('mousemove', drawSignature);

        function startDrawing(event) {
var rect = signatureCanvas.getBoundingClientRect();
var x = event.clientX - rect.left;
var y = event.clientY - rect.top;
signatureBox = { x: x, y: y, width: 0, height: 0 };
        }

        function stopDrawing() {
signatureBox = null;
saveSignatureBox();
}

        function drawSignature(event) {
if (!signatureBox) return;
var rect = signatureCanvas.getBoundingClientRect();
var x = event.clientX - rect.left;
var y = event.clientY - rect.top;
signatureBox.width = x - signatureBox.x;
signatureBox.height = y - signatureBox.y;

// Clear and redraw the signature box
signatureCtx.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
signatureCtx.strokeStyle = 'red';
signatureCtx.lineWidth = 2;
signatureCtx.strokeRect(signatureBox.x, signatureBox.y, signatureBox.width, signatureBox.height);
}

        function saveSignatureBox() {
if (!signatureBox) return;
var value = [pageNum, signatureBox.y, signatureBox.x, signatureBox.height / signatureCanvas.height, signatureBox.width / signatureCanvas.width].join(';');
$('#${elementParamName!}_${uniqueKey!}').val(value);
}
    });
</script>
</#if>
