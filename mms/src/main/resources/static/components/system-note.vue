<template>
  <div class="system-note">
    <div
      style="overflow: auto"
      id="op-system-note"
      class="modal fade"
      role="dialog"
      aria-labelledby="exampleModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog" role="document">
        <div class="modal-content" v-show="contentType === contentTypes.LIST">
          <div
            class="trash-bin"
            @click="contentType = contentTypes.REMOVED"
            title="Trash bin"
          >
            <img src="/images/icon/bin.svg" alt="bin" />
          </div>
          <div class="note-header">
            <div class="note-icon">
              <img src="/images/icon/comment-blue.svg" alt="note" />
            </div>
            <div v-if="isMultiple" class="note-count">
              <span>Add Note</span>
            </div>
            <div
              v-else-if="noteCount === 1 || noteCount === 0"
              class="note-count"
            >
              {{ noteCount }} <span v-text="resources['note']"></span>
            </div>
            <div v-else class="note-count">
              {{ noteCount }} <span v-text="resources['notes']"></span>
            </div>
          </div>
          <div class="note-body">
            <div v-if="!isMultiple" class="list-note">
              <template v-for="result in results">
                <system-note-item
                  :result="result"
                  :current-user="currentUser"
                  :company-types="companyTypes"
                  :object-types="objectTypes"
                  :focused-result="focusedItem"
                  :clear-update="clearUpdate"
                  :is-adding-note="isAddingNote"
                  @delete-system-note="deleteSystemNote"
                  @reload="getSystemNoteData"
                  @change-focus-item="changeFocusItem"
                >
                </system-note-item>
              </template>
            </div>
            <div class="add-note-wrapper" v-show="isAddingNote">
              <div class="add-note-container" :class="{ focus: isFocusInput }">
                <div class="add-note-header">
                  <div class="author" v-if="currentUser">
                    <div
                      v-if="
                        currentUser.company.companyType ===
                        companyTypes.IN_HOUSE
                      "
                      class="author-icon note-icon-oem"
                    >
                      o
                    </div>
                    <div
                      v-if="
                        currentUser.company.companyType ===
                        companyTypes.SUPPLIER
                      "
                      class="author-icon note-icon-supplier"
                    >
                      s
                    </div>
                    <div
                      v-if="
                        currentUser.company.companyType ===
                        companyTypes.TOOL_MAKER
                      "
                      class="author-icon note-icon-toolmaker"
                    >
                      t
                    </div>
                    <div class="author-name">
                      {{ currentUser.name }} ({{ currentUser.company.name }})
                    </div>
                  </div>
                  <div class="right-header">
                    <div class="cancel-add-note">
                      <span @click="isAddingNote = false">Delete</span>
                    </div>
                    <div class="add-time" id="add-note-time">
                      2020-12-04 04:30:20
                    </div>
                  </div>
                </div>
                <div class="add-note-form">
                  <div class="add-note-content">
                    <div
                      class="system-note-shadow"
                      id="message-content"
                      @focus="focusInNoteInput"
                      @blur="focusOutNoteInput"
                      contenteditable="true"
                      @input="setMessageContent"
                      required
                    ></div>
                    <system-note-user
                      @select-user="selectUser"
                      id="add-user-popup"
                    ></system-note-user>
                  </div>
                </div>
              </div>
            </div>
            <div
              v-if="isAddingNote === false && resetAddNote === true"
              class="add-note-action"
              @click="showAddNoteForm"
            >
              <div class="add-note-icon">
                <img src="/images/icon/add-circle.svg" alt="add note" />
              </div>
              <div class="add-note-title" v-text="resources['add_note']"></div>
            </div>
            <div
              v-else-if="isAddingNote"
              class="add-note-action"
              @click="showAddNoteForm"
            >
              <span class="btn btn-success">Save Note</span>
            </div>
          </div>
          <div class="note-footer text-right">
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
              v-text="resources['close']"
            ></button>
          </div>
        </div>
        <div class="modal-content" v-if="contentType === contentTypes.REMOVED">
          <div class="note-header">
            <div class="note-icon">
              <img src="/images/icon/new_bin.svg" alt="note" />
            </div>
            <div
              v-if="removedTotal === 1 || removedTotal === 0"
              class="note-count"
            >
              {{ removedTotal }} deleted note
            </div>
            <div v-else class="note-count">
              {{ removedTotal }} deleted notes
            </div>
          </div>
          <div class="note-body">
            <div class="list-note">
              <div
                class="note-item restore"
                v-for="(removedResult, index) in removedResults"
                :key="index"
              >
                <div
                  class="restore-note"
                  @click="restoreSystemNote(removedResult.id)"
                  v-if="
                    currentUser && currentUser.id === removedResult.creator.id
                  "
                  v-text="resources['restore']"
                ></div>
                <div class="note-item-header">
                  <div class="author">
                    <div
                      v-if="removedResult.companyType === companyTypes.IN_HOUSE"
                      class="author-icon note-icon-oem"
                    >
                      o
                    </div>
                    <div
                      v-if="removedResult.companyType === companyTypes.SUPPLIER"
                      class="author-icon note-icon-supplier"
                    >
                      s
                    </div>
                    <div
                      v-if="
                        removedResult.companyType === companyTypes.TOOL_MAKER
                      "
                      class="author-icon note-icon-toolmaker"
                    >
                      t
                    </div>
                    <div class="author-name">
                      {{ removedResult.creator.name }} ({{
                        removedResult.companyName
                      }})
                    </div>
                  </div>
                  <div class="note-time">
                    {{ removedResult.createdDateTime }}
                  </div>
                </div>
                <div
                  class="note-item-content"
                  v-html="getMessageContent(removedResult)"
                ></div>
              </div>
            </div>
          </div>
          <div class="note-footer text-right">
            <button
              type="button"
              class="btn btn-primary"
              @click="contentType = contentTypes.LIST"
              v-text="resources['cancel']"
            ></button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
function stringInject(str, data) {
  if (typeof str === "string" && data instanceof Array) {
    return str.replace(/({\d})/g, function (i) {
      return data[i.replace(/{/, "").replace(/}/, "")];
    });
  } else if (typeof str === "string" && data instanceof Object) {
    if (Object.keys(data).length === 0) {
      return str;
    }

    for (var key in data) {
      return str.replace(/({([^}]+)})/g, function (i) {
        var key = i.replace(/{/, "").replace(/}/, "");
        if (!data[key]) {
          return i;
        }

        return data[key];
      });
    }
  } else if (
    (typeof str === "string" && data instanceof Array === false) ||
    (typeof str === "string" && data instanceof Object === false)
  ) {
    return str;
  } else {
    return false;
  }
}

module.exports = {
  props: {
    systemNoteFunction: String,
    resources: Object,
    handleSubmit: Function,
  },
  components: {
    "system-note-user": httpVueLoader("/components/system-note-user.vue"),
    "system-note-item": httpVueLoader("/components/system-note-item.vue"),
  },
  data() {
    return {
      resetAddNote: true,
      results: [],
      clearUpdate: 0,
      total: 0,
      removedResults: [],
      removedTotal: 0,
      contentTypes: {
        LIST: "LIST",
        REMOVED: "REMOVED",
        ADD: "ADD",
      },
      companyTypes: {
        IN_HOUSE: "IN_HOUSE",
        TOOL_MAKER: "TOOL_MAKER",
        SUPPLIER: "SUPPLIER",
      },
      contentType: "LIST",
      noteForm: {
        message: null,
        systemNoteFunction: null,
      },
      isSubmitting: false,
      // currentUser: null,
      paramid: {
        id: null,
      },
      objectTypes: {
        ACCESS_FEATURE: "ACCESS_FEATURE",
        ACCESS_GROUP: "ACCESS_GROUP",
        CATEGORY: "CATEGORY",
        COMPANY: "COMPANY",
        COUNTER: "COUNTER",
        LOCATION: "LOCATION",
        PART: "PART",
        TERMINAL: "TERMINAL",
        TOOLING: "TOOLING",
        USER: "USER",
      },
      isAddingNote: false,
      isFocusInput: false,
      tagText: null,
      objectType: null,
      objectTypeId: null,
      focusedItem: {},
      isMultiple: false,
    };
  },
  methods: {
    setAddNoteTime() {
      if (this.isMultiple) return;
      setInterval(function () {
        let element = document.getElementById("add-note-time");
        let datetime = moment().format("YYYY-MM-DD HH:mm:ss");
        if (element) element.innerHTML = `${datetime}`;
      }, 1000);
    },
    requestFormData: function () {
      var requestFormData = new FormData();
      requestFormData.append("payload", JSON.stringify(this.noteForm));
      return requestFormData;
    },
    showSystemNote(result, isMultiple, objectType) {
      console.log("@showSystemNote", {
        result,
        isMultiple,
        objectType,
        systemNoteFunction: this.systemNoteFunction,
      });
      this.isAddingNote = false;
      this.contentType = "LIST";
      this.resetNoteForm();

      this.isMultiple = Boolean(isMultiple);
      if (this.isMultiple) {
        // multiple
        this.noteForm.objectFunctionIds = result;
        if (objectType) this.objectType = objectType;
        delete this.noteForm.objectFunctionId;
      } else {
        // single
        this.clearUpdate += result.id;
        this.noteForm.objectFunctionId = result.id;
        delete this.noteForm.objectFunctionIds;
        this.getSystemNoteData();
        this.getSystemNoteRemovedData();
        this.objectType =
          this.objectTypes[this.systemNoteFunction] || result.objectType;
        this.objectTypeId = result.id;
        if (this.systemNoteFunction.toString().endsWith("_ALERT")) {
          if (result.objectType != null) {
            this.objectType = result.objectType;
          } else this.objectType = this.objectTypes.TOOLING;
        }
      }
      console.log(this.noteForm);
      $("#op-system-note").modal("show");
    },
    getSystemNoteData() {
      let params = {
        objectFunctionId: this.noteForm.objectFunctionId,
        systemNoteFunction: this.systemNoteFunction,
        trashBin: false,
      };
      if (this.systemNoteFunction.toString() == "DISCONNECTION_ALERT") {
        params.objectType = this.objectType || "";
      }
      params = Common.param(params);
      axios
        .get(`/api/system-note/list?${params}`)
        .then((response) => {
          this.results = response.data.dataList;
          this.total = response.data.total;
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    getSystemNoteRemovedData() {
      let params = {
        objectFunctionId: this.noteForm.objectFunctionId,
        systemNoteFunction: this.systemNoteFunction,
        trashBin: true,
      };
      if (this.systemNoteFunction.toString() == "DISCONNECTION_ALERT") {
        params.objectType = this.objectType || "";
      }
      params = Common.param(params);
      axios
        .get(`/api/system-note/list?${params}`)
        .then((response) => {
          this.removedResults = response.data.dataList;
          this.removedTotal = response.data.total;
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    deleteSystemNote(id) {
      axios
        .delete(`/api/system-note/${id}`)
        .then(() => {
          this.getSystemNoteData();
          this.getSystemNoteRemovedData();
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    restoreSystemNote(id) {
      axios
        .post(`/api/system-note/restore/${id}`)
        .then(() => {
          this.getSystemNoteData();
          this.getSystemNoteRemovedData();
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    async submitAddNote() {
      if (this.isSubmitting) {
        return;
      }
      console.log("@submitAddNote");
      this.isSubmitting = true;
      let systemNoteParamList = [];
      let messageParams = [];
      let messageContentElement = document.getElementById("message-content");
      let paramIndex = 0;
      messageContentElement.childNodes.forEach((node) => {
        if (node.nodeName.toLowerCase() === "#text") {
          messageParams.push(node.data);
        } else if (node.nodeName.toLowerCase() === "a") {
          messageParams.push(`{${paramIndex++}}`);
          systemNoteParamList.push({
            text: node.innerText,
            objectId: node.dataset.objectId,
            objectType: node.dataset.objectType,
          });
        } else if (node.nodeName.toLowerCase() === "div") {
          let subMessageParams = ["<div>"];
          node.childNodes.forEach((childNode) => {
            if (childNode.nodeName.toLowerCase() === "#text") {
              subMessageParams.push(childNode.data);
            } else if (childNode.nodeName.toLowerCase() === "a") {
              subMessageParams.push(`{${paramIndex++}}`);
              systemNoteParamList.push({
                text: childNode.innerText,
                objectId: childNode.dataset.objectId,
                objectType: childNode.dataset.objectType,
              });
            }
          });
          subMessageParams.push("</div>");
          messageParams.push(subMessageParams.join(" "));
        }
      });

      this.noteForm.message = messageParams.join(" ");
      this.noteForm.systemNoteParamList = systemNoteParamList;
      this.noteForm.objectType = this.objectType;

      try {
        console.log("@submitAddNote", this.isMultiple);
        if (this.isMultiple) {
          delete this.noteForm.objectFunctionId;
          if (this.handleSubmit) {
            this.noteForm.systemNoteParamList.menuKey = "";
            this.noteForm.params = this.noteForm.systemNoteParamList;
            const dataBody = {
              message: this.noteForm.message,
              params: this.noteForm.params,
            };
            await this.handleSubmit(dataBody);
          } else {
            await axios.post("/api/system-note/batch-create", this.noteForm);
          }
          this.isMultiple = false;
          $("#op-system-note").modal("hide");
          this.setToastAlertGlobal({
            title: "Success!",
            content: "New notes have been added.",
            show: true,
          });
        } else if (!this.isMultiple) {
          delete this.noteForm.objectFunctionIds;
          if (this.handleSubmit) {
            this.noteForm.systemNoteParamList.menuKey = "";
            this.noteForm.params = this.noteForm.systemNoteParamList;
            const dataBody = {
              message: this.noteForm.message,
              params: this.noteForm.params,
            };
            await this.handleSubmit(dataBody);
          } else {
            await axios.post(
              "/api/system-note/create-system-note",
              this.noteForm
            );
          }
          // single: refetch
          this.getSystemNoteData();
          this.resetNoteForm();
          this.contentType = this.contentTypes.LIST;
          this.setToastAlertGlobal({
            title: "Success!",
            content: "New note has been added.",
            show: true,
          });
        }
      } catch (error) {
        console.log(error);
      } finally {
        this.isSubmitting = false;
        this.isAddingNote = false;
      }
    },
    resetNoteForm() {
      this.noteForm.message = null;
      if (document.getElementById("message-content")) {
        document.getElementById("message-content").innerHTML = "";
      }
    },
    showSearchUserPopup(text, e) {
      var child = Common.vue.getChild(this.$children, "system-note-user");
      if (child != null) {
        child.showSearchUserPopup(text, e);
      }
    },
    hideSearchUserPopup() {
      var child = Common.vue.getChild(this.$children, "system-note-user");
      if (child != null) {
        child.hideSearchUserPopup();
      }
    },
    showAddNoteForm() {
      if (this.isAddingNote) {
        let noteContent =
          document.getElementById("message-content").innerText &&
          document.getElementById("message-content").innerText.trim();
        if (noteContent) {
          this.submitAddNote();
        } else {
          Common.alert("Note is required");
        }
      } else {
        this.resetNoteForm();
        this.hideSearchUserPopup();
        this.isAddingNote = true;
        document.getElementById("message-content").innerText = "\r";

        setTimeout(() => {
          this.initCaret();
        }, 100);
      }
    },
    initCaret() {
      var tag = document.getElementById("message-content");

      // Creates range object
      var setpos = document.createRange();

      // Creates object for selection
      var set = window.getSelection();

      // Set start position of range
      setpos.setStart(tag.childNodes[0], 0);

      // Collapse range within its boundary points
      // Returns boolean
      setpos.collapse(true);

      // Remove all ranges set
      set.removeAllRanges();

      // Add range with respect to range object.
      set.addRange(setpos);

      // Set cursor on focus
      tag.focus();
    },
    // async getCurrentUser() {
    //     try {
    //         const me = await Common.getSystem('me')
    //         this.currentUser = JSON.parse(me)
    //     } catch (error) {
    //         console.log(error)
    //     }
    // },
    getMessageContent(noteObject) {
      let message = noteObject.message;
      let arrParam = noteObject.systemNoteParamList;
      let arrParamStr = [];
      if (message != null) {
        message = message.replaceAll("\n", "<br/>");
      }
      if (arrParam != null && arrParam.length > 0) {
        arrParam.forEach((p) => {
          let paramStr = "";
          // if(p.objectType == 'USER'){
          //     paramStr='@'+p.text;
          // }else {
          //     paramStr = p.text;
          // }
          paramStr = p.text;
          let hrefVal = p.menuKey != null ? p.menuKey : "javascript:void(0)";
          paramStr = `<a  href="${hrefVal}" data-object-id="${p.objectId}" data-object-type="${p.objectType}">${paramStr}</a>`;
          // paramStr=`<a href="javascript:void(0)"">${paramStr}</a>`;
          arrParamStr.push(paramStr);
        });
        message = stringInject(message, arrParamStr);
      }
      return message;
    },
    getHtmlContent(s) {
      let val = s;
      if (s != null) val = s.replaceAll("\n", "<br/>");
      return val;
    },
    selectUser(user, isFromClosest) {
      let messageContentElement = document.getElementById("message-content");
      if (isFromClosest) {
        messageContentElement.removeChild(messageContentElement.lastChild);
      }
      let lastNode = messageContentElement.lastChild;
      let node = document.createElement("a");
      node.innerText = "@" + user.name;
      node.href = "#";
      node.dataset.objectId = user.id;
      node.dataset.objectType = this.objectTypes.USER;
      if (lastNode.nodeName.toLowerCase() === "#text") {
        if (
          !lastNode.data.trim() &&
          messageContentElement.childNodes.length > 1 &&
          messageContentElement.childNodes[
            messageContentElement.childNodes.length - 2
          ].nodeName.toLowerCase() === "div"
        ) {
          lastNode =
            messageContentElement.childNodes[
              messageContentElement.childNodes.length - 2
            ];
        }
        // second line and then
        if (["div"].includes(lastNode.nodeName.toLowerCase())) {
          lastNode.innerHTML = lastNode.innerHTML.substr(
            0,
            lastNode.innerHTML.length - this.tagText.length
          );
          lastNode.appendChild(node);
          messageContentElement.lastChild = lastNode;
        } else {
          // first line
          lastNode.data = lastNode.data.substr(
            0,
            lastNode.data.length - this.tagText.length
          );
          messageContentElement.appendChild(node);
        }
      } else if (lastNode.nodeName.toLowerCase() === "a") {
        lastNode.innerHTML = node.innerHTML;
        messageContentElement.lastChild = lastNode;
      } else {
        lastNode.innerHTML = lastNode.innerHTML.substr(
          0,
          lastNode.innerHTML.length - this.tagText.length
        );
        lastNode.appendChild(node);
        messageContentElement.lastChild = lastNode;
      }
      this.setCaret();
    },
    findClosestUser() {
      var child = Common.vue.getChild(this.$children, "system-note-user");
      if (child != null) {
        child.findClosestUser();
      }
    },
    setMessageContent(e) {
      let sel = window.getSelection();
      let focusNode = sel.focusNode;
      let focusOffset = sel.focusOffset;
      let messageContentElement = document.getElementById("message-content");
      if (messageContentElement.childNodes.length === 0) {
        this.hideSearchUserPopup();
        return;
      }
      // let html = messageContentElement.innerHTML;
      // messageContentElement.innerHTML = html;
      let lastNode = messageContentElement.lastChild;
      let currentText = null;
      if (lastNode.nodeName.toLowerCase() === "#text") {
        if (
          messageContentElement.childNodes.length > 1 &&
          !lastNode.data.trim() &&
          ["a", "div"].includes(
            messageContentElement.childNodes[
              messageContentElement.childNodes.length - 2
            ].nodeName.toLowerCase()
          )
        ) {
          lastNode =
            messageContentElement.childNodes[
              messageContentElement.childNodes.length - 2
            ];
          currentText = lastNode.innerText;
        } else {
          currentText = lastNode.data;
        }
      } else {
        currentText = lastNode.innerText;
      }
      if (e.inputType === "deleteContentBackward") {
        messageContentElement.childNodes.forEach((node, index) => {
          if (node.nodeName.toLowerCase() === "#text") {
            if (!node.data) {
              messageContentElement.removeChild(node);
            }
          } else if (!node.innerText) {
            messageContentElement.removeChild(node);
          }
        });
        this.hideSearchUserPopup();

        lastNode =
          messageContentElement.childNodes[
            messageContentElement.childNodes.length - 1
          ];
        if (!lastNode) {
          return;
        }
        if (lastNode.nodeName.toLowerCase() === "#text") {
          if (!lastNode.data || !lastNode.data.trim()) {
            let childNodes = messageContentElement.childNodes;
            for (let i = childNodes.length - 2; i >= 0; i--) {
              if (childNodes[i].nodeName.toLowerCase() === "a") {
                break;
              }
              if (
                childNodes[i].nodeName.toLowerCase() !== "#text" &&
                childNodes[i].innerText
              ) {
                lastNode = childNodes[i];
                break;
              }
            }
          } else {
            let words = lastNode.data
              .replaceAll(String.fromCharCode(160), " ")
              .split(" ");
            let lastWord = null;
            if (words.length > 0) {
              lastWord = words[words.length - 1];
              if (lastWord[0] === "@") {
                this.tagText = lastWord;
                this.showSearchUserPopup(
                  lastWord.substr(1, lastWord.length - 1),
                  e
                );
              }
            }
          }
        } else if (lastNode.nodeName.toLowerCase() === "a") {
          messageContentElement.removeChild(lastNode);
        } else if (lastNode.nodeName.toLowerCase() === "div") {
          let lastChildOfLastNode = lastNode.lastChild;
          if (lastChildOfLastNode) {
            if (lastChildOfLastNode.nodeName.toLowerCase() === "a") {
              lastNode.removeChild(lastChildOfLastNode);
            } else if (lastChildOfLastNode.nodeName.toLowerCase() === "#text") {
              let words = lastChildOfLastNode.data
                .replaceAll(String.fromCharCode(160), " ")
                .split(" ");
              let lastWord = null;
              if (words.length > 0) {
                lastWord = words[words.length - 1];
                if (lastWord[0] === "@") {
                  this.tagText = lastWord;
                  this.showSearchUserPopup(
                    lastWord.substr(1, lastWord.length - 1),
                    e
                  );
                }
              }
            }
          }
        }

        sel = window.getSelection();
        focusNode = sel.focusNode;
        focusOffset = sel.focusOffset;
      } else {
        let words = currentText
          .replaceAll(String.fromCharCode(160), " ")
          .split(" ");
        let lastWord = null;
        if (words.length > 0) {
          lastWord = words[words.length - 1];
        }
        if (
          e.data !== " " &&
          lastWord[0] === "@" &&
          lastWord[lastWord.length - 1] !== " "
        ) {
          this.tagText = lastWord;
          this.showSearchUserPopup(lastWord.substr(1, lastWord.length - 1), e);
        } else {
          if (e.inputType === "insertParagraph") {
            lastNode =
              messageContentElement.childNodes[
                messageContentElement.childNodes.length - 2
              ];
            if (lastNode.nodeName.toLowerCase() === "#text") {
              let words = lastNode.data
                .replaceAll(String.fromCharCode(160), " ")
                .split(" ");
              let lastWord = null;
              if (words.length > 0) {
                lastWord = words[words.length - 1];
                if (lastWord[0] === "@") {
                  this.findClosestUser();
                  sel = window.getSelection();
                  focusNode = sel.focusNode;
                  focusOffset = sel.focusOffset;
                  this.setCaret(false, focusNode, focusOffset);
                  return;
                }
              }
            } else if (lastNode.nodeName.toLowerCase() === "div") {
              let lastChildOfLastNode = lastNode.lastChild;
              if (
                lastChildOfLastNode &&
                lastChildOfLastNode.nodeName === "#text"
              ) {
                let words = lastChildOfLastNode.data
                  .replaceAll(String.fromCharCode(160), " ")
                  .split(" ");
                let lastWord = null;
                if (words.length > 0) {
                  lastWord = words[words.length - 1];
                  if (lastWord[0] === "@") {
                    this.findClosestUser();
                    sel = window.getSelection();
                    focusNode = sel.focusNode;
                    focusOffset = sel.focusOffset;
                    this.setCaret(false, focusNode, focusOffset);
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
        if (currentText[currentText.length - 1] === String.fromCharCode(160)) {
          this.detectItemCode(0);
        }
        if (e.inputType === "insertParagraph") {
          this.detectItemCode(1);
        }
        if (!currentText) {
          return;
        }
      }

      this.setCaret(false, focusNode, focusOffset);
    },
    setCaret(goToLast, focusNode, focusOffset) {
      let el = document.getElementById("message-content");
      let range = document.createRange();
      let sel = window.getSelection();
      let lastNode = el.childNodes[el.childNodes.length - 1];
      if (goToLast || focusNode == null) range.setStartAfter(lastNode);
      else range.setStart(focusNode, focusOffset);

      range.collapse(true);
      sel.removeAllRanges();
      sel.addRange(range);
    },
    detectItemCode(offset) {
      let el = document.getElementById("message-content");
      let lastNode = el.childNodes[el.childNodes.length - offset - 1];
      if (
        lastNode.nodeName.toLowerCase() === "#text" ||
        lastNode.nodeName.toLowerCase() === "div" ||
        offset
      ) {
        let arrText = "";
        if (lastNode.nodeName.toLowerCase() === "#text") {
          if (lastNode.data.trim()) {
            arrText = lastNode.data
              .replaceAll(String.fromCharCode(160), " ")
              .split(" ");
          } else {
            if (
              el.childNodes.length > 1 &&
              el.childNodes[el.childNodes.length - 2].nodeName.toLowerCase() ===
                "div"
            ) {
              lastNode = el.childNodes[el.childNodes.length - 2];
            }

            let lastChildOfLastNode =
              lastNode.childNodes[lastNode.childNodes.length - 1];
            if (lastChildOfLastNode.nodeName.toLowerCase() === "#text") {
              arrText = lastChildOfLastNode.data
                .replaceAll(String.fromCharCode(160), " ")
                .split(" ");
            } else {
              arrText = lastChildOfLastNode.innerText
                .replaceAll(String.fromCharCode(160), " ")
                .split(" ");
            }
          }
        } else {
          let lastChildOfLastNode =
            lastNode.childNodes[lastNode.childNodes.length - 1];
          if (
            !lastChildOfLastNode ||
            lastChildOfLastNode.nodeName.toLowerCase() === "a"
          ) {
            return;
          }
          if (lastChildOfLastNode.nodeName.toLowerCase() === "#text") {
            arrText = lastChildOfLastNode.data
              .replaceAll(String.fromCharCode(160), " ")
              .split(" ");
          } else {
            arrText = lastChildOfLastNode.innerText
              .replaceAll(String.fromCharCode(160), " ")
              .split(" ");
          }
        }

        if (
          (arrText.length > 1 || (arrText.length === 1 && offset)) &&
          arrText[arrText.length + offset - 2] != null &&
          arrText[arrText.length + offset - 2].length > 1 &&
          !this.isMultiple
        ) {
          let code = arrText[arrText.length + offset - 2];
          let self = this;
          axios
            .get(`/api/system-note/search-object-by-code?code=${code}`)
            .then((response) => {
              if (
                response.data.dataList != null &&
                response.data.dataList.length > 0
              ) {
                let paramCode = response.data.dataList[0];
                let node = document.createElement("a");
                node.innerText = paramCode.text;
                node.href = "#";
                node.dataset.objectId = paramCode.objectId;
                node.dataset.objectType = paramCode.objectType;
                if (lastNode.nodeName.toLowerCase() === "div") {
                  let endIndex = lastNode.innerHTML.length - code.length;
                  if (!offset) {
                    endIndex -= "&nbsp;".length;
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
                  let endIndex =
                    el.innerHTML.length - "&nbsp;".length - code.length;
                  el.innerHTML = el.innerHTML.substr(0, endIndex);
                  el.appendChild(node);
                }

                self.setCaret(true);
              }
            })
            .catch((error) => {
              console.log("error", error);
            });
        }
      }
    },
    focusInNoteInput() {
      this.isFocusInput = true;
    },
    focusOutNoteInput() {
      this.isFocusInput = false;
    },
    changeFocusItem(result) {
      this.focusedItem = result;

      result.id != null
        ? (this.resetAddNote = false)
        : (this.resetAddNote = true);
    },
  },
  mounted() {
    this.noteForm.systemNoteFunction = this.systemNoteFunction;
    this.setAddNoteTime();
    // this.getCurrentUser();
    this.$nextTick(function () {
      //no show pop up invite user
      let notificationType = Common.getParameter("notificationType");
      if (!["INVITE_USER"].includes(notificationType)) {
        this.paramid.id = Common.getParameter("param");
      }
    });
  },
  computed: {
    noteCount: function () {
      if (this.isAddingNote) {
        return this.total + 1;
      }
      return this.total;
    },
    currentUser() {
      return headerVm?.currentUser;
    },
  },
  watch: {
    "paramid.id": function (value) {
      if (this.paramid.id != "") {
        this.showSystemNote(this.paramid);
      }
    },
    systemNoteFunction: function (value) {
      this.noteForm.systemNoteFunction = this.systemNoteFunction;
    },
  },
};
</script>

<style scoped>
.btn-success {
  padding: 0.25rem 0.5rem;
}
</style>
