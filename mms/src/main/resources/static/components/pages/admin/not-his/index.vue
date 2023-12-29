<script>
module.exports = {
  components: {
    "emdn-notification-history": EmoldinoComponents.NotificationHistoryLog,
  },
  methods: {
    async getNotifications({ page, size, category }) {
      const url = new URL("/api/common/not-his", window.location.origin);
      if (page) url.searchParams.append("page", String(page));
      if (size) url.searchParams.append("size", String(size));
      if (category) url.searchParams.append("notiCategory", category);
      const res = await axios.get(url);
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
  // TODO(sun.lee): Refactor using custom hooks
  setup() {
    const currentUser = useSystem("me");
    const isAdmin = computed(() => {
      return currentUser.value?.admin ?? false;
    });

    return { isAdmin };
  },
};
</script>
<template>
  <div style="height: 100%; width: 100%; padding: 1rem">
    <emdn-notification-history
      :is-admin-user="isAdmin"
      :get-notifications="getNotifications"
      :read-notification-by-id="readNotificationById"
    />
  </div>
</template>
<style scoped></style>
