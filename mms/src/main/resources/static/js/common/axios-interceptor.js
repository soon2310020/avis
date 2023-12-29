const availableStatusCodes = [400, 500, 501];

axios.interceptors.response.use(function (response) {
    return response;
}, function (error) {
    const errorResponse = error.response.data;

    if (availableStatusCodes.includes(errorResponse.status)) {
        Common.alert(errorResponse.message, "Error");
    }

    return Promise.reject(error);
});
