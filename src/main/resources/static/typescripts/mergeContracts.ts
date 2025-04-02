async function buildMergeContracts(cameFromUrl: string, contracts: Contract[]): Promise<void> {
    await loadURL("/mergeContracts");

    const messages = await loadLocalization("mergeContracts");
    if (!messages) return;

    selectedContract = null;
    selectedCounterparty = null;
    headerContract = null;

    createContractList(messages, contracts, true);

    document.getElementById("backButton")?.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    document.getElementById("mergeContractsHeader")?.addEventListener("click", async () => await mergeContracts(messages));
    document.getElementById("selectHeader")?.addEventListener("click", () => selectHeader());
    document.getElementById("removeHeader")?.addEventListener("click", () => removeHeader());
}

async function mergeContracts(messages: Record<string, string>): Promise<void> {
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

        const responseBody: Response = await response.json();

        if (responseBody.alertType === AlertType.SUCCESS) {
            removeElements(contractsContainer)
        }
    } catch (error) {
        console.error("There was an error merging the contracts", error);
        showAlert('error', messages["error_generic"]);
    }
}

function selectHeader(): void {
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

function removeHeader(): void {
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