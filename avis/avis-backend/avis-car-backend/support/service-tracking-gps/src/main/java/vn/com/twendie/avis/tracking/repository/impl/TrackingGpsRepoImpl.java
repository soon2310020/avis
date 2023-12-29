package vn.com.twendie.avis.tracking.repository.impl;

import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.api.core.util.OkHttpUtils;
import vn.com.twendie.avis.api.rest.exception.HttpException;
import vn.com.twendie.avis.tracking.config.TrackingGpsConfig;
import vn.com.twendie.avis.tracking.model.payload.Credential;
import vn.com.twendie.avis.tracking.model.payload.VehicleActivityPayload;
import vn.com.twendie.avis.tracking.model.payload.VehicleFeeStationPayload;
import vn.com.twendie.avis.tracking.model.payload.VehicleStopPayload;
import vn.com.twendie.avis.tracking.model.wrapper.VehicleActivityWrapper;
import vn.com.twendie.avis.tracking.model.wrapper.VehicleFeeStationWrapper;
import vn.com.twendie.avis.tracking.model.wrapper.VehicleStopWrapper;
import vn.com.twendie.avis.tracking.repository.TrackingGpsRepo;

import static vn.com.twendie.avis.tracking.constant.TrackingGpsConstant.ApiPath.*;

@Repository
public class TrackingGpsRepoImpl implements TrackingGpsRepo {

    private final TrackingGpsConfig trackingGpsConfig;
    private final OkHttpUtils okHttpUtils;

    public TrackingGpsRepoImpl(TrackingGpsConfig trackingGpsConfig, OkHttpUtils okHttpUtils) {
        this.trackingGpsConfig = trackingGpsConfig;
        this.okHttpUtils = okHttpUtils;
    }

    @Override
    public VehicleActivityWrapper getVehicleActivities(VehicleActivityPayload payload) throws Exception {
        return callTrackingApi(GET_VEHICLE_ACTIVITIES, payload, VehicleActivityWrapper.class);
    }

    @Override
    public VehicleStopWrapper getVehicleStops(VehicleStopPayload payload) throws Exception {
        return callTrackingApi(GET_VEHICLE_STOPS, payload, VehicleStopWrapper.class);
    }

    @Override
    public VehicleFeeStationWrapper getVehicleFeeStations(VehicleFeeStationPayload payload) throws Exception {
        return callTrackingApi(GET_VEHICLE_FEE_STATIONS, payload, VehicleFeeStationWrapper.class);
    }

    private <P extends Credential, R> R callTrackingApi(String path, P payload, Class<R> clazz) throws Exception {
        payload.setCredential(trackingGpsConfig.getCredential());
        Request request = okHttpUtils.createRequestBuilder()
                .url(trackingGpsConfig.getBaseUrl() + path)
                .post(okHttpUtils.createPostBody(payload))
                .build();
        Response response = okHttpUtils.execute(request);
        if (response.isSuccessful()) {
            return okHttpUtils.getResponseAsObject(response, clazz);
        } else {
            throw new HttpException("Call tracking api " + path + " failed with http code: " + response.code());
        }
    }

}
