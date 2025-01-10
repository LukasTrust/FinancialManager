let selectedFiles = new Set(); // Initialize as a Set to ensure unique files
let fileList;

function buildBankAccountOverview() {
    const fileBrowsButton = document.querySelector(".fileBrowseButton");
    const fileBrowsInput = document.querySelector(".fileBrowseInput");
    const fileUploadBox = document.querySelector(".fileUploadBox");
    const fileInstructions = document.querySelector(".fileInstructions");
    fileList = document.querySelector(".fileList");

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
    Array.from(files).forEach((file) => {
        // Skip files already added
        if (selectedFiles.has(file.name)) {
            showAlert('warning', 'The file ${file.name} is already selected.');
            return;
        }

        // Add file to the Set
        selectedFiles.add(file.name);

        const fileItem = createFileItem(file);
        if (fileItem) {
            fileList.prepend(fileItem);
        }
    });
}

function createFileItem(file) {
    const { name, size } = file;
    const extension = name.split(".").pop().toLowerCase();

    // Filter unsupported file types
    if (!['csv', 'txt', 'pdf', 'xls'].includes(extension)) {
        showAlert('warning', 'Only csv, txt, pdf and xls files are allowed');
        return null;
    }

    // Create the HTML structure
    const listItem = document.createElement("li");
    listItem.className = "fileListItem";

    const fileExtension = document.createElement("div");
    fileExtension.className = "fileExtension";
    fileExtension.textContent = extension;

    const fileContentWrapper = document.createElement("div");
    fileContentWrapper.className = "fileContentWrapper";

    const fileContent = document.createElement("div");
    fileContent.className = "fileContent";

    const fileDetails = document.createElement("div");
    fileDetails.className = "fileDetails";

    const fileName = document.createElement("h5");
    fileName.className = "fileName";
    fileName.textContent = name;

    const fileInfo = document.createElement("div");
    fileInfo.className = "fileInfo";

    const fileSize = document.createElement("small");
    fileSize.className = "fileSize";
    fileSize.textContent = `${(size / 1024).toFixed(2)} KB`;

    const removeButton = document.createElement("button");
    removeButton.className = "removeButton";
    removeButton.innerHTML = `<span class="bi bi-x-square"></span>`;
    removeButton.addEventListener("click", () => {
        listItem.remove();
        selectedFiles.delete(name);
    });

    // Append elements
    fileInfo.appendChild(fileSize);
    fileDetails.appendChild(fileName);
    fileDetails.appendChild(fileInfo);
    fileContent.appendChild(fileDetails);
    fileContent.appendChild(removeButton);
    fileContentWrapper.appendChild(fileContent);

    listItem.appendChild(fileExtension);
    listItem.appendChild(fileContentWrapper);

    return listItem;
}