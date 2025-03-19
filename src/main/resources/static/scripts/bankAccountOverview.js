async function buildBankAccountOverview() {
    const messages = await loadLocalization("bankAccountOverview");
    if (!messages)
        return;
    handleFileBrowser(messages);
    handleDateRangeSelection(messages);
    await updateVisuals(messages);
}
async function updateVisuals(messages, startDate = null, endDate = null) {
    await loadLineChart(messages, startDate, endDate);
    await loadKeyFigures(messages, startDate, endDate);
}
function handleDateRangeSelection(messages) {
    const startDate = document.getElementById("startDate");
    const endDate = document.getElementById("endDate");
    const clearDateButton = document.getElementById("clearDateButton");
    startDate.addEventListener("input", async () => await checkDates(messages, startDate, endDate));
    endDate.addEventListener("input", async () => await checkDates(messages, startDate, endDate));
    clearDateButton.addEventListener("click", async () => {
        startDate.value = '';
        endDate.value = '';
        await updateVisuals(messages);
    });
}
async function checkDates(messages, startDate, endDate) {
    const startValue = startDate.value.trim() || null;
    const endValue = endDate.value.trim() || null;
    if (startValue || endValue) {
        await updateVisuals(messages, startValue, endValue);
    }
}
function handleFileBrowser(messages) {
    const fileBrowsButton = document.querySelector(".fileBrowseButton");
    const fileBrowsInput = document.querySelector(".fileBrowseInput");
    const fileUploadBox = document.querySelector(".fileUploadBox");
    const fileInstructions = document.querySelector(".fileInstructions");
    fileUploadBox.addEventListener("drop", async (event) => {
        var _a, _b;
        event.preventDefault();
        await handleSelectedFiles(messages, (_b = (_a = event.dataTransfer) === null || _a === void 0 ? void 0 : _a.files) !== null && _b !== void 0 ? _b : new FileList());
    });
    fileUploadBox.addEventListener("dragover", (event) => {
        event.preventDefault();
        fileUploadBox.classList.add("active");
        fileInstructions.innerText = messages["fileUploadDrop"];
    });
    fileUploadBox.addEventListener("dragleave", (event) => {
        event.preventDefault();
        fileUploadBox.classList.remove("active");
        fileInstructions.innerText = messages["fileUploadDrag"];
    });
    fileBrowsButton.addEventListener("click", () => {
        fileBrowsInput.click();
    });
    fileBrowsInput.addEventListener("change", async (event) => {
        var _a;
        const target = event.target;
        await handleSelectedFiles(messages, (_a = target.files) !== null && _a !== void 0 ? _a : new FileList());
    });
}
async function handleSelectedFiles(messages, files) {
    const validFiles = [];
    const allowedExtensions = ["csv", "txt", "pdf", "xls"];
    Array.from(files).forEach((file) => {
        var _a;
        const extension = (_a = file.name.split(".").pop()) === null || _a === void 0 ? void 0 : _a.toLowerCase();
        if (!extension || !allowedExtensions.includes(extension)) {
            showAlert("WARNING", messages["warning_WrongFileFormat"]);
            return;
        }
        validFiles.push(file);
    });
    if (validFiles.length > 0) {
        showLoadingBar(messages);
        const newDate = await sendFiles(messages, validFiles);
        closeDialog();
        if (newDate) {
            await updateVisuals(messages);
        }
    }
}
async function sendFiles(messages, files) {
    try {
        const formData = new FormData();
        files.forEach((file) => formData.append("files", file));
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/upload/data`, {
            method: 'POST',
            body: formData,
        });
        const responseBody = await response.json();
        let newDate = false;
        responseBody.forEach((fileResponse) => {
            showAlert(fileResponse.body.alertType, fileResponse.body.message);
            if (fileResponse.body.alertType === AlertType.SUCCESS) {
                newDate = true;
            }
        });
        return newDate;
    }
    catch (error) {
        console.error("Error uploading files:", error);
        showAlert("ERROR", messages["error_generic"]);
        return false;
    }
}
//# sourceMappingURL=bankAccountOverview.js.map