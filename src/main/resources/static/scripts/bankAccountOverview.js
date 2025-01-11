let bankAccountId;

function buildBankAccountOverview(id) {
    bankAccountId = id;

    const fileBrowsButton = document.querySelector(".fileBrowseButton");
    const fileBrowsInput = document.querySelector(".fileBrowseInput");
    const fileUploadBox = document.querySelector(".fileUploadBox");
    const fileInstructions = document.querySelector(".fileInstructions");

    fileUploadBox.addEventListener("drop", (event) => {
        event.preventDefault();
        handleSelectedFiles(event.dataTransfer.files);
    });

    fileUploadBox.addEventListener("dragover", (event) => {
        event.preventDefault();
        fileUploadBox.classList.add("active");
        fileInstructions.innerText = "Release to upload or";
    });

    fileUploadBox.addEventListener("dragleave", (event) => {
        event.preventDefault();
        fileUploadBox.classList.remove("active");
        fileInstructions.innerText = "Drag files here or";
    });

    fileBrowsButton.addEventListener("click", () => {
        fileBrowsInput.click();
    });

    fileBrowsInput.addEventListener("change", (event) => {
        handleSelectedFiles(event.target.files);
    });
}

function handleSelectedFiles(files) {
    const validFiles = [];

    Array.from(files).forEach((file) => {
        const { name, size } = file;
        const extension = name.split(".").pop().toLowerCase();

        // Filter unsupported file types
        if (!['csv', 'txt', 'pdf', 'xls'].includes(extension)) {
            showAlert('warning', 'Only csv, txt, pdf and xls files are allowed');
            return null;
        }

        validFiles.push(file); // Collect valid files
    });

    if (validFiles.length > 0) {
        sendFiles(validFiles); // Send valid files with POST request
    }
}

function sendFiles(files) {
    const formData = new FormData();

    files.forEach((file) => {
        formData.append("files", file);
    });

    console.log(bankAccountId);

    fetch(`/bankAccountOverview/${bankAccountId}/upload/data`, {
        method: 'POST',
        body: formData,
    })
        .then(response => response.json())
        .then(data => {
            showAlert('success', 'Files uploaded successfully');
            console.log('Server response:', data);
        })
        .catch(error => {
            showAlert('error', 'Error uploading files');
            console.error('Error:', error);
        });
}