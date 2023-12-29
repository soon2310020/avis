/**
 * This function subscribes to an RxJS Observable and returns the result.
 * @param observable - RxJS Observable
 * @returns - The result of the subscription
 */
const useSubscribe = (observable) => {
  const result = ref();

  watchEffect((cleanup) => {
    const subscription = observable.subscribe((value) => {
      result.value = value;
    });
    cleanup(() => subscription.unsubscribe());
  });

  return readonly(result);
};
