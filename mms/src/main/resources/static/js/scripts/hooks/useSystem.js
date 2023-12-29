/**
 * It parses a value and return the parsed value or the original value.
 * If the value is not a valid JSON string, it returns the original value.
 */
JSON.safeParse = (jsonStr) => {
  try {
    return JSON.parse(jsonStr);
  } catch (_) {
    return jsonStr;
  }
};

/**
 * Get system information.
 *
 * If session storage has the system information, it will use the system information from session storage.
 * If it doesn't have the system information, it will get the system information from the API.
 *
 * The key should be one of the following: "server", "type", "localTimeZone", "language", "me", "options", "messages", "versions", "menuPermissions" or undefined.
 * @param {string} key - The key of the system information
 */
function useSystem(key) {
  const systemKeys = [
    "server",
    "type",
    "localTimeZone",
    "language",
    "me",
    "options",
    "messages",
    "versions",
    "menuPermissions",
  ];

  if (key && !systemKeys.includes(key)) {
    console.error(
      'The key must be one of the following: "server", "type", "localTimeZone", "language", "me", "options", "messages", "versions", "menuPermissions"'
    );
    return;
  }

  const systemInfo = ref({});
  const system = computed(() => {
    if (!key) return systemInfo.value;
    return systemInfo.value[key];
  });

  const getSystem = () => {
    return systemKeys.reduce((acc, key) => {
      acc[key] = JSON.safeParse(sessionStorage.getItem(key));
      return acc;
    }, {});
  };

  const setSystemFromSessionStorage = () => {
    systemInfo.value = getSystem();
  };

  const setSystemFromApi = async () => {
    try {
      const response = await axios.get("/api/common/man-pag");
      systemInfo.value = response.data;
    } catch (error) {
      console.error("🚀 ~ file: useSystem.js:46 ~ onMounted ~ error:", error);
    }
  };

  onMounted(() => {
    window.addEventListener("storage", setSystemFromSessionStorage);
    const systemFromStorage = getSystem();

    const isEmpty = Object.values(systemFromStorage).some((value) => !value);
    if (!isEmpty) {
      systemInfo.value = systemFromStorage;
      return;
    }

    setSystemFromApi();
  });

  onUnmounted(() => {
    window.removeEventListener("storage", setSystemFromSessionStorage);
  });

  return readonly(system);
}
