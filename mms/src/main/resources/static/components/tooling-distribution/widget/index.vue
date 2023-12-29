<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #body>
      <div
        style="position: relative"
        class="distribution-tooling-map"
        ref="distributionToolingMap"
      ></div>
    </template>
  </emdn-widget>
</template>

<script>
/**
 * reference:
 * https://developers.google.com/maps/documentation/javascript/examples/map-geolocation
 * https://developers.google.com/earth-engine/apidocs/map-setzoom
 * https://developers.google.com/maps/documentation/javascript/examples/overlay-popup
 * https://developers.google.com/maps/documentation/javascript/examples/control-positioning
 */

// CONSTANTS
const MARKER_ICON_ACTIVE = {
  url: "/images/icon/red-pin.svg",
  scaledSize: new google.maps.Size(28, 48),
};

const ZOOM_DEGREE = {
  XXL: 1, // See the entire world
  XL: 3,
  LG: 7,
  MD: 12,
  XS: 24, // samllest posible
};

// UTILITIES
const handleLocationError = (browserHasGeolocation, infoWindow, pos) => {
  infoWindow.setPosition(pos);
  infoWindow.setContent(
    browserHasGeolocation
      ? "Error: The Geolocation service failed."
      : "Error: Your browser doesn't support geolocation."
  );
  infoWindow.open(map);
};

const popupRender = (__location) => {
  console.log("popupRender__location", __location);
  const mapDataStyle = `margin-top: 0; margin-bottom: 0.25rem;`;
  const companyName = __location.companyName ?? "Company";
  const locationName = __location.locationName ?? "Location";
  const total = __location.total ?? 0;
  const active = __location.active ?? 0;
  const idle = __location.idle ?? 0;
  const inactive = __location.inactive ?? 0;
  const sensorOffline = __location.sensorOffline ?? 0;
  const sensorDetached = __location.sensorDetached ?? 0;
  const hyperlink = {
    active: `/admin/molds?equipmentStatus=ALL&dashboardRedirected=true&moldStatusList=IN_PRODUCTION&locationName=${locationName}`,
    idle: `/admin/molds?equipmentStatus=ALL&dashboardRedirected=true&moldStatusList=IDLE&locationName=${locationName}`,
    inactive: `/admin/molds?equipmentStatus=ALL&dashboardRedirected=true&moldStatusList=NOT_WORKING&locationName=${locationName}`,
    sensorOffline: `/admin/molds?equipmentStatus=ALL&dashboardRedirected=true&moldStatusList=SENSOR_OFFLINE&locationName=${locationName}`,
    sensorDetached: `/admin/molds?equipmentStatus=ALL&dashboardRedirected=true&moldStatusList=SENSOR_DETACHED&locationName=${locationName}`,
  };
  return `
    <div style="width: 300px;">
        <div class="map-title"> ${
          companyName || "Company"
        } at ${locationName} </div>
        <div class="map-data">
            <span class="map-data-total-number"><span style="font-weight: 600;font-size: 13px;">Total tooling: ${total} </span></span>
        </div>
        <div class="map-data">
            <span class="map-data-title"><span class="op-active op-active-sm label label-success"></span>In Production</span>
            <span class="map-data-count">
                <a href="${
                  hyperlink.active
                }" id="animation-one" class="invite-link"> ${active} </a>
            </span>
        </div>
        <div class="map-data">
            <span class="map-data-title"><span class="op-active op-active-sm label label-warning"></span>Idle</span>
            <span class="map-data-count">
                <a href="${
                  hyperlink.idle
                }" id="animation-one" class="invite-link"> ${idle} </a>
            </span>
        </div>
        <div class="map-data">
            <span class="map-data-title"><span class="op-active op-active-sm label label-inactive"></span>Inactive</span>
            <span class="map-data-count">
                <a href="${
                  hyperlink.inactive
                }" id="animation-one" class="invite-link"> ${inactive} </a>
            </span>
        </div>
        <div class="map-data">
            <span class="map-data-title"><span class="op-active op-active-sm label label-danger"></span>Sensor Offline</span>
            <span class="map-data-count">
                <a href="${
                  hyperlink.sensorOffline
                }" id="animation-one" class="invite-link"> ${sensorOffline} </a>
            </span>
        </div>
        <div class="map-data">
            <span class="map-data-title"><span class="op-active op-active-sm label label-detached"></span>Sensor Detached</span>
            <span class="map-data-count">
                <a href="${
                  hyperlink.sensorDetached
                }" id="animation-one" class="invite-link"> ${sensorDetached} </a>
            </span>
        </div>  
    </div>
    `;
};

module.exports = {
  name: "ToolingDistributionWidget",
  props: {
    key: String,
    label: String,
  },
  data() {
    return {
      map: null,
      markerCoordinates: [],
      bounds: null,
      markers: [],
      zoom: 12,
      zoomControl: true,
      infoWindow: null,
      geocoder: null,
      center: { lat: 37.7749, lng: -122.4194 },
      search: "",
      debounceSearch: null,
      scaleControl: false,
      streetViewControl: false,
      mapTypeControl: false,
    };
  },
  methods: {
    // api
    async fetchData() {
      try {
        const response = await axios.get("/api/common/dsh/tol-dist");
        if (response?.data?.length) {
          this.addMarkers(response.data);
        }
      } catch (error) {
        return error;
      }
    },
    async findLocation(
      params /** accept: address, location, placeId */,
      { first = false, data }
    ) {
      try {
        const response = await this.geocoder.geocode(params);
        if (first) {
          const __firstMatched = response.results[0].geometry.location;
          return { lat: __firstMatched.lat(), lng: __firstMatched.lng(), data };
        } else {
          return response.results
            .filter((o) => Array.isArray(o) && o.length)
            .map((o) => o.map((r) => ({ ...r, data })));
        }
      } catch (error) {
        return error;
      }
    },
    // controller
    controllerAddElement(__element) {
      this.map.controls[google.maps.ControlPosition.TOP_CENTER].push(__element);
    },
    // locate
    centerToPosition(__position) {
      if (!__position) {
        // center to current position if doesn't specify target
        if (!navigator.geolocation) {
          handleLocationError(false, this.infoWindow, this.map.getCenter()); // browser doesn't support Geolocation
          return;
        }

        navigator.geolocation.getCurrentPosition(
          (position) =>
            (this.center = {
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            }),
          () => handleLocationError(true, this.infoWindow, this.map.getCenter())
        );
        return;
      }

      // center after target position
      const pos = { lat: "", lng: "" };
      // coerce
      if (__position.coords)
        Object.assign(pos, {
          lat: __position.coords.latitude,
          lng: __position.coords.longitude,
        });
      if (__position.latitude && __position.longitude)
        Object.assign(pos, {
          lat: __position.latitude,
          lng: __position.longitude,
        });
      if (__position.lat && __position.lng) Object.assign(pos, __position);
      this.center = pos;
      this.zoom = 10;
    },
    // marker
    async addMarkers(__listLocations = []) {
      const __listLocationHasNoLatLng = [];

      for (let i = 0; i < __listLocations.length; i++) {
        const element = __listLocations[i];
        if (!element.latitude || !element.longitude) {
          // add those locations don't have lat lng to stack
          __listLocationHasNoLatLng.push(element);
          continue;
        }
        const pos = { lat: element.latitude, lng: element.longitude };
        const marker = new google.maps.Marker({
          ...element,
          position: pos,
          icon: MARKER_ICON_ACTIVE,
          map: this.map,
        });
        marker.addListener("click", () => this.handleClickMarker(marker));
        this.markers.push(marker);
      }

      // tracking by address
      console.log(
        "__listLocationHasNoLatLng.length",
        __listLocationHasNoLatLng.length
      );
      for (let i = 0; i < __listLocationHasNoLatLng.length; i++) {
        const element = __listLocationHasNoLatLng[i];

        // need to prevent fetching times reach limit by setTimeout
        setTimeout(async () => {
          this.findLocation(
            { address: element.address },
            { first: true, data: element }
          ).then((result, status) => {
            if (status !== google.maps.GeocoderStatus.OK || !result) {
              return;
            }

            console.log("result", result);
            const pos = { lat: result.lat, lng: result.lng };
            const marker = new google.maps.Marker({
              ...result,
              position: pos,
              icon: MARKER_ICON_ACTIVE,
              map: this.map,
            });
            marker.addListener("click", () => this.handleClickMarker(marker));
            this.markers.push(marker);
          });
        }, 250 * i);
      }

      const targetCenter = __listLocations.find(
        (o) => o.latitude && o.longitude
      ); // find first has lat lng
      this.zoom = ZOOM_DEGREE.XXL;
      if (!targetCenter)
        return this.centerToPosition(); // auto center when found none
      else return this.centerToPosition(targetCenter);
    },
    handleClickMarker(__location) {
      console.log("@handleClickMarker");
      this.infoWindow.setPosition();
      this.infoWindow.setContent(popupRender(__location));
      this.infoWindow.open({
        anchor: __location,
        map: this.map,
      });
    },
    initMap() {
      const options = {
        zoom: this.zoom,
        center: this.center,
        zoomControl: this.zoomControl,
        scaleControl: this.scaleControl,
        mapTypeControl: this.mapTypeControl,
        streetViewControl: this.streetViewControl,
      };
      const mapEl = this.$refs["distributionToolingMap"];
      console.log(mapEl);
      if (!mapEl) return;

      this.map = new google.maps.Map(mapEl, options);
      this.geocoder = new google.maps.Geocoder();
      this.infoWindow = new google.maps.InfoWindow();
      this.bounds = new google.maps.LatLngBounds();
    },
  },
  mounted() {
    this.initMap();
    this.fetchData();
  },
  watch: {
    center: {
      handler(newVal) {
        this.map.setCenter(newVal);
      },
      deep: true,
    },
    zoom(newVal) {
      this.map.setZoom(newVal);
    },
    search(newVal) {
      clearTimeout(this.debounceSearch);
      this.debounceSearch = setTimeout(() => {
        this.findLocation({ address: newVal });
      }, 1000);
    },
  },
};
</script>

<style scoped>
.distribution-tooling-map {
  height: 246px;
}

.map-data {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.15rem;
}

.map-data-total-tooling {
  margin-bottom: 0.5rem;
  margin-top: 0.5rem;
}

.map-data-total-number {
  display: inline-block;
  width: 100%;
}

.map-data-title {
  display: inline-block;
  width: 60%;
}

.map-data-title span {
  margin-right: 10px;
}
</style>
