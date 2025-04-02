async function buildMergeContracts(cameFromUrl, contracts) {
    var _a, _b, _c;
    await loadURL("/mergeContracts");
    const messages = await loadLocalization("mergeContracts");
    if (!messages)
        return;
    selectedContract = null;
    createContractList(messages, contracts);
    (_a = document.getElementById("backButton")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    (_b = document.getElementById("mergeContractsHeader")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", async () => await mergeContracts(messages));
    (_c = document.getElementById("selectHeader")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", () => selectHeader());
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
    const contractsContainer = document.getElementById("contractsContainer");
    const headerContainer = document.getElementById("headerContract");
    if (headerContainer.children) {
        moveElements(headerContainer, contractsContainer);
    }
    moveElements(null, headerContainer, selectedContract);
}
//# sourceMappingURL=mergeContracts.js.map