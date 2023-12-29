<template>
  <div
    style="z-index: 9999"
    id="op-warning-modal"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-hidden="true"
  >
    <div
      class="modal-dialog modal-dialog-centered"
      role="document"
    >
      <div
        style="box-shadow: 0 0 3px #00000029"
        class="modal-content"
      >
        <div class="modal-body">
          <div
            style="display: flex; align-items: center; margin-bottom: 5px"
            class="modal-title"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="11.444"
              height="10.097"
              viewBox="0 0 11.444 10.097"
            >
              <path
                id="Icon_awesome-exclamation-triangle"
                data-name="Icon awesome-exclamation-triangle"
                d="M11.315,8.677a.947.947,0,0,1-.826,1.42H.955a.947.947,0,0,1-.826-1.42L4.9.473a.958.958,0,0,1,1.652,0Zm-5.593-1.7a.907.907,0,1,0,.914.907A.911.911,0,0,0,5.722,6.981ZM4.854,3.72,5,6.4a.238.238,0,0,0,.238.224H6.2A.238.238,0,0,0,6.442,6.4L6.59,3.72a.237.237,0,0,0-.238-.25H5.092a.237.237,0,0,0-.238.25Z"
                transform="translate(0)"
                fill="#db3b21"
              />
            </svg>
            <b style="
                margin-left: 5px;
                font-size: 14px;
                color: #4b4b4b;
                line-height: 100%;
              ">{{ title }}
            </b>
          </div>
          <div class="custom-modal-info">
            <span>{{ description }}</span>
          </div>
          <div
            class="custom-modal-confirm-zone warning-action"
            style="justify-content: flex-end;"
          >
            <a
              id="warning-danger-btn"
              href="javascript:void(0)"
              style="margin-right: 4px;"
              class="custom-dropdown-button"
              @click="showWarningAnimation('warning-danger-btn', false)"
            >Cancel</a>
            <a
              id="warning-confirm-btn"
              href="javascript:void(0)"
              class="warning-button warning-button-confirm"
              @click="showWarningAnimation('warning-confirm-btn', true)"
            >Change Input Frequency</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    title: String,
    description: String,
    confirm: Function
  },
  data() {
    return {
    };
  },
  methods: {
    callConfirm() {
      setTimeout(() => {
        this.confirm()
        this.onCloseModal()
      }, 700)
    },
    handleClickOutside(event) {
      console.log($(event.target).closest('#op-warning-modal .modal-content').length)
      if (!$(event.target).closest('#op-warning-modal .modal-content').length) {
        console.log('click outside')
        this.$emit('cancel')
        this.onCloseModal()
      }
    },
    showDeletePopup() {
      $("#op-warning-modal").modal("show");
      $('#op-warning-modal').on('click', this.handleClickOutside)
    },
    onCloseModal() {
      this.$emit('close')
      $("#op-warning-modal").modal("hide");
      $("#op-warning-modal").off('click', this.handleClickOutside)
    },
    animationPrimary() {
      $('.animationPrimary').click(function () {
        $(this).addClass('primary-animation');
        $(this).one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function (event) {
          $(this).removeClass('primary-animation')
        });
      });
    },
    showWarningAnimation(id, type) {
      const el = document.getElementById(id);
      if (el) {
        if (type) {
          el.classList.add("warning-button-success-animation");
          setTimeout(() => {
            el.classList.remove("warning-button-success-animation");
            this.callConfirm();
            this.onCloseModal()
          }, 700);
        } else {
          el.classList.add("custom-dropdown-button-animation");
          setTimeout(() => {
            el.classList.remove("custom-dropdown-button-animation");
            // this.handleClickOutside()
            this.onCloseModal()
            this.$emit('cancel')
          }, 700);
        }
      }
    },
  },
  mounted() {
    this.$nextTick(function () {
      // 모든 화면이 렌더링된 후 실행합니다.
      //this.paging(1);
      this.animationPrimary();
    });
  }
};
</script>
<style scoped>
.modal-content {
  width: 353px;
  margin: auto;
  padding: 5px;
  border-radius: 3px !important;
}
.warning-button {
  font-size: 14px;
  padding: 6px 8px;
  border-radius: 3px;
  text-decoration: none !important;
}
.warning-button-default {
  color: #909090;
  background-color: #fff;
  box-shadow: 0 0 0 0.1rem #909090;
}
.warning-button-default:hover {
  color: #909090;
  background: #f8f8f8 !important;
}
.warning-button-default-animation {
  animation: default-button-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: none;
}
@keyframes default-button-animation {
  0% {
  }
  33% {
    box-shadow: 0 0 0 0.2rem #909090;
  }
  66% {
    box-shadow: 0 0 0 0.2rem #909090;
  }
  100% {
  }
}
.warning-button-confirm {
  color: #fff;
  background-color: #47b576;
}
.warning-button-confirm:hover {
  color: #fff;
  background-color: #3ea662;
  border-color: #3a9d5d;
}
.warning-button-success-animation {
  animation: success-button-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: none;
}
@keyframes success-button-animation {
  0% {
  }
  33% {
    box-shadow: 0 0 0 0.2rem rgb(77 189 116 / 50%);
  }
  66% {
    box-shadow: 0 0 0 0.2rem rgb(77 189 116 / 50%);
  }
  100% {
  }
}
.custom-dropdown-button {
  width: fit-content;
  height: fit-content;
  padding: 6px 8px;
  border-radius: 3px;
  background-color: #ffffff;
  border: unset !important;
  outline: 1px solid #909090;
  color: #4b4b4b;
  display: flex;
  align-items: center;
  text-decoration: none !important;
}
.custom-dropdown-button:hover {
  outline: 1px solid #4b4b4b;
}

@keyframes custom-dropdown-button-primary-animation {
  0% {
  }
  33% {
    outline: 3px solid #909090;
  }
  66% {
    outline: 3px solid #909090;
  }
  0% {
  }
}
.custom-dropdown-button-animation {
  animation: custom-dropdown-button-primary-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  outline: 1px solid transparents;
}
</style>