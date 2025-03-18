async function buildMergeContracts(ameFromUrl: string, contracts: Contract[]): Promise<void> {
    await loadURL("/mergeContracts");

    const messages = await loadLocalization("mergeContracts");
    if (!messages) return;

    selectedContract = null;

    createContractList(messages, contracts);

    document.getElementById("backButton")?.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    document.getElementById("mergeContractsHeader")?.addEventListener("click", async () => await mergeContracts(messages));
    document.getElementById("selectHeader")?.addEventListener("click", async () => a);
}

async function mergeContracts(messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/contracts/${bankAccountId}/data/onlyContract`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }

    } catch (error) {
        console.error("There was an error merging the contracts", error);
        showAlert('error', messages["error_generic"]);
    }
}