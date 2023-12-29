<template>
    <div class="note-item-wrapper">
        <div class="note-item can-remove" :class="{focus: isFocusInput}">
            <div class="remove-note" @click="deleteSystemNote(result.id)" v-if="currentUser && currentUser.id === result.creator.id && !isUpdating">Delete</div>
            <div class="remove-note" @click="cancelUpdate" v-if="isUpdating">Cancel</div>
            <div class="note-item-header">
                <div class="author">
                    <div v-if="result.companyType === companyTypes.IN_HOUSE" class="author-icon note-icon-oem">o</div>
                    <div v-if="result.companyType === companyTypes.SUPPLIER" class="author-icon note-icon-supplier">s</div>
                    <div v-if="result.companyType === companyTypes.TOOL_MAKER" class="author-icon note-icon-toolmaker">t</div>
                    <div class="author-name">{{ result.creator.name }} ({{ result.companyName }})</div>
                </div>
                <div class="note-time">
                    {{ result.createdDateTime }} 
                </div>
            </div>
            <div class="note-form-wrapper" v-show="isUpdating">
                <div class="add-note-form mt-3 mb-0">
                    <div class="add-note-content">
                        <div class="system-note-shadow"
                             :id="'message-content-' + result.id"
                             @focus="focusInNoteInput"
                             @blur="focusOutNoteInput" contenteditable="true" @input="setMessageContent">
                        </div>
                        <system-note-user @select-user="selectUser" :id="'update-user-popup-' + result.id"></system-note-user>
                    </div>
                </div>
            </div>
            <!--  -->
            <div v-show="!isUpdating" class="note-item-content" @click="showUpdateForm(isUpdating)" v-html="getMessageContent(result)">
            </div>
        </div>
        <div class="mb-2" v-show="isUpdating && focusedResult.id === result.id" @click="updateSystemNote">
            <span class="btn btn-success btn-update-note" :id="'save-note-' + result.id">Save Note</span>
        </div>
    </div>

</template>

<script>

    module.exports = {
        props: {
            result: {
                type: Object,
                default: () => {}
            },
            currentUser: {
                type: Object,
                default: () => {}
            },
            clearUpdate: [Number, String],
            companyTypes: {
                type: Object,
                default: () => {}
            },
            objectTypes: {
                type: Object,
                default: () => {}
            },
            focusedResult: {
                type: Object,
                default: () => {}
            },
            isAddingNote: Boolean
        },
        components: {
            'system-note-user': httpVueLoader('/components/system-note-user.vue')
        },
        data() {
            return {
                isUpdating: false,
                isFocusInput: false,
                isSubmitting: false,
                noteForm: {},
            }
        },
        methods: {
            stringInject(str, data) {
                if (typeof str === 'string' && data instanceof Array) {

                    return str.replace(/({\d})/g, function (i) {
                        return data[i.replace(/{/, '').replace(/}/, '')];
                    });
                } else if (typeof str === 'string' && data instanceof Object) {

                    if (Object.keys(data).length === 0) {
                        return str;
                    }

                    for (var key in data) {
                        return str.replace(/({([^}]+)})/g, function (i) {
                            var key = i.replace(/{/, '').replace(/}/, '');
                            if (!data[key]) {
                                return i;
                            }

                            return data[key];
                        });
                    }
                } else if (typeof str === 'string' && data instanceof Array === false || typeof str === 'string' && data instanceof Object === false) {

                    return str;
                } else {

                    return false;
                }
            },
            showUpdateForm() {
                if (!this.currentUser || this.currentUser.id !== this.result.creator.id) {
                    return;
                }
                if (this.isUpdating) {
                    return;
                }
                // checking if other is updating
                if (this.focusedResult.id) {
                    return;
                }
                // if is adding note
                if (this.isAddingNote) {
                    return;
                }
                this.isUpdating = true;
                this.isFocusInput = true;
                setTimeout(() => {
                    let messageContentElement = document.getElementById("message-content-" + this.result.id);
                    messageContentElement.innerHTML = this.getMessageContent(this.result);
                    if (messageContentElement.innerText && messageContentElement.innerText.trim()) {
                        this.setCaret();
                    }
                }, 5);
                this.$emit('change-focus-item', this.result);
            },
            updateSystemNote() {
                let el = document.getElementById("message-content-" + this.result.id);
                let noteContent = el.innerText && el.innerText.trim();
                if (!noteContent) {
                    Common.alert("Note is required");
                    return;
                }
                if (this.isSubmitting) {
                    return;
                }
                this.isSubmitting = true;
                let systemNoteParamList = [];
                let messageParams = [];
                let messageContentElement = document.getElementById("message-content-" + this.result.id);
                let paramIndex = 0;
                messageContentElement.childNodes.forEach(node => {
                    if (node.nodeName.toLowerCase() === '#text') {
                        messageParams.push(node.data);
                    } else if (node.nodeName.toLowerCase() === 'a') {
                        messageParams.push(`{${paramIndex++}}`);
                        systemNoteParamList.push({
                            text: node.innerText,
                            objectId: node.dataset.objectId,
                            objectType: node.dataset.objectType
                        });
                    } else if (node.nodeName.toLowerCase() === 'div') {
                        let subMessageParams = ['<div>'];
                        node.childNodes.forEach(childNode => {
                            if (childNode.nodeName.toLowerCase() === '#text') {
                                subMessageParams.push(childNode.data);
                            } else if (childNode.nodeName.toLowerCase() === 'a') {
                                subMessageParams.push(`{${paramIndex++}}`);
                                systemNoteParamList.push({
                                    text: childNode.innerText,
                                    objectId: childNode.dataset.objectId,
                                    objectType: childNode.dataset.objectType
                                });
                            }
                        });
                        subMessageParams.push('</div>');
                        messageParams.push(subMessageParams.join(' '));
                    }
                });

                this.noteForm.message = messageParams.join(" ");
                this.noteForm.systemNoteParamList = systemNoteParamList;
                this.noteForm.systemNoteFunction = this.result.systemNoteFunction;
                this.noteForm.objectFunctionId = this.result.objectFunctionId;
                this.noteForm.objectType = this.result.objectType;

                axios.put(`/api/system-note/${this.result.id}`, this.noteForm).then((response) => {
                    this.result.message = response.data.message;
                    this.result.systemNoteParamList = response.data.systemNoteParamList;
                    this.isUpdating = false;
                    this.isFocusInput = false;
                    this.reloadList();
                    this.$emit('change-focus-item', {});
                }).catch(error => {
                    console.log('error', error);
                }).finally(() => {
                    this.isSubmitting = false;
                })
            },
            deleteSystemNote() {
                this.$emit('delete-system-note', this.result.id);
            },
            cancelUpdate() {
                this.isUpdating = false;
                this.isFocusInput = false;
                this.$emit('change-focus-item', {});
                this.hideSearchUserPopup();
            },
            focusInNoteInput() {
                this.isFocusInput = true;
                this.$emit('change-focus-item', this.result);
            },
            reloadList() {
                this.$emit('reload');
            },
            focusOutNoteInput() {
                this.isFocusInput = false;
            },
            selectUser(user, isFromClosest) {
                let messageContentElement = document.getElementById("message-content-" + this.result.id);
                if (isFromClosest) {
                    messageContentElement.removeChild(messageContentElement.lastChild);
                }
                let lastNode = messageContentElement.lastChild;
                let node = document.createElement('a');
                node.innerText = '@' + user.name;
                node.href="#";
                node.dataset.objectId = user.id;
                node.dataset.objectType = this.objectTypes.USER;
                if (lastNode.nodeName.toLowerCase() === '#text') {

                    if (!lastNode.data.trim() && messageContentElement.childNodes.length > 1 && messageContentElement.childNodes[messageContentElement.childNodes.length - 2].nodeName.toLowerCase() === 'div') {
                        lastNode = messageContentElement.childNodes[messageContentElement.childNodes.length - 2];
                    }
                    // second line and then
                    if (['div'].includes(lastNode.nodeName.toLowerCase())) {
                        lastNode.innerHTML = lastNode.innerHTML.substr(0, lastNode.innerHTML.length - this.tagText.length);
                        lastNode.appendChild(node);
                        messageContentElement.lastChild = lastNode;
                    }else { // first line
                        lastNode.data = lastNode.data.substr(0, lastNode.data.length - this.tagText.length);
                        messageContentElement.appendChild(node);
                    }
                }else if (lastNode.nodeName.toLowerCase() === 'a'){
                    lastNode.innerHTML = node.innerHTML;
                    messageContentElement.lastChild = lastNode;
                }else {
                    lastNode.innerHTML = lastNode.innerHTML.substr(0, lastNode.innerHTML.length - this.tagText.length);
                    lastNode.appendChild(node);
                    messageContentElement.lastChild = lastNode;
                }
                this.setCaret();
            },
            findClosestUser(){
                var child = Common.vue.getChild(this.$children, 'system-note-user');
                if (child != null) {
                    child.findClosestUser();
                }
            },
            setMessageContent(e) {
                this.checkAddNote = e.data;
                let sel = window.getSelection();
                let focusNode= sel.focusNode;
                let focusOffset = sel.focusOffset;

                let messageContentElement = document.getElementById("message-content-" + this.result.id);
                if (messageContentElement.childNodes.length === 0) {
                    this.hideSearchUserPopup();
                    return;
                }
                // let html = messageContentElement.innerHTML;
                // messageContentElement.innerHTML = html;
                let lastNode = messageContentElement.lastChild;
                let currentText = null;
                if (lastNode.nodeName.toLowerCase() === '#text') {
                    if (messageContentElement.childNodes.length > 1 && !lastNode.data.trim() && ['a', 'div'].includes(messageContentElement.childNodes[messageContentElement.childNodes.length - 2].nodeName.toLowerCase())) {
                        lastNode =  messageContentElement.childNodes[messageContentElement.childNodes.length - 2];
                        currentText = lastNode.innerText;
                    } else {
                        currentText = lastNode.data;
                    }

                } else {
                    currentText = lastNode.innerText;
                }
                if (e.inputType === 'deleteContentBackward') {
                    messageContentElement.childNodes.forEach((node, index) => {
                        if (node.nodeName.toLowerCase() === '#text') {
                            if (!node.data) {
                                messageContentElement.removeChild(node);
                            }
                        } else if (!node.innerText) {
                            messageContentElement.removeChild(node);
                        }
                    });
                    this.hideSearchUserPopup();

                    lastNode = messageContentElement.childNodes[messageContentElement.childNodes.length - 1];
                    if (!lastNode) {
                        return;
                    }
                    if (lastNode.nodeName.toLowerCase() === '#text') {
                        if ((!lastNode.data || !lastNode.data.trim())) {
                            let childNodes = messageContentElement.childNodes;
                            for(let i = childNodes.length - 2; i >=0; i--) {
                                if (childNodes[i].nodeName.toLowerCase() === 'a') {
                                    break;
                                }
                                if (childNodes[i].nodeName.toLowerCase() !== '#text' && childNodes[i].innerText) {
                                    lastNode = childNodes[i];
                                    break;
                                }
                            }
                        } else {
                            let words = lastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                            let lastWord = null;
                            if (words.length > 0) {
                                lastWord = words[words.length - 1];
                                if (lastWord[0] === '@') {
                                    this.tagText = lastWord;
                                    this.showSearchUserPopup(lastWord.substr(1, lastWord.length - 1), e);
                                }
                            }

                        }
                    } else if (lastNode.nodeName.toLowerCase() === 'a') {
                        messageContentElement.removeChild(lastNode);
                    } else if (lastNode.nodeName.toLowerCase() === 'div') {
                        let lastChildOfLastNode = lastNode.lastChild;
                        if (lastChildOfLastNode) {
                            if (lastChildOfLastNode.nodeName.toLowerCase() === 'a') {
                                lastNode.removeChild(lastChildOfLastNode);
                            }else if (lastChildOfLastNode.nodeName.toLowerCase() === '#text'){
                                let words = lastChildOfLastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                                let lastWord = null;
                                if (words.length > 0) {
                                    lastWord = words[words.length - 1];
                                    if (lastWord[0] === '@') {
                                        this.tagText = lastWord;
                                        this.showSearchUserPopup(lastWord.substr(1, lastWord.length - 1), e);
                                    }
                                }
                            }
                        }
                    }

                    sel = window.getSelection();
                    focusNode= sel.focusNode;
                    focusOffset = sel.focusOffset;
                } else {
                    let words = currentText.replaceAll(String.fromCharCode(160), ' ').split(' ');
                    let lastWord = null;
                    if (words.length > 0) {
                        lastWord = words[words.length - 1];
                    }
                    if (e.data !== ' ' && lastWord[0] === '@' && lastWord[lastWord.length - 1] !== ' ') {
                        this.tagText = lastWord;
                        this.showSearchUserPopup(lastWord.substr(1, lastWord.length - 1), e);
                    } else {
                        if (e.inputType === 'insertParagraph') {
                            lastNode = messageContentElement.childNodes[messageContentElement.childNodes.length - 2];
                            if (lastNode.nodeName.toLowerCase() === '#text') {
                                let words = lastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                                let lastWord = null;
                                if(words.length > 0) {
                                    lastWord = words[words.length - 1];
                                    if (lastWord[0] === '@') {
                                        this.findClosestUser();
                                        sel = window.getSelection();
                                        focusNode= sel.focusNode;
                                        focusOffset = sel.focusOffset;
                                        this.setCaret(false,focusNode,focusOffset);
                                        return;
                                    }
                                }
                            } else if (lastNode.nodeName.toLowerCase() === 'div') {
                                let lastChildOfLastNode = lastNode.lastChild;
                                if (lastChildOfLastNode && lastChildOfLastNode.nodeName === '#text') {
                                    let words = lastChildOfLastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                                    let lastWord = null;
                                    if(words.length > 0) {
                                        lastWord = words[words.length - 1];
                                        if (lastWord[0] === '@') {
                                            this.findClosestUser();
                                            sel = window.getSelection();
                                            focusNode= sel.focusNode;
                                            focusOffset = sel.focusOffset;
                                            this.setCaret(false,focusNode,focusOffset);
                                            return;
                                        }
                                    }
                                }
                            }
                        } else {
                            this.hideSearchUserPopup();
                        }
                    }
                    //for item code
                    if(currentText[currentText.length - 1] === String.fromCharCode(160)){
                        this.detectItemCode(0);
                    }
                    if (e.inputType === 'insertParagraph') {
                        this.detectItemCode(1);
                    }
                    if (!currentText) {
                        return;
                    }
                }

                this.setCaret(false,focusNode,focusOffset);
            },
            setCaret(goToLast,focusNode , focusOffset) {
                let el = document.getElementById("message-content-" + this.result.id);
                let range = document.createRange();
                let sel = window.getSelection();
                let lastNode = el.childNodes[el.childNodes.length - 1];
                if (goToLast || focusNode==null)
                    range.setStartAfter(lastNode);
                else
                    range.setStart(focusNode,focusOffset);

                range.collapse(true);
                sel.removeAllRanges();
                sel.addRange(range);
            },
            detectItemCode(offset) {
                let el = document.getElementById("message-content-" + this.result.id);
                let lastNode = el.childNodes[el.childNodes.length - offset - 1];
                if (lastNode.nodeName.toLowerCase() === '#text' || lastNode.nodeName.toLowerCase() === 'div' || offset) {
                    let arrText = '';
                    if (lastNode.nodeName.toLowerCase() === '#text') {
                        if (lastNode.data.trim()) {
                            arrText = lastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                        } else {
                            if (el.childNodes.length > 1 && el.childNodes[el.childNodes.length - 2].nodeName.toLowerCase() === 'div') {
                                lastNode = el.childNodes[el.childNodes.length - 2];
                            }

                            let lastChildOfLastNode = lastNode.childNodes[lastNode.childNodes.length - 1];
                            if (lastChildOfLastNode.nodeName.toLowerCase() === '#text') {
                                arrText = lastChildOfLastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                            } else {
                                arrText = lastChildOfLastNode.innerText.replaceAll(String.fromCharCode(160), ' ').split(' ');
                            }
                        }

                    } else {
                        let lastChildOfLastNode = lastNode.childNodes[lastNode.childNodes.length - 1];
                        if (!lastChildOfLastNode || lastChildOfLastNode.nodeName.toLowerCase() === 'a') {
                            return;
                        }
                        if (lastChildOfLastNode.nodeName.toLowerCase() === '#text') {
                            arrText = lastChildOfLastNode.data.replaceAll(String.fromCharCode(160), ' ').split(' ');
                        } else {
                            arrText = lastChildOfLastNode.innerText.replaceAll(String.fromCharCode(160), ' ').split(' ');
                        }
                    }

                    if ((arrText.length > 1 || (arrText.length === 1 && offset)) && arrText[arrText.length + offset - 2] != null && arrText[arrText.length + offset - 2].length > 1) {
                        let code = arrText[arrText.length + offset - 2];
                        let self = this;
                        axios.get(`/api/system-note/search-object-by-code?code=${code}`).then(response => {
                            if (response.data.dataList != null && response.data.dataList.length > 0) {
                                let paramCode = response.data.dataList[0];
                                let node = document.createElement('a');
                                node.innerText = paramCode.text;
                                node.href="#";
                                node.dataset.objectId = paramCode.objectId;
                                node.dataset.objectType = paramCode.objectType;
                                if (lastNode.nodeName.toLowerCase() === 'div') {
                                    let endIndex = lastNode.innerHTML.length - code.length;
                                    if (!offset) {
                                        endIndex -= '&nbsp;'.length;
                                    }
                                    lastNode.innerHTML = lastNode.innerHTML.substr(0, endIndex);
                                    lastNode.appendChild(node);
                                    el.lastChild = lastNode;
                                } else if (offset) {
                                    let endIndex = lastNode.data.length - code.length;
                                    lastNode.data = lastNode.data.substr(0, endIndex);
                                    lastNode.parentNode.insertBefore(node, lastNode.nextSibling);
                                    el.childNodes[el.childNodes.length - 2] = lastNode;
                                } else {
                                    let endIndex = el.innerHTML.length - '&nbsp;'.length - code.length;
                                    el.innerHTML = el.innerHTML.substr(0, endIndex);
                                    el.appendChild(node);
                                }

                                self.setCaret(true);
                            }
                        }).catch(error => {
                            console.log('error', error);
                        })
                    }
                }
            },
            getMessageContent(noteObject) {
                let message = noteObject.message;
                let arrParam = noteObject.systemNoteParamList;
                let arrParamStr = [];
                if (message != null) {
                    message = message.replaceAll('\n', '<br/>');
                }
                if (arrParam != null && arrParam.length > 0) {
                    arrParam.forEach(p => {
                        let paramStr = p.text;
                        // let hrefVal = p.menuKey != null ? (p.menuKey +'?param=' + p.objectId) : "#";
                        let hrefVal = p.menuKey != null ? (p.menuKey + '?objectId=' + p.objectId) : "javascript:void(0)";
                        paramStr = `<a  href="${hrefVal}" data-object-id="${p.objectId}" data-object-type="${p.objectType}">${paramStr}</a>`;
                        arrParamStr.push(paramStr);
                    });
                    message = this.stringInject(message, arrParamStr);
                }
                return message;
            },
            showSearchUserPopup(text, e) {
                var child = Common.vue.getChild(this.$children, 'system-note-user');
                if (child != null) {
                    child.showSearchUserPopup(text, e);
                }
            },
            hideSearchUserPopup() {
                var child = Common.vue.getChild(this.$children, 'system-note-user');
                if (child != null) {
                    child.hideSearchUserPopup();
                }
            },
        },
        mounted() {
        },
        watch: {
            clearUpdate(item) {
                this.cancelUpdate()
            },
        },
        computed: {

        }
    }
</script>

<style scoped>
</style>