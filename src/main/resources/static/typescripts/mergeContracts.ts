async function buildMergeContracts(cameFromUrl: string, contracts: Contract[]): Promise<void> {
    await loadURL("/mergeContracts");

    const messages = await loadLocalization("mergeContracts");
    if (!messages) return;

    selectedContract = null;

    createContractList(messages, contracts);

    document.getElementById("backButton")?.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    document.getElementById("mergeContractsHeader")?.addEventListener("click", async () => await mergeContracts(messages));
    document.getElementById("selectHeader")?.addEventListener("click", () => selectHeader());
}

async function mergeContracts(messages: Record<string, string>): Promise<void> {
    try {
        const contractsContainer = document.getElementById("contractsContainer");
        const ids = getIdsFromContainer(contractsContainer);

        const response = await fetch(`/contracts/${bankAccountId}/data/mergeContracts`, {
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
    const contractsContainer = document.getElementById("contractsContainer");
    const headerContainer = document.getElementById("headerContract");

    if (headerContainer.children) {
        moveElements(headerContainer, contractsContainer);
    }

    moveElements(null, headerContainer, selectedContract);
}