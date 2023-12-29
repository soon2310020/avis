package saleson.service.googleapi;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.Property;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import saleson.api.location.payload.LocationPayload;
import saleson.common.util.StringUtils;
import saleson.model.Mold;

@Service
public class AddressGeocoderService {

    @Value("${geocoder.api.key}")
    String apiKey;
    @Value("${geocoder.api.language}")
    String language;

    public LocationPayload findAddress(String address){
//        String address = "1600 Amphitheatre Parkway, Mountain View, CA";
//        String apiKey = "YOUR_API_KEY_HERE"; // replace with your own Google Maps API key

        try {
            LocationPayload locationPayload =  new LocationPayload();
            // create a new GeoApiContext with the API key
            GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).connectTimeout(10, TimeUnit.SECONDS).build();

            // use the GeocodingApi to get the latitude and longitude coordinates for the address
            GeocodingResult[] results = GeocodingApi.geocode(context, address)
                .language(language).await();
            if(results.length==0){
//                return null;
                throw new BizException("Address cannot be found on google map. Please correct your address", "AddressGeocoder");
            }
            LatLng location = results[0].geometry.location;
            AddressComponent[] addressComponents = results[0].addressComponents;
            if(addressComponents.length>0)
                Arrays.stream(addressComponents).forEach(addressComponent -> {
                   boolean isCountry= Arrays.stream(addressComponent.types).anyMatch(type -> AddressComponentType.COUNTRY.equals(type));
                    if (isCountry)
                        locationPayload.setCountryCode(addressComponent.shortName);
                });
            if (StringUtils.isEmpty(locationPayload.getCountryCode())) return null;
            double latitude = location.lat;
            double longitude = location.lng;
            locationPayload.setLatitude(latitude);
            locationPayload.setLongitude(longitude);


            // print the latitude and longitude coordinates
            System.out.println("countryCode: " + locationPayload.getCountryCode());
            System.out.println("Latitude: " + latitude);
            System.out.println("Longitude: " + longitude);
            return locationPayload;
        } catch (BizException e) {
            LogUtils.saveErrorQuietly(e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            LogUtils.saveErrorQuietly(e);
            e.printStackTrace();
//            throw new BizException(e.getMessage(), "AddressGeocoder");
        }
        return null;
    }
}
