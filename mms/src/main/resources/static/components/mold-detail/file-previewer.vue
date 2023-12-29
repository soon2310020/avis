<template>
    <div id="op-file-previewer"
         class="modal fade"
         tabindex="-1"
         role="dialog"
         aria-labelledby="exampleModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-body">
                    <div class="previewer-header">
                        <div class="left-header">
                            <div class="back-detail-btn" @click="backToDetail">
                                <img src="/images/icon/left-arrow.svg" alt="back"/>
                            </div>
                            <div class="file-name"> {{ file.saveLocation }}</div>
                        </div>
                        <div class="right-header file-action">
                            <div class="action-item print" v-show="false">
                                <img src="/images/icon/print.svg" alt="print"/>
                            </div>
                            <div class="action-item download">
                                <a :href="file.saveLocation" :download="file.fileName"><img src="/images/icon/download.svg" alt="print"/></a>
                            </div>
                            <div class="action-item share" v-show="false">
                                <img src="/images/icon/share.svg" alt="print"/>
                            </div>
                        </div>
                    </div>
                    <div class="previewer-body">
                        <div v-if="canPreviewFile()" class="pdf-wrapper">
                            <div id="pdf-container">
                                <canvas id="pdf_renderer"></canvas>
                            </div>
                        </div>
                        <div v-else class="no-preview">No preview available</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        props: {
            back: Function
        },
        data() {
            return {
                file: {},
                previewState: {
                    pdf: null,
                    currentPage: 1,
                    zoom: 2,
                    numPages: 0
                },
              fromPopup:null,
            };
        },
        methods: {
            showFilePreviewer(file,fromPopup) {
              console.log('showFilePreviewer')
                $(this.getRootToCurrentId()).modal("show");
                this.file = file;
                this.fromPopup=fromPopup;
                if (!this.canPreviewFile())
                    return false;
                pdfjsLib.getDocument(file.saveLocation).then((pdf) => {
                    this.previewState.pdf = pdf;
                    this.previewState.numPages = pdf.numPages;
                    pdf.getPage(this.previewState.currentPage).then(this.handlePages);
                });
            },
            handlePages(page) {
                //This gives us the page's dimensions at full scale
                const viewport = page.getViewport(1.5);

                //We'll create a canvas for each page to draw it on
                const canvas = document.createElement("canvas");
                canvas.style.display = "block";
                const context = canvas.getContext('2d');
                canvas.height = viewport.height;
                canvas.width = viewport.width;

                //Draw it on the canvas
                page.render({canvasContext: context, viewport: viewport});

                //Add it to the web page
                document.getElementById('pdf-container').appendChild(canvas);

                //Move to next page
                this.previewState.currentPage++;
                if (this.previewState.pdf !== null && this.previewState.currentPage <= this.previewState.numPages) {
                    this.previewState.pdf.getPage(this.previewState.currentPage).then(this.handlePages);
                }
            },
            canPreviewFile() {
              console.log(this.file, 'fileeeeee')
                return this.file && this.file.saveLocation &&  this.file.saveLocation.endsWith('.pdf');
            },
            backToDetail(){
                $(this.getRootToCurrentId()).modal("hide");
                this.back(this.fromPopup);
            }
        },
        mounted() {
        }
    };
</script>
