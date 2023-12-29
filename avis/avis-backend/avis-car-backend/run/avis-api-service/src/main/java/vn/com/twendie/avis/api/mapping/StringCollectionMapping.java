package vn.com.twendie.avis.api.mapping;

import org.apache.commons.collections4.CollectionUtils;
import vn.com.twendie.avis.data.enumtype.CustomerIdCardEnum;

import java.util.Collection;
import java.util.Objects;

public class StringCollectionMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        Collection<String> collection = (Collection<String>) value;
        return CollectionUtils.isEmpty(collection) ? "" : String.join("\n", collection);
    }

}
