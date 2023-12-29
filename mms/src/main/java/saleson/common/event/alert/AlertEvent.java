package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Mold;

public interface AlertEvent<T> {
	T get();

	AlertType getAlertType();

	Mold getMold();
}
