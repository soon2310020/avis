importScripts("/dist/mms-notification.umd.js");

/**
 * @file This file contains the service worker script for Firebase Cloud Messaging (FCM) using EMoldino firebase package.
 * Firebase SDK will automatically initialize Firebase Messaging in the Service Worker.
 * And Firebase SDK is used in [EMoldino firebase package]{@link https://github.com/emoldino/emoldino-ui-components/issues/704}.
 * @see {@link https://firebase.google.com/docs/cloud-messaging/js/receive#handle_messages_when_your_web_app_is_in_the_foreground}
 */

const mmsNotification = new MmsNotification.MmsNotification();

mmsNotification.onBackgroundMessage(async (payload) => {
  const { title, body } = payload.notification;

  if (title && body) {
    self.registration.showNotification(title, {
      body,
    });
  }

  const clients = await self.clients.matchAll({
    includeUncontrolled: true,
  });

  clients.forEach((client) => {
    client.postMessage(payload);
  });
});
