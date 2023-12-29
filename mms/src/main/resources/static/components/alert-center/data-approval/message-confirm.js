module.exports = {
  name: "message-confirm",
  props: {
    resources: Object,
    isVisible: Boolean,
    data: Object,
    callbackMessageForm: Function,
  },
  setup(props, ctx) {
    console.log("message confirm setup");
    const modalTitle = ref(props.data?.title);

    // state
    const dataState = ref({
      param: {
        message: "",
      },
    });
    watch(
      () => props.data,
      (newVal) => {
        console.log("message-confirm props.data: ", newVal);
        modalTitle.value = newVal?.title;
        dataState.value = newVal;
      }
    );
    const message = ref("");

    const _cleanState = () => {
      dataState.value = {
        param: {
          message: "",
        },
      };
    };
    //api
    const submit = async () => {
      console.log("message: ", message);
      if (
        props.isRequiredMessage &&
        (!message.value || message.value?.trim() == "")
      ) {
        Common.alert("Please enter the message!");
        return false;
      }
      if (Array.isArray(dataState.value?.param)) {
        dataState.value.param = dataState.value?.param.map((value) => {
          value.message = message.value;
          return value;
        });
      } else {
        dataState.value.param.message = message.value;
      }
      if (dataState.value?.method == "POST") {
        try {
          const response = await axios.post(
            dataState.value?.url,
            dataState.value?.param
          );
          message.value = "";
          if (
            response.data != null &&
            !response.data.success &&
            response.data.message
          ) {
            Common.alert(response.data.message);
            return;
          }
          ctx.emit("close");
          props.callbackMessageForm();
        } catch (error) {
          console.log(error.response);
        }
        return;
      }
      try {
        const response = await axios.put(
          dataState.value?.url,
          dataState.value?.param
        );
        message.value = "";
        if (
          response.data != null &&
          !response.data.success &&
          response.data.message
        ) {
          Common.alert(response.data.message);
          return;
        }
        ctx.emit("close");
        props.callbackMessageForm();
      } catch (error) {
        console.log(error.response);
      }
    };
    // handler
    const handleCloseDialog = () => {
      ctx.emit("close");
      _cleanState();
    };

    const handleSubmit = () => submit();

    return {
      modalTitle,
      dataState,
      message,
      handleCloseDialog,
      handleSubmit,
    };
  },
};
