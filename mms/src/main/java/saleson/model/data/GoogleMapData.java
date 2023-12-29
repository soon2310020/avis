package saleson.model.data;

import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Getter
public class GoogleMapData {

	private List<MapData> mapDataList = new ArrayList<>();			// Location Name

	public GoogleMapData(List<MapChartData> mapChartDataList) {
		convert(mapChartDataList);
	}

	private void convert(List<MapChartData> mapChartDataList) {
		for (MapChartData mapChartData : mapChartDataList) {
			Optional<MapData> mapData = mapDataList.stream()
					.filter(m -> m.getLocationName().equals(mapChartData.getLocationName()))
					.findFirst();

			if (mapData.isPresent()) {
				mapData.get().addData(mapChartData);
			} else {
				mapDataList.add(new MapData(mapChartData));
			}
		}
	}



	@Getter
	@Setter
	public class MapData {
		private String locationName;
		private Double latitude;
		private Double longitude;
		private String partName;
		private String companyName;
		private String address;
		private long active;
		private long idle;
		private long inactive;
		private long disconnected;

		private long sensorOffline;

		private long sensorDetached;

		public MapData(MapChartData mapChartData) {
			this.locationName = mapChartData.getLocationName();
			this.address = mapChartData.getAddress();
			this.partName = mapChartData.getPartName();

			addData(mapChartData);
		}

		public void addData(MapChartData mapChartData) {
			if (EquipmentStatus.DETACHED == mapChartData.getCounterEquipmentStatus()) {
				sensorDetached = sensorDetached +  mapChartData.getValue();
			} else if (OperatingStatus.DISCONNECTED == mapChartData.getCounterOperatingStatus()
					|| OperatingStatus.DISCONNECTED == mapChartData.getOperatingStatus() ) {
				sensorOffline = sensorOffline + mapChartData.getValue();
			} else if (OperatingStatus.WORKING == mapChartData.getOperatingStatus()) {
				active = active + mapChartData.getValue();
			} else if (OperatingStatus.IDLE == mapChartData.getOperatingStatus()) {
				idle = idle + mapChartData.getValue();
			} else if (OperatingStatus.NOT_WORKING == mapChartData.getOperatingStatus()) {
				inactive = inactive + mapChartData.getValue();
			}
		}
	}
}


