let bankAccountId;

async function buildBankAccountOverview(id) {
    bankAccountId = id;

    const messages = await fetchLocalization("bankAccountOverview");

    handleFileBrowser(messages);
    await loadLineChart(messages);
    await loadKeyFigures(messages);
}

function handleFileBrowser(messages) {
    const fileBrowsButton = document.querySelector(".fileBrowseButton");
    const fileBrowsInput = document.querySelector(".fileBrowseInput");
    const fileUploadBox = document.querySelector(".fileUploadBox");
    const fileInstructions = document.querySelector(".fileInstructions");

    fileUploadBox.addEventListener("drop", async (event) => {
        event.preventDefault();
        await handleSelectedFiles(event.dataTransfer.files);
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
        await handleSelectedFiles(messages, event.target.files);
    });
}

async function loadLineChart(messages, startDate = null, endDate = null) {
    try {
        let url = `/bankAccountOverview/${bankAccountId}/data/lineChart`;
        const params = new URLSearchParams();

        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            showAlert("ERROR", messages["error_loadingLineChart"]);
            return;
        }

        const responseBody = await response.json();

        const bankName = document.getElementById("bankName");

        bankName.innerText = responseBody.seriesList[0].name;

        createLineChart(responseBody);
    } catch (error) {
        showAlert("ERROR", messages["error_generic"]);
        console.error("Error loading chart:", error);
    }
}

async function loadKeyFigures(messages, startDate = null, endDate = null) {
    try {
        let url = `/bankAccountOverview/${bankAccountId}/data/keyFigures`;
        const params = new URLSearchParams();

        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            showAlert("ERROR", messages["error_loadingKeyFigures"]);
            return;
        }

        const responseBody = await response.json();

        createKeyFigures(responseBody)
    } catch (error) {
        showAlert("ERROR", messages["error_generic"]);
        console.error("Error loading key figures", error);
    }
}

async function handleSelectedFiles(messages, files) {
    const validFiles = [];

    Array.from(files).forEach((file) => {
        const extension = file.split(".").pop().toLowerCase();

        // Filter unsupported file types
        if (!["csv", "txt", "pdf", "xls"].includes(extension)) {
            showAlert("WARNING", messages["warning_WrongFileFormat"]);
            return null;
        }

        validFiles.push(file);
    });

    if (validFiles.length > 0) {
        const newDate = await sendFiles(messages, validFiles);

        if (newDate) {
            await loadLineChart(messages);
            await loadKeyFigures(messages);
        }
    }
}

async function sendFiles(messages, files) {
    try {
        const formData = new FormData();

        files.forEach((file) => {
            formData.append("files", file);
        });

        const response = await fetch(`/bankAccountOverview/${bankAccountId}/upload/data`, {
            method: 'POST',
            body: formData,
        });

        const responseBody = await response.json();

        let newDate = false;

        for (let count = 0; count < responseBody.length; count++) {
            const responseOfFile = responseBody[count].body;
            showAlert(responseOfFile.alertType, responseOfFile.message);
            if (responseOfFile.alertType === "SUCCESS") {
                newDate = true;
            }
        }

        return newDate;
    } catch (error) {
        error("ERROR", messages["error_generic"]);
        console.error("Error loading chart:", error);
    }
}