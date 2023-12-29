/**
 * getPermissoinList와 isMenuPermitted의 param은 동일함
 *
 * 1st param: menu permission resourceId
 * 2nd param: sub menu permission resourceId
 * 3rd param: item id (menu || sub menu)
 */

// permission list를 조회
Common.getPermissionList = async function (menuId, subMenuId, itemId) {
  const menuPermissions = JSON.parse(await Common.getSystem("menuPermissions"));

  let result;

  if (menuId && subMenuId && itemId) {
    result = menuPermissions[menuId].children[subMenuId].items[itemId];
  } else if (menuId && subMenuId) {
    result = menuPermissions[menuId].children[subMenuId];
  } else if (menuId && itemId) {
    result = menuPermissions[menuId].items[itemId];
  } else if (menuId) {
    result = menuPermissions[menuId];
  }

  console.log("getPermissionList: ", result);

  return result;
};

// permission 여부를 조회
Common.isMenuPermitted = async function (menuId, subMenuId, itemId) {
  const menuPermissions = JSON.parse(await Common.getSystem("menuPermissions"));

  let result;

  if (menuId && subMenuId && itemId) {
    result =
      menuPermissions[menuId].children[subMenuId].items[itemId].permitted;
  } else if (menuId && subMenuId) {
    result = menuPermissions[menuId].children[subMenuId];
  } else if (menuId && itemId) {
    result = menuPermissions[menuId].items[itemId].permitted;
  } else if (menuId) {
    result = menuPermissions[menuId];
  }

  console.log("isMenuPermitted: ", result);

  if (result) {
    return true;
  } else {
    return false;
  }
};

Common.PERMISSION_CODE = {
  // ALERT_CENTER: "CM9030",
  ALERT_GENERAL: "CM9031",
  ALERT_ASSETS: "AS9030",
  ALERT_PRODUCTION: "PR9030",
  ALERT_CENTER_RELOCATION: "ALERT-RELOCATION",
  ALERT_CENTER_DISCONNECTED: "ALERT-DISCONNECTED",
  ALERT_CENTER_CYCLE_TIME: "ALERT-CYCLE_TIME",
  ALERT_CENTER_MAINTENANCE: "ALERT-MAINTENANCE",
  ALERT_CENTER_EFFICIENCY: "ALERT-EFFICIENCY",
  ALERT_CENTER_MISCONFIGURE: "ALERT-MISCONFIGURE",
  ALERT_CENTER_DATA_SUBMISSION: "ALERT-DATA_SUBMISSION",
  ALERT_CENTER_REFURBISHMENT: "ALERT-REFURBISHMENT",
  ALERT_CENTER_DETACHMENT: "ALERT-DETACHMENT",
  ALERT_CENTER_DOWNTIME: "ALERT-DOWNTIME",
  ALERT_CENTER_DOWNTIME_MACHINE: "ALERT-DOWNTIME_MACHINE",
  OEE_CENTER: "PR9010",
  OEE_CENTER_NUMBER_OF_MACHINES: "PR9010-01",
  OEE_CENTER_MACHINE_DOWNTIME: "PR9010-02",
  OEE_CENTER_MACHINE_DOWNTIME_DETAIL: "PR9010-03",
  OEE_CENTER_PARTS_PRODUCED: "PR9010-04",
  OEE_CENTER_PARTS_PRODUCED_DETAIL: "PR9010-05",
  OEE_CENTER_REJECT_RATE: "PR9010-06",
  OEE_CENTER_REJECT_RATE_DETAIL: "PR9010-07",
  OEE_CENTER_OEE: "PR9010-08",
  OEE_CENTER_MACHINE_DOWNTIME_EVENTS: "PR9010-09",
  DASHBOARD: "CM9020",
  DATA_IMPORT: "CM1070",
  DATA_IMPORT_IMPORT_COMPANY: "IMPORT-COMPANY",
  DATA_IMPORT_IMPORT_MACHINE: "IMPORT-MACHINE",
  DATA_IMPORT_IMPORT_PART: "IMPORT-PART",
  DATA_IMPORT_IMPORT_PLANT: "IMPORT-PLANT",
  DATA_IMPORT_IMPORT_TOOLING: "IMPORT-TOOLING",
  DATA_IMPORT_IMPORT_USER: "IMPORT-USER",
  DATA_FAMILY: "CM1071",
  DATA_FAMILY_TOOLING: "AS1010",
  DATA_FAMILY_MACHINE: "AS1020",
  DATA_FAMILY_COMPANY: "CM1010",
  DATA_FAMILY_PLANT: "CM1020",
  DATA_FAMILY_PART: "PR1010",
  DATA_FAMILY_CATEGORY: "SC1010",
  DEVICES: "CM1072",
  DEVICES_TERMINAL: "AS1030",
  DEVICES_SENSOR: "AS1040",
};
