<template>
  <div class="note-body-wrapper">
    <template v-for="word in messageArray">
      <a-tooltip v-if="/({\d})/g.test(word)" placement="bottom">
            <template slot="title">
              <div style="padding: 6px 8px; font-size: 13px">
                <div>
                  <b>{{ getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId)?.name }}</b>
                </div>
                <div>
                  Company: <span v-if="getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId)?.companyName">{{ getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId).companyName }}</span>
                </div>
                <div>Department: {{ getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId)?.department }}</div>
                <div style="margin-bottom: 15px">Position: {{ getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId)?.position }}</div>
                <div>Email: {{ getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId)?.email }}</div>
                <div>Phone: {{ getUsers(systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.objectId)?.contactNumber }}</div>
              </div>
            </template>
            <span 
              style="background: #64D0C3; border-radius: 3px; padding: 2px 8px;font-size: 14.66px;color: #4B4B4B;">
              {{ systemNoteParamList[Number(word.match(/\d+/)[0], 10)]?.text }}
            </span>
          </a-tooltip>
      
      <span v-else style="word-break: break-all;">{{ word }}{{ [' ', ''].includes(word) ? '' : '&nbsp;' }}</span>
    </template>
  </div>
</template>

<script>
module.exports = {
  props: {
    message: String,
    systemNoteParamList: Array,
    disableLimit: Boolean,
    users: Array
  },
  computed: {
    messageArray() {
      const messages = [];
      let length = 0;
      const tagRegex = /({\d})/g;
      const newMessage = this.message.replaceAll('<div>', '\n').replaceAll('</div>', '')

      const tempMessage = this.disableLimit ? newMessage : newMessage.substring(0, 120)

      if (_.isEmpty(this.systemNoteParamList)) {
        return this.disableLimit ? [newMessage] : [this.truncateText(newMessage, 120)]
      }
      tempMessage.split(' ').every(word => {
        let wordLength = 0;
        if (tagRegex.test(word)) {
          wordLength += word.replace(tagRegex, i => this.systemNoteParamList[i])?.length
          if (wordLength + length > 120 && !this.disableLimit) {
            messages.push('...')
            return false
          } else {
            length += wordLength;
            messages.push(word);
          }
        } else {
          const newWord = word.replaceAll('<div>', '\n').replaceAll('</div>', '')
          const subWord = this.disableLimit ? newWord : newWord.substring(0, 120 - Math.min(length, 120))
          if (length + word?.length - subWord?.length > 120 && !this.disableLimit) {
            messages.push('...');
            return false
          } else {
            messages.push(subWord);
            length += subWord?.length + 1
          }
        }
        return true
      })
      return messages
    }
  },
  methods: {
    truncateText(text, size) {
      text = text && text.trim();
      if (text && text.length > size) {
        text = text.slice(0, size) + "..."
      }
      return text;
    },
    getUsers(id) {
      if (id && this.users) {
        const user = this.users.find(item => item.id === id)
        return user
      }
      return {}
    }
  },
};
</script>

<style scoped>
.preview-card-wrapper {
  height: 184px;
  background: #FFFFFF;
  border: 0.5px solid #8B8B8B;
  border-radius: 3px;
  position: relative;
}

.header-wrapper {
  background: #F5F8FF;
  border-bottom: 0.5px solid #8B8B8B;
  border-radius: 3px 3px 0px 0px;
  font-weight: 400;
  font-size: 11.25px;
  line-height: 13px;
  color: #4B4B4B;
  display: flex;
  align-items: center;
  padding: 9.5px 8px;
  justify-content: space-between;
  height: 33px;
}

.note-body-wrapper {
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  display: flex;
  color: #4B4B4B;
  padding: 16px;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
}

.btn-custom {
  padding: 3px 6px;
  display: inline-flex;
  align-items: center;
  height: 25px;
  width: 25px;
  position: absolute;
  ;
  bottom: 8px;
  left: 8px
}

.action-btn {
  background: #D9D9D9;
  border-color: transparent;
}

.action-btn:focus {
  background: #DAEEFF;
}

.action-btn:hover img {
  filter: sepia(206%) hue-rotate(163deg) saturate(900%);
}

.action-btn:hover {
  background: #DAEEFF;
}
</style>