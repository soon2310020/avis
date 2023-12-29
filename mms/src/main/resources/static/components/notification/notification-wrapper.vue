<script>
module.exports = {
  name: "notification-wrapper",
  components: {
    "emdn-notification-root": EmoldinoComponents.NotificationRoot,
    "emdn-notification-icon-button": EmoldinoComponents.NotificationIconButton,
    "emdn-notification-drawer": EmoldinoComponents.NotificationDrawer,
  },
  methods: {
    async setNotification(value) {
      await axios.post(`/api/common/not-bel/turn-on-off?on=${value}`);
    },
    async getNotifications({ page, size, category, status }) {
      const url = new URL("/api/common/not-bel", window.location.origin);
      if (page) url.searchParams.append("page", String(page));
      if (size) url.searchParams.append("size", String(size));
      if (category) url.searchParams.append("notiCategory", category);
      if (status) url.searchParams.append("notiStatus", status);

      const res = await axios.get(url);
      return res.data;
    },
    async readNotifications(category) {
      const url = new URL("/api/common/not-bel/read", window.location.origin);
      if (category) url.searchParams.append("notiCategory", category);

      const res = await axios.post(url);
      return res.data;
    },
    async readNotificationById(id) {
      const url = new URL(
        `/api/common/not-bel/${id}/read`,
        window.location.origin
      );
      const res = await axios.post(url);
      return res.data;
    },
  },
  setup() {
    const newNotification = useSubscribe(notificationCubit.notification$);
    const currentUser = useSystem("me");
    const isAdmin = computed(() => {
      return currentUser.value?.admin ?? false;
    });

    // TODO(sun.lee): Handle added notification payload data
    watchEffect(() => {
      console.log(
        "🚀 ~ file: notification-wrapper.vue:43 ~ watchEffect ~ newNotification.value:",
        newNotification.value
      );
    });

    return { newNotification, isAdmin };
  },
};
</script>
<template>
  <emdn-notification-root
    :is-admin-user="isAdmin"
    :added-notification="newNotification"
    :set-notification="setNotification"
    :get-notifications="getNotifications"
    :read-notifications="readNotifications"
    :read-notification-by-id="readNotificationById"
  >
    <template #trigger>
      <emdn-notification-icon-button />
    </template>
    <emdn-notification-drawer />
  </emdn-notification-root>
</template>
