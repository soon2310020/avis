Common.getCodes = async () => {
  if (localStorage.getItem("codes")) {
    return JSON.parse(localStorage.getItem("codes"));
  }

  try {
    const response = await axios.get("/api/codes");
    localStorage.setItem("codes", JSON.stringify(response.data));
    return response.data;
  } catch (error) {
    console.log(error);
  }
};
