package saleson.api.user.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import saleson.model.User;

import java.io.IOException;

public class UserAlertSerializer extends JsonSerializer<User> {
	@Override
	public void serialize(User user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("id", String.valueOf(user.getId()));
		gen.writeStringField("email", user.getEmail());
		gen.writeStringField("mobileNumber", user.getMobileDialingCode() + user.getMobileNumber());
		gen.writeEndObject();
	}
}
