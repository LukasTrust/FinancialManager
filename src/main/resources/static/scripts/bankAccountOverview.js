async function buildBankAccountOverview() {
    const messages = await loadLocalization("bankAccountOverview");
    if (!messages)
        return;
    handleFileBrowser(messages);
    handleDateRangeSelection(messages, true);
    await updateVisuals(messages, true);
    setUpSearchStringFields(messages, false);
    await setUpAddButtons(messages);
    fillSearchStringFields(messages);
    document.getElementById("deleteDataButton").addEventListener("click", async () => await deleteData(messages));
}
async function deleteData(messages) {
    try {
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/deleteData`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS)
            await updateVisuals(messages, true);
    }
    catch (error) {
        console.error("There was an error deleting the data", error);
        showAlert('error', messages["error_generic"]);
    }
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
            await updateVisuals(messages, true);
        }
    }
}
async function sendFiles(messages, files) {
    try {
        const formData = new FormData();
        files.forEach((file) => formData.append("files", file));
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/upload`, {
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
async function removeSearchString(element, searchString, listId, messages) {
    var _a;
    try {
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/removeSearchString/${listId}/${searchString}`, {
            method: 'POST',
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS)
            (_a = element.parentElement) === null || _a === void 0 ? void 0 : _a.removeChild(element);
    }
    catch (error) {
        console.error("Error removeSearchString:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
async function addSearchStringBankAccount(searchString, listId, messages) {
    try {
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/addSearchString/${listId}/${searchString}`, { method: 'POST' });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS) {
            const listElement = document.getElementById(listId);
            if (listElement) {
                addStringToList(messages, listElement, searchString, (element) => removeSearchString(element, searchString, listId, messages));
            }
            else {
                console.warn(`List element not found for ID: ${listId}`);
            }
        }
    }
    catch (error) {
        console.error("Error adding search string:", error);
        showAlert("ERROR", messages["error_generic"] || "An unexpected error occurred.");
    }
}
function handleFileBrowser(messages) {
    const fileBrowsButton = document.querySelector(".fileBrowseButton");
    const fileBrowsInput = document.getElementById("fileBrowseInput");
    const fileUploadBox = document.querySelector(".fileUploadBox");
    const fileInstructions = document.getElementById("fileInstructions");
    fileUploadBox.addEventListener("drop", async (event) => {
        var _a, _b;
        event.preventDefault();
        await handleSelectedFiles(messages, (_b = (_a = event.dataTransfer) === null || _a === void 0 ? void 0 : _a.files) !== null && _b !== void 0 ? _b : new FileList());
    });
    fileUploadBox.addEventListener("dragover", (event) => {
        event.preventDefault();
        fileUploadBox.classList.add("active");
        fileInstructions.textContent = messages["fileUploadDrop"];
    });
    fileUploadBox.addEventListener("dragleave", (event) => {
        event.preventDefault();
        fileUploadBox.classList.remove("active");
        fileInstructions.textContent = messages["fileUploadDrag"];
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
async function setUpAddButtons(messages) {
    searchStringFields.forEach(({ addButtonId, listId, inputId }) => {
        const buttonElement = document.getElementById(addButtonId);
        const inputElement = document.getElementById(inputId);
        buttonElement.addEventListener("click", async (event) => {
            event.preventDefault(); // Prevents form reset
            const newSearchString = inputElement.value.trim();
            if (newSearchString) {
                await addSearchStringBankAccount(newSearchString, listId, messages);
                inputElement.value = "";
            }
            else {
                console.warn("Input is empty.");
            }
        });
    });
}
function fillSearchStringFields(messages) {
    searchStringFields.forEach(field => {
        const listId = field.listId;
        const stringList = document.getElementById(listId);
        if (!stringList) {
            console.warn(`Element with ID '${listId}' not found.`);
            return;
        }
        const bankAccount = bankAccounts[bankAccountId];
        // Corrected property existence check
        if (listId === "interestRateSearchStrings" && !(listId in bankAccount)) {
            stringList.style.visibility = "hidden";
            return;
        }
        if (bankAccount[listId] !== null)
            bankAccount[listId].forEach((searchString) => {
                addStringToList(messages, stringList, searchString, (element) => removeSearchString(element, searchString, listId, messages));
            });
    });
}
//# sourceMappingURL=bankAccountOverview.js.map