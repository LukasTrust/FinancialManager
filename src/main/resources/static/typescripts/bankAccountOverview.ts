async function buildBankAccountOverview(): Promise<void> {
    const messages = await fetchLocalization("bankAccountOverview");
    if (!messages) return;

    handleFileBrowser(messages);
    handleDateRangeSelection(messages);
    await updateVisuals(messages);
}

async function updateVisuals(messages: Record<string, string>, startDate: string | null = null, endDate: string | null = null): Promise<void> {
    await loadLineChart(messages, startDate, endDate);
    await loadKeyFigures(messages, startDate, endDate);
}

function handleDateRangeSelection(messages: Record<string, string>): void {
    const startDate = document.getElementById("startDate") as HTMLInputElement;
    const endDate = document.getElementById("endDate") as HTMLInputElement;
    const clearDateButton = document.getElementById("clearDateButton") as HTMLButtonElement;

    startDate.addEventListener("input", async () => await checkDates(messages, startDate, endDate));
    endDate.addEventListener("input", async () => await checkDates(messages, startDate, endDate));
    clearDateButton.addEventListener("click", async () => {
        startDate.value = '';
        endDate.value = '';
        await updateVisuals(messages);
    });
}

async function checkDates(messages: Record<string, string>, startDate: HTMLInputElement, endDate: HTMLInputElement): Promise<void> {
    const startValue = startDate.value.trim() || null;
    const endValue = endDate.value.trim() || null;

    if (startValue || endValue) {
        await updateVisuals(messages, startValue, endValue);
    }
}

function handleFileBrowser(messages: Record<string, string>): void {
    const fileBrowsButton = document.querySelector(".fileBrowseButton") as HTMLButtonElement;
    const fileBrowsInput = document.querySelector(".fileBrowseInput") as HTMLInputElement;
    const fileUploadBox = document.querySelector(".fileUploadBox") as HTMLDivElement;
    const fileInstructions = document.querySelector(".fileInstructions") as HTMLDivElement;

    fileUploadBox.addEventListener("drop", async (event: DragEvent) => {
        event.preventDefault();
        await handleSelectedFiles(messages, event.dataTransfer?.files ?? new FileList());
    });

    fileUploadBox.addEventListener("dragover", (event: DragEvent) => {
        event.preventDefault();
        fileUploadBox.classList.add("active");
        fileInstructions.innerText = messages["fileUploadDrop"];
    });

    fileUploadBox.addEventListener("dragleave", (event: DragEvent) => {
        event.preventDefault();
        fileUploadBox.classList.remove("active");
        fileInstructions.innerText = messages["fileUploadDrag"];
    });

    fileBrowsButton.addEventListener("click", () => {
        fileBrowsInput.click();
    });

    fileBrowsInput.addEventListener("change", async (event: Event) => {
        const target = event.target as HTMLInputElement;
        await handleSelectedFiles(messages, target.files ?? new FileList());
    });
}

async function handleSelectedFiles(messages: Record<string, string>, files: FileList): Promise<void> {
    const validFiles: File[] = [];
    const allowedExtensions = ["csv", "txt", "pdf", "xls"];

    Array.from(files).forEach((file) => {
        const extension = file.name.split(".").pop()?.toLowerCase();

        if (!extension || !allowedExtensions.includes(extension)) {
            showAlert("WARNING", messages["warning_WrongFileFormat"]);
            return;
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

async function sendFiles(messages: Record<string, string>, files: File[]): Promise<boolean> {
    try {
        const formData = new FormData();
        files.forEach((file) => formData.append("files", file));

        const response = await fetch(`/bankAccountOverview/${bankAccountId}/upload/data`, {
            method: 'POST',
            body: formData,
        });

        const responseBody = await response.json();
        let newDate = false;

        responseBody.forEach((fileResponse: { body: { alertType: string; message: string } }) => {
            showAlert(fileResponse.body.alertType, fileResponse.body.message);
            if (fileResponse.body.alertType === "SUCCESS") {
                newDate = true;
            }
        });

        return newDate;
    } catch (error) {
        console.error("Error uploading files:", error);
        showAlert("ERROR", messages["error_generic"]);
        return false;
    }
}
