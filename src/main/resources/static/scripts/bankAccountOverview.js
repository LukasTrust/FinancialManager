async function buildBankAccountOverview() {
    const messages = await fetchLocalization("bankAccountOverview");

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
            await updateVisuals(messages);
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