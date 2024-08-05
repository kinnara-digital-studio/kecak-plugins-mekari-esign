<#if includeMetaData!>
<div class="form-cell" ${elementMetaData!}>
    <label class="label" style="position:absolute;top:10px;left:10px;">
        ${element.properties.label!}
        <span class="form-cell-validator">${decoration!}</span>
        <#if error??> <span class="form-error-message">${error!}</span></#if>
    </label>
    <div style='border: 5px solid grey;height:100px;background-color:#EFF1F2;color:#C4C7CB;align:center;'>
        <span style='position:absolute;top:10px;left:270px;font-weight:bold;font-size:70px;align:center;'>PDF</span>
    <div>
</div>
<#else>
    <#assign uniqueKey = element.properties.elementUniqueKey />

    <style>
.btn {
background: #2916c3;
border: none;
border-radius: 4px;
min-height: 31px;
min-width: 70px;
padding: 4px 16px;
text-align: center;
text-transform: uppercase;
color: white;
cursor: pointer;
display: inline-block;
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
</style>
<div class="form-cell">
        <div style="text-align:center;background-color: #5480fb;color: #e5e5ef;">
            <span class="btn" onclick="downloadPDF()" style="cursor:pointer;">
                <i class="fa fa-download"></i>
                Download
            </span>

            <span class="btn prev" style="cursor:pointer;">
                <i class="fa fa-angle-left"></i>
                Prev
            </span>
            ||
            <span class="btn next" style="cursor:pointer;">
                Next
                <i class="fa fa-angle-right"></i>
            </span>
            &nbsp; &nbsp;
            <span>Page: <span class="page_num"></span> / <span class="page_count"></span></span>
        </div>
        <div id="document-container-${uniqueKey}" style="width:750px;height:600px;overflow-y:scroll;background: gray">
            <div class="wrapper">
                <canvas id="pdf-canvas-${uniqueKey}" width="100%" height="500px" style="display:block;cursor:pointer;cursor: hand;"></canvas>
                <canvas id="stamp-canvas-${uniqueKey}"></canvas>
            </div>
            <div class="uk-panel uk-panel-box" id="signature-or-initial">
                <div class="item">
                    <img src="" alt="Signature" data-tipe="1" >
                </div>
            </div>
        </div>
        <input id="${elementParamName!}_${uniqueKey!}" name="${elementParamName!}" type="hidden" value="1;0;0;1;1">
    </div>
    <script>
        document.addEventListener('DOMContentLoaded', function(){
var url = "${request.contextPath}${pdfFile!?html}";

var pdfDoc = null,
pageNum = 1,
pageRendering = false,
pageNumPending = null,
scale = 1,
pdfCanvas = document.getElementById('pdf-canvas-${uniqueKey}'),
                stampCanvas = document.getElementById('stamp-canvas-${uniqueKey}'),
                ctx = pdfCanvas.getContext('2d');

            var dataObjects = [],
                fobject = null;

            function renderPage(num) {
pageRendering = true;
pdfDoc.getPage(num).then(function(page) {
var viewport = page.getViewport({scale: scale});
                    pdfCanvas.height = viewport.height;
                    pdfCanvas.width = viewport.width;

                    var renderContext = {
canvasContext: ctx,
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

                document.querySelector('.page_num').textContent = num;
            }

            function queueRenderPage(num) {
if (pageRendering) {
pageNumPending = num;
} else {
renderPage(num);
}
            }

            function onPrevPage() {
if (pageNum <= 1) {
return;
}
                pageNum--;
                queueRenderPage(pageNum);
            }
            document.querySelector('.prev').addEventListener('click', onPrevPage);

            function onNextPage() {
if (pageNum >= pdfDoc.numPages) {
return;
}
                pageNum++;
                queueRenderPage(pageNum);
            }
            document.querySelector('.next').addEventListener('click', onNextPage);

            pdfjsLib.getDocument(url).promise.then(function(pdfDoc_) {
pdfDoc = pdfDoc_;
document.querySelector('.page_count').textContent = pdfDoc.numPages;
renderPage(pageNum);
}).catch(console.error);

            function downloadPDF() {
window.open('${request.contextPath}${pdfFile!?html}?attachment=true');
            }
        });
    </script>
</#if>
