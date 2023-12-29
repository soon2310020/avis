/**
 * @file This file creates a Firebase Cloud Messaging (FCM) instance
 * using [EMoldino firebase pakcage]{@link https://github.com/emoldino/emoldino-ui-components/issues/704}.
 */
const __tokenKey__ = "fcmToken";

const mmsNotification = new MmsNotification.MmsNotification({
  isFirebaseEnabled: !isChinaTimezone,
  serviceWorkerScriptURL: "/js/scripts/fragments/firebase-messaging-sw.js",
  serviceWorkerScope: "/js/scripts/fragments/",
});

async function sendFcmTokenToServer() {
  let fcmToken;
  try {
    fcmToken = await mmsNotification.getFcmToken();
    sessionStorage.setItem(__tokenKey__, fcmToken);
  } catch (error) {
    console.error("Error while getting FCM token", error);
    return;
  }

  try {
    await axios.post("/api/common/man-pag/login", {
      messagingToken: fcmToken,
    });
  } catch (error) {
    console.error("Error while sending FCM token to server", error);
  }
}

function removeFcmToken() {
  sessionStorage.removeItem(__tokenKey__);
}

(async () => {
  try {
    await mmsNotification.requestPermission();
  } catch (error) {
    console.error("Error while requesting notification permission:", error);
    return;
  }

  const fcmToken = sessionStorage.getItem(__tokenKey__);
  if (!fcmToken) await sendFcmTokenToServer();

  navigator.serviceWorker.addEventListener("message", (event) => {
    const payload = event.data;

    if (!notificationCubit.isNotification(payload.data)) return;
    const notification = payload.data;

    notificationCubit.addNotification({
      ...notification,
      id: Number(notification.id),
      content: payload.notification.body,
      notiStatus: "UNREAD",
    });
  });
})();
