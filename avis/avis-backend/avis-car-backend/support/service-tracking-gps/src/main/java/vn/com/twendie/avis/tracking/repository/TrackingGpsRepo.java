package vn.com.twendie.avis.tracking.repository;

import vn.com.twendie.avis.tracking.model.payload.VehicleActivityPayload;
import vn.com.twendie.avis.tracking.model.payload.VehicleFeeStationPayload;
import vn.com.twendie.avis.tracking.model.payload.VehicleStopPayload;
import vn.com.twendie.avis.tracking.model.wrapper.VehicleActivityWrapper;
import vn.com.twendie.avis.tracking.model.wrapper.VehicleFeeStationWrapper;
import vn.com.twendie.avis.tracking.model.wrapper.VehicleStopWrapper;

public interface TrackingGpsRepo {

    VehicleActivityWrapper getVehicleActivities(VehicleActivityPayload payload) throws Exception;

    VehicleStopWrapper getVehicleStops(VehicleStopPayload payload) throws Exception;

    VehicleFeeStationWrapper getVehicleFeeStations(VehicleFeeStationPayload payload) throws Exception;

}
