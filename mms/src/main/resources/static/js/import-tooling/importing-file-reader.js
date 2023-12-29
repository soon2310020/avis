class ImportingFileReader {
    constructor(){
        this.name = '';
        this.totalSize = 0;
        this.progress = 0;
        this.loadedSize = 0;
        this.isDone = false;
        this.isCancel = false;
        this.acceptTypes = [];
        this.reader = new FileReader();
        this.loadFileSuccessCallback = null;
        this.checkingReadingDoneCallback = null;
    }

    accept(fileType) {
        if (typeof fileType === 'object') {
            fileType.forEach(type => {
                this.acceptTypes.push(type);
            });
        } else if (typeof fileType === 'string') {
            this.acceptTypes.push(fileType);
        }

        return this;
    }

    isValidType() {
        return this.acceptTypes.includes(this.extractFileType());
    }
    cancel() {
        this.isCancel = true;
        this.checkingReadingDoneCallback();
        this.reader.abort();
    }

    setLoadFileSuccessCallback(loadFileSuccessCallback) {
        this.loadFileSuccessCallback = loadFileSuccessCallback;
        return this;
    }
    setCheckingReadingDoneCallback(checkingReadingDoneCallback) {
        this.checkingReadingDoneCallback = checkingReadingDoneCallback;
        return this;
    }

    errorHandler(evt){
        switch(evt.target.error.code) {
            case evt.target.error.NOT_FOUND_ERR:
                console.log('File Not Found!');
                break;
            case evt.target.error.NOT_READABLE_ERR:
                console.log('File is not readable');
                break;
            case evt.target.error.ABORT_ERR:
                break; // noop
            default:
                console.log('An error occurred reading this file.');
        }
    }

    updateProgress(evt) {
        // evt is an ProgressEvent.
        if (evt.lengthComputable) {
            var percentLoaded = Math.round((evt.loaded / evt.total) * 100);
            // Increase the progress bar length.
            if (percentLoaded < 100) {
                this.progress = percentLoaded;
                this.loadedSize = evt.loaded>>10;
            }
        }
    }

    isImportedDone() {
        return this.isCancel || this.progress === 100;
    }

    readFile(file){
        this.name = file.name;
        this.totalSize = file.size>>10;
        this.reader.onerror = this.errorHandler;
        this.reader.onprogress = (evt) => {
            this.updateProgress(evt);
        };
        this.reader.onabort = function(e) {
            console.log('File read cancelled');
        };
        this.reader.onloadstart = function(e) {
            console.log('onloadStart');
        };
        this.reader.onload = (e) => {
            // when load file done
            this.isDone = true;
            this.progress = 100;
            this.loadedSize = e.loaded>>10;

            // read file content
            let data = e.target.result;
            if (this.isValidType()) {
                this.loadFileSuccessCallback(this.name, data);
                this.checkingReadingDoneCallback();
            } else {
                this.isCancel = true;
                this.checkingReadingDoneCallback();
                console.error('invalid file type: ' + this.extractFileType() + ', only accept: ' + this.acceptTypes);
            }

        };

        // Read in the image file as a binary string.
        this.reader.readAsBinaryString(file);
    }

    extractFileType() {
        if (!this.name) {
            return null;
        }
        return this.name.split('.').pop();
    }
}