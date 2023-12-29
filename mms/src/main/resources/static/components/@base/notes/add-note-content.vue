<template>
  <div class="add-note-content" style="position: relative">
    <div
      class="system-note-shadow"
      style="position: unset; min-height: 88px"
      ref="message-content"
      @focus="focusInNoteInput"
      @blur="focusOutNoteInput"
      contenteditable="true"
      @input="setMessageContent"
      :data-text="resources['type_to_tag_a_user']"
      required
    ></div>
    <user-note-list
      @select-user="selectUser"
      id="add-user-popup"
      :users="users"
    ></user-note-list>
    <div
      style="
        display: flex;
        justify-content: flex-end;
        margin: 7px 8px;
        align-items: center;
      "
    >
      <span style="margin-right: 8px">{{ messageLengthTxt }}</span>
      <base-button
        level="secondary"
        style="margin-right: 8px"
        @click="cancelAddNote"
        >{{ resources["cancel"] }}</base-button
      >
      <base-button
        level="primary"
        @click="addNote"
        :disabled="messageLength === 0 || messageLength > 180"
        >{{
          isReplies ? resources["reply"] : resources["add_note"]
        }}</base-button
      >
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    objectFunction: Object,
    systemNoteFunction: String,
    parentId: Number,
    users: Array,
    handleSubmit: Function,
  },
  components: {
    "user-note-list": httpVueLoader(
      "/components/@base/notes/user-note-list.vue"
    ),
  },
  data() {
    return {
      isFocusInput: false,
      messageLength: 0,
      noteForm: {
        message: null,
        systemNoteFunction: null,
      },
      typingTimmer: null,
    };
  },
  computed: {
    messageLengthTxt() {
      return this.messageLength + "/180";
    },
    isReplies() {
      return !!this.parentId;
    },
  },
  methods: {
    cancelAddNote() {
      this.clearNote();
      this.$emit("on-cancel");
    },
    clearNote() {
      this.$refs["message-content"].innerHTML = "";
      this.messageLength = 0;
    },
    loadNotePayload() {
      let systemNoteParamList = [];
      let messageParams = [];
      let messageContentElement = this.$refs["message-content"];
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
        } else if (["span", "pre"].includes(node.nodeName.toLowerCase())) {
          messageParams.push(node.innerText);
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
      this.noteForm.objectType = "USER";
      this.noteForm.objectFunctionId = this.objectFunction?.id;
      this.noteForm.systemNoteFunction = this.systemNoteFunction;
      if (this.parentId) {
        this.noteForm.parentId = this.parentId;
      }
    },
    async addNote() {
      this.loadNotePayload();
      if (this.objectFunction) {
        try {
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
          this.clearNote();
          this.$emit("on-add-note");
          this.$emit("on-cancel");
        } catch (error) {
          console.log("add-note-content:::error", error);
        } finally {
          this.isSubmitting = false;
          this.isAddingNote = false;
        }
      } else {
        this.$emit("on-add-note", this.noteForm);
      }
    },
    focusInNoteInput() {
      this.isFocusInput = true;
    },
    focusOutNoteInput() {
      this.onFinishTyping();
      this.isFocusInput = false;
    },
    hideSearchUserPopup() {
      var child = Common.vue.getChild(this.$children, "user-note-list");
      if (child != null) {
        child.hideSearchUserPopup();
      }
    },
    showSearchUserPopup(text, e) {
      var child = Common.vue.getChild(this.$children, "user-note-list");
      if (child != null) {
        child.showSearchUserPopup(text, e);
      }
    },
    findClosestUser() {
      var child = Common.vue.getChild(this.$children, "user-note-list");
      if (child != null) {
        child.findClosestUser();
      }
    },
    selectUser(user, isFromClosest) {
      let messageContentElement = this.$refs["message-content"];
      if (isFromClosest) {
        messageContentElement.removeChild(messageContentElement.lastChild);
      }
      let lastNode = messageContentElement.lastChild;
      let node = document.createElement("a");
      node.innerText = "@" + user.name;
      node.href = "#";
      node.dataset.objectId = user.id;
      node.dataset.objectType = "USER";
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
    setCaret(goToLast, focusNode, focusOffset) {
      let el = this.$refs["message-content"];
      this.messageLength = el.textContent?.length || 0;

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
      let el = this.$refs["message-content"];
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
          arrText[arrText.length + offset - 2].length > 1
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

    setMessageContent(e) {
      let sel = window.getSelection();
      let focusNode = sel.focusNode;
      let focusOffset = sel.focusOffset;
      let messageContentElement = this.$refs["message-content"];
      this.messageLength = messageContentElement.textContent?.length || 0;
      if (messageContentElement.childNodes.length === 0) {
        this.hideSearchUserPopup();
        return;
      }
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
          this.onFinishTyping();
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
                  this.onFinishTyping();
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
                    this.onFinishTyping();
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
          this.onFinishTyping();
          return;
        }
      }

      this.setCaret(false, focusNode, focusOffset);
      this.onFinishTyping();
    },
    onFinishTyping() {
      clearTimeout(this.typingTimmer);
      this.typingTimmer = setTimeout(() => {
        this.typingTimmer = undefined;
        console.log("change message here");
        this.loadNotePayload();
        this.$emit("change-message", this.noteForm);
      }, 1000);
    },
  },
};
</script>

<style scoped>
[contentEditable="true"]:empty:not(:focus):before {
  content: attr(data-text);
}
.system-note-shadow:focus,
.system-note-shadow:focus {
  outline: none;
}
*:focus {
  outline: none;
}
</style>
