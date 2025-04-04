async function buildBankAccountOverview(): Promise<void> {
    const messages = await loadLocalization("bankAccountOverview");
    if (!messages) return;

    handleFileBrowser(messages);
    handleDateRangeSelection(messages, true);
    await updateVisuals(messages, true);

    setUpSearchStringFields(messages, false);
    await setUpAddButtons(messages);
    fillSearchStringFields(messages);

    document.getElementById("deleteDataButton").addEventListener("click", async () => await deleteData(messages));
}

async function deleteData(messages: Record<string, string>) {
    try {
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/deleteData`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'}
        });

        const responseBody: Response = await response.json();

        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === AlertType.SUCCESS)
            await updateVisuals(messages, true);

    } catch (error) {
        console.error("There was an error deleting the data", error);
        showAlert('error', messages["error_generic"]);
    }
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
        showLoadingBar(messages);

        const newDate = await sendFiles(messages, validFiles);

        closeDialog();

        if (newDate) {
            await updateVisuals(messages, true);
        }
    }
}

async function sendFiles(messages: Record<string, string>, files: File[]): Promise<boolean> {
    try {
        const formData = new FormData();
        files.forEach((file) => formData.append("files", file));

        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/upload`, {
            method: 'POST',
            body: formData,
        });

        const responseBody = await response.json();
        let newDate = false;

        responseBody.forEach((fileResponse: { body: Response }) => {
            showAlert(fileResponse.body.alertType, fileResponse.body.message);
            if (fileResponse.body.alertType === AlertType.SUCCESS) {
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

async function removeSearchString(element: HTMLElement, searchString: string, listId: string, messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/removeSearchString/${listId}/${searchString}`, {
            method: 'POST',
        });

        const responseBody = await response.json();

        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === AlertType.SUCCESS)
            element.parentElement?.removeChild(element);
    } catch (error) {
        console.error("Error removeSearchString:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

async function addSearchStringBankAccount(searchString: string, listId: string, messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/bankAccountOverview/${bankAccountId}/data/addSearchString/${listId}/${searchString}`,
            { method: 'POST' });

        const responseBody = await response.json();

        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === AlertType.SUCCESS) {
            const listElement = document.getElementById(listId);
            if (listElement) {
                addStringToList(messages, listElement, searchString, (element) => removeSearchString(element, searchString, listId, messages));
            } else {
                console.warn(`List element not found for ID: ${listId}`);
            }
        }
    } catch (error) {
        console.error("Error adding search string:", error);
        showAlert("ERROR", messages["error_generic"] || "An unexpected error occurred.");
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

async function setUpAddButtons(messages: Record<string, string>): Promise<void> {
    searchStringFields.forEach(({ addButtonId, listId, inputId }) => {
        const buttonElement = document.getElementById(addButtonId) as HTMLButtonElement | null;
        const inputElement = document.getElementById(inputId) as HTMLInputElement | null;

        buttonElement.addEventListener("click", async (event) => {
            event.preventDefault(); // Prevents form reset
            const newSearchString = inputElement.value.trim();

            if (newSearchString) {
                await addSearchStringBankAccount(newSearchString, listId, messages);
                inputElement.value = "";
            } else {
                console.warn("Input is empty.");
            }
        });
    });
}

function fillSearchStringFields(messages: Record<string, string>): void {
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

        bankAccount[listId].forEach((searchString: string) => {
            addStringToList(messages, stringList, searchString, (element) => removeSearchString(element, searchString, listId, messages));
        });
    });
}