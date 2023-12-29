/**
 * Notification Cubit built with RxJS
 * The idea is from BLoC pattern usually used in Flutter
 */
class NotificationCubit {
  notificationSubject = new rxjs.Subject();

  get notification$() {
    return this.notificationSubject.asObservable();
  }

  addNotification(notification) {
    this.notificationSubject.next(notification);
  }

  dispose() {
    this.notificationSubject.complete();
  }

  isNotification(notification) {
    return (
      notification &&
      notification.hasOwnProperty("id") &&
      notification.hasOwnProperty("notiCategory") &&
      notification.hasOwnProperty("notiCode") &&
      notification.hasOwnProperty("notiPriority") &&
      notification.hasOwnProperty("sentDateTime") &&
      notification.hasOwnProperty("senderId") &&
      notification.hasOwnProperty("senderName")
    );
  }
}

const notificationCubit = new NotificationCubit();
