<template>
    <div class="upload-wrapper">
        <div class="upload-content">
            <div id="dragdrop" @dragover="stop" @dragleave="stop" @drop="dragAccept">
                <div class="dragdrop-center">
                    <div class="icon">
                        <img src="/images/import-tooling/uploading-spreadsheet.svg" alt="import icon"/>
                    </div>
                    <div class="browser">
                        <input type="file" name="file" id="file" @change="handleFileSelect" multiple  ref="fileupload">
                        <div>
                            <span v-text="resources['drag_drop']"></span>
                            <label for="file" class="browser-title" v-text="resources['browse']"></label>
                            <span v-text="resources['file_upload']"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div id="file-list">
                <div class="file-item"
                     :class="[importedFile.isDone? 'complete': 'processing', importedFile.isCancel ? 'cancel': '']"
                     v-for="(importedFile, index) in fileList">
                    <div class="file-icon">
                        <img src="/images/import-tooling/file-upload.svg" alt="file icon">
                    </div>
                    <div class="progress-info">
                        <div class="file-name">
                            {{ importedFile.name }}
                        </div>
                        <div class="progress-bar">
                            <div class="progress-bar-current" :style="{width: importedFile.progress + '%'}"></div>
                        </div>
                        <div class="progress-hint">
                            <span class="current-size">{{importedFile.loadedSize}}</span>
                             <span  v-text="resources['of']"></span>
                             <span class="total-size">{{importedFile.totalSize}}</span>
                            <span class="size-unit" v-text="resources['kb']"></span>
                        </div>
                    </div>
                    <div class="status" v-if="importedFile.isDone" @click="removeFile(index)" style="cursor: pointer;">
                       <img src="/images/import-tooling/close.svg"  alt="cancel" style="width: 5px; margin-bottom: 20px; margin-left: -5px;">
                    </div>
                    <div v-else class="status" @click="importedFile.cancel()" style="cursor: pointer;">
                        <img src="/images/import-tooling/close.svg"  alt="cancel" style="width: 5px; margin-bottom: 20px; margin-left: -5px;">
                    </div>
                </div>
            </div>
            <div class="download-template">
              <p v-text="resources['need_help_getting_started']"></p>
              <div v-text="resources[isTooling ? 'download_tooling_data_import_template' : 'download_part_data_import_template']" @click="downloadTemplate()"></div>
<!--              <div v-text="resources['download_tooling_data_import_template']" @click="downloadTemplate()"></div>-->
            </div>
        </div>
    </div>
</template>

<script>
    module.exports = {
        name: "Importing",
      props: {
        resources: Object,
        rowHeader: {
          default: 1,
          type: Number
        },
        rowHeaderReplace: {
          default: null,
          type: Number
        },
        rowDataStart: {
          default: 3,
          type: Number
        },
        colDataStart: {
          default: 0,
          type: Number
        },
        isTooling: {
          default: false,
          type: Boolean
        }
      },
        data() {
            return {
                fileList: [],
                acceptTypes: ['xls', 'xlsx']
            }
        },
        methods: {
            handleFileSelect(evt) {
                for (var i = 0; i < evt.target.files.length; i++) {
                    this.readFile(evt.target.files[i]);
                }
            },
            removeFile(index) {
                this.fileList.splice(index, 1);
              this.$emit('remove-import-file',index);
              if(this.$refs.fileupload && this.$refs.fileupload.value){
                  this.$refs.fileupload.value=null;
                }
            },
            dragAccept(evt) {
                this.stop(evt);
                if (evt.dataTransfer.files.length > 0)
                    for (var i = 0; i < evt.dataTransfer.files.length; i++) {
                        this.readFile(evt.dataTransfer.files[i]);
                    }
            },
            stop(evt) {
                evt.stopPropagation();
                evt.preventDefault();
            },
            readFile(file) {
                this.$emit('processing-import');
                let importingFileReader = new ImportingFileReader();
                importingFileReader.accept(this.acceptTypes);
                importingFileReader.accept(this.acceptTypes)
                  .setLoadFileSuccessCallback(this.readFileSuccess)
                  .setCheckingReadingDoneCallback(this.checkingImportAllDone)
                  .readFile(file);
                this.fileList.push(importingFileReader);
            },

            readFileSuccess(fileName, rawData) {
                const workbook = XLSX.read(rawData, {
                    type: 'binary'
                });
                const firstSheetName = workbook.SheetNames[0];
                let xlsxData = XLSX.utils.sheet_to_json(workbook.Sheets[firstSheetName], {header: 1});

                // set header
                let headers = [];
                xlsxData[this.rowHeader].forEach((headerTitle, index) => {
                  if(index<this.colDataStart) return;
                    headers[index] = {
                            // name: headerTitle.replace(/[^0-9A-Za-z ()%:\-]*/g, '')
                            name: headerTitle.replaceAll('\n',' ').replaceAll('*','').replace(/ +/g, ' ').trim()
                        }
                    headers[headers.length - 1].maxLength = 0;
                });

              let headersReplace = [];
                if (this.rowHeaderReplace) {
                  xlsxData[this.rowHeaderReplace].forEach((headerTitle, index) => {
                    if(index<this.colDataStart) return;
                    headersReplace[index] = {
                      // name: headerTitle.replace(/[^0-9A-Za-z ()%:\-]*/g, '')
                      name: headerTitle.replaceAll('\n',' ').replaceAll('*','').replace(/ +/g, ' ').trim()
                    }


                    headersReplace[headersReplace.length - 1].maxLength = 0;
                  });
                }
              // headers = [...headers, ...headersReplace];
              headersReplace.forEach((headerItem, index) => {
                if(headerItem) {
                  headers[index] = headerItem;
                }
              })
                console.log('header', headers)
                let data = [];
                const dataLength = xlsxData.length;
                const headerLength = headers.length;
                for(let i = this.rowDataStart; i < dataLength; i++) {
                    if(xlsxData[i].length > 0) {
                        const row = xlsxData[i];
                        let item = [];
                        for(let j = this.colDataStart; j < headerLength; j++) {
                            if(!row[j]) {
                                row[j] = '';
                            }
                            // row[j] = row[j].replace(/ +/g, ' ');
                            row[j] = row[j].replace(/ +/g, ' ').trim();
                            if (row[j].length > headers[j].maxLength) {
                                headers[j].maxLength = row[j].length;
                            }
                            item.push(row[j]);
                        }
                        data.push(item);
                    }
                }

                this.$emit('import-file', fileName, headers, data, headersReplace);
            },
            async downloadTemplate () {
              const url = this.isTooling ? '/api/molds/exportExcelImportTemplate' : '/api/parts/exportExcelImportTemplate'
              const self = this
              axios.get(url, {responseType: 'blob', observe: 'response'}).then(function (response) {
                var blob = new Blob([response.data], {type: "text/plain;charset=utf-8"});
                console.log(self.isTooling, 'this.isToolingthis.isToolingthis.isToolingthis.isTooling')
                saveAs(blob, self.isTooling ? 'eMoldino_tooling_data_import_template.xlsx' : 'eMoldino_part_data_import_template.xlsx');
              }).catch((e) => {
                console.log(e)
              })
            },
            checkingImportAllDone(){
                let isAllDone = true;
                this.fileList.forEach(importedFile => {
                    console.log('importedFile.name', importedFile.name);
                    console.log('importedFile.isImportedDone()', importedFile.isImportedDone());
                    if(!importedFile.isImportedDone()) {
                        isAllDone = false;
                    }
                });
                console.log('isAllDone', isAllDone);
                if (isAllDone) {
                    this.$emit('imported-done')
                }
            },
            emitEvent() {
                this.$emit('child');
            }
        }

    };
</script>

<style scoped>
.download-template {
  text-align: center;
  margin-top: 15px;
}
.download-template p {
  color: #616161;
  margin-bottom: 7px;
}
.download-template > div {
  display: inline-block;
  color: #463DA5;
  cursor: pointer;
}
</style>