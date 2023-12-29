package saleson.common.context;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;

public class EmoldinoResourceMessageBundleSource extends ReloadableResourceBundleMessageSource {

	@Override
	protected Properties loadProperties(Resource resource, String fileName) throws IOException {
		return super.loadProperties(resource, fileName);
	}

	public Properties getMessages(Locale locale) {
		return getMergedProperties(locale).getProperties();
	}

}
