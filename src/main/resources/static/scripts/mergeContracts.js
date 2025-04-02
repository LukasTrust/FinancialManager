async function buildMergeContracts(cameFromUrl, contracts) {
    var _a, _b, _c, _d;
    await loadURL("/mergeContracts");
    const messages = await loadLocalization("mergeContracts");
    if (!messages)
        return;
    selectedContract = null;
    selectedCounterparty = null;
    headerContract = null;
    createContractList(messages, contracts, true);
    (_a = document.getElementById("backButton")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    (_b = document.getElementById("mergeContractsHeader")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", async () => await mergeContracts(messages));
    (_c = document.getElementById("selectHeader")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", () => selectHeader());
    (_d = document.getElementById("removeHeader")) === null || _d === void 0 ? void 0 : _d.addEventListener("click", () => removeHeader());
}
async function mergeContracts(messages) {
    try {
        const contractsContainer = document.getElementById("contractsContainer");
        const ids = getIdsFromContainer(contractsContainer);
        if (ids.length === 0) {
            showAlert(AlertType.INFO, messages["selectAHeader"]);
            return;
        }
        const headerContainer = document.getElementById("headerContract");
        const headerIds = getIdsFromContainer(headerContainer);
        if (ids.length !== 1) {
            showAlert(AlertType.INFO, messages["selectContracts"]);
            return;
        }
        const headerId = headerIds[0];
        const response = await fetch(`/contracts/${bankAccountId}/data/merge/${headerId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
        const responseBody = await response.json();
        if (responseBody.alertType === AlertType.SUCCESS) {
            removeElements(contractsContainer);
        }
    }
    catch (error) {
        console.error("There was an error merging the contracts", error);
        showAlert('error', messages["error_generic"]);
    }
}
function selectHeader() {
    if (selectedContract !== null && headerContract !== selectedContract) {
        const headerContainer = document.getElementById("headerContract");
        removeHeader();
        headerContract = selectedContract;
        selectedContract = null;
        moveElements(null, headerContainer, headerContract);
        toggleSelection(headerContract, headerContract, "selected");
        updateContractAvailability();
    }
}
function removeHeader() {
    if (headerContract) {
        const contractsContainer = document.getElementById("contractsContainer");
        moveElements(null, contractsContainer, headerContract);
        if (headerContract.classList.contains("selected")) {
            selectedContract = null;
            toggleSelection(headerContract, headerContract, "selected");
        }
        headerContract = null;
        selectedCounterparty = null;
        updateContractAvailability();
    }
}
//# sourceMappingURL=mergeContracts.js.map