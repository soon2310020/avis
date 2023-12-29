Common.getDatePickerButtonDisplay = function (startDate, endDate, timeScale) {
  console.log("getDatePickerButtonDisplay");

  let startUnix = moment(startDate, "YYYYMMDD").unix();
  let endUnix = moment(endDate, "YYYYMMDD").unix();

  let difference = endUnix - startUnix;
  let middleDate = moment.unix(startUnix + difference / 2);

  if (timeScale === "DATE") {
    return middleDate.format("YYYY-MM-DD");
  }
  if (timeScale === "WEEK") {
    return `Week ${middleDate.format("WW, YYYY")}`;
  }
  if (timeScale === "MONTH") {
    return middleDate.format("MMM, YYYY");
  }
  if (timeScale === "YEAR") {
    return middleDate.format("YYYY");
  }
  if (timeScale === "CUSTOM") {
    let start = moment(startDate, "YYYYMMDD").format("YYYY-MM-DD");
    let end = moment(endDate, "YYYYMMDD").format("YYYY-MM-DD");
    return `${start} - ${end}`;
  }
  if (timeScale === "QUARTER") {
    return `Q${middleDate.format("Q, YYYY")}`;
  }
};

Common.getDatePickerButtonDisplay;

Common.getDatePickerTimeValue = function (fromDate, toDate, timeScale) {
  console.log("getDatePickerTimeValue");
  console.log("fromDate: ", fromDate);
  console.log("toDate: ", toDate);
  console.log("timeScale: ", timeScale);

  let dateFormat = "";
  if (timeScale === "DATE") {
    dateFormat = "YYYYMMDD";
  }
  if (timeScale === "WEEK") {
    dateFormat = "YYYYWW";
  }
  if (timeScale === "MONTH") {
    dateFormat = "YYYYMM";
  }
  if (timeScale === "YEAR") {
    dateFormat = "YYYY";
  }
  if (timeScale === "QUARTER") {
    dateFormat = "YYYY[0]Q";
  }

  let fromDateTs = moment(fromDate, "YYYY-MM-DD").unix();
  let toDateTs = moment(toDate, "YYYY-MM-DD").unix();

  let difference = toDateTs - fromDateTs;
  let timeValueTs = fromDateTs + (difference ? difference / 2 : 0);
  let timeValue = moment.unix(timeValueTs).format(dateFormat);

  // CUSTOM
  if (timeScale === "CUSTOM") {
    let newFromDate = moment.unix(fromDateTs).format("YYYYMMDD");
    let newtoDate = moment.unix(toDateTs).format("YYYYMMDD");

    timeValue = `${newFromDate}-${newtoDate}`;
  }

  console.log("timeValue: ", timeValue);
  return timeValue;
};
