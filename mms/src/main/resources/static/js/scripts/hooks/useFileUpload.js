/**
 * @template T, U
 * @callback OnSuccessCallback - Called with the response data and mutation variables when the mutation is successful.
 * @param {T} data - The response data.
 * @param {U} [variables] - The mutation variables.
 * @returns {void}
 */

/**
 * @template T
 * @callback OnErrorCallback - Called with the error and mutation variables when an error occurs in the mutation.
 * @param {Error} error - The error.
 * @param {T} [variables] - The mutation variables.
 * @returns {void}
 */

/**
 * @typedef {Object} FileUploadResult - The result of the axios request.
 * @property {string} id
 */

/**
 * @template T, U
 * @typedef {Object} FileUploadOptions - Options for the useFileUploadMutation hook.
 * @property {OnSuccessCallback<T, U>} [onSuccess] - Called with the response data and mutation variables when the mutation is successful.
 * @property {OnErrorCallback<U>} [onError] - Called with the error and mutation variables when an error occurs in the mutation.
 */

/**
 * A custom hook for handling file pre-upload.
 * @param {FileUploadOptions<FileUploadResult, File>} [options]
 */
function useFileUploadMutation(options) {
  const data = ref();
  const status = ref("idle");
  const variables = ref();
  const error = ref(null);

  const isIdle = computed(() => status.value === "idle");
  const isPending = computed(() => status.value === "pending");
  const isError = computed(() => status.value === "error");
  const isSuccess = computed(() => status.value === "success");

  /**
   * @param {File} file
   * @param {FileUploadOptions<FileUploadResult, File>} [options]
   */
  const mutateAsync = async (file, options) => {
    status.value = "pending";
    variables.value = file;
    const formData = new FormData();
    formData.append("file", file);
    try {
      const response = await axios.post("/api/common/fle-tmp", formData);
      data.value = response.data;
      error.value = null;
      status.value = "success";
      options?.onSuccess?.(response.data, file);
      return response.data;
    } catch (err) {
      console.error(err);
      status.value = "error";
      error.value = err;
      options?.onError?.(err, file);
    }
  };

  watch(status, (newVal) => {
    if (newVal === "success") options?.onSuccess?.(data.value, variables.value);
    if (newVal === "error") options?.onError?.(error.value, variables.value);
  });

  return {
    data,
    status,
    error,
    isIdle,
    isPending,
    isError,
    isSuccess,
    mutateAsync,
    mutate(payload, options) {
      mutateAsync(payload, options);
    },
  };
}
