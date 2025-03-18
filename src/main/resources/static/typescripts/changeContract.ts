async function buildChangeContract(cameFromUrl: string, transactions: Transaction[]): Promise<void> {
    await loadURL("/changeContract");

    const messages= await loadLocalization("changeContract");
    if (!messages) return;

    selectedTransactionGroup = null;
    selectedCounterparty = null;
    selectedContract = null;

    createGroupedTransactions(messages, transactions);
    await fillContracts(messages);

    document.getElementById("backButton")?.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    document.getElementById("addGroupToContract")?.addEventListener("click", async () => await addGroupToContract(messages));
    document.getElementById("removeGroupFromContract")?.addEventListener("click", async () => await removeContractFromTransactions(messages));
}

async function loadOnlyContracts(messages: Record<string, string>): Promise<Contract[] | undefined> {
    try {
        const response = await fetch(`/contracts/${bankAccountId}/data/onlyContract`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }

        return await response.json();
    } catch (error) {
        console.error("There was an error loading the contracts:", error);
        showAlert('error', messages["error_generic"]);
    }
}

async function addGroupToContract(messages: Record<string, string>): Promise<void> {
    if (!selectedContract) {
        showAlert("ERROR", messages["error_noContractSelected"]);
        return;
    }

    const transactionIds: number[] = getIdsOfTransactionGroup(messages);

    if (transactionIds.length === 0) {
        return;
    }

    const contractId: string | null = selectedContract.id;
    const elementText = selectedContract.querySelector(".normalText");
    const contractName: string = elementText?.innerHTML || "";

    if (!contractId) {
        showAlert("ERROR", messages["error_noContractFound"]);
        return;
    }

    try {
        const response = await fetch(`/transactions/${bankAccountId}/data/addContractToTransactions/${contractId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(transactionIds)
        });

        const responseBody: Response = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === AlertType.SUCCESS) {
            transactionIds.forEach(transactionId => {
                const transactionElement = document.getElementById(transactionId.toString());
                if (transactionElement) {
                    let leftColumn = transactionElement.querySelector(".listContainerColumn") as HTMLElement;
                    const toolTip = leftColumn.querySelector(".tooltip") as HTMLElement;

                    if (selectedContract)
                        createContractSection(
                            messages,
                            leftColumn,
                            contractName,
                            selectedContract.dataset.startDate!,
                            selectedContract.dataset.lastPaymentDate!,
                            () => removeContractFromTransactionDialog(messages, { id: transactionId }, toolTip)
                        );

                    toggleTransactionSelection(selectedTransactionGroup);
                }
            });
        }
    } catch (error) {
        console.error("There was an error adding a contract to transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}

async function removeContractFromTransactions(messages: Record<string, string>): Promise<void> {
    const transactionIds: number[] = getIdsOfTransactionGroup(messages);

    if (transactionIds.length === 0) {
        return;
    }

    try {
        const response = await fetch(`/transactions/${bankAccountId}/data/removeContractFromTransactions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(transactionIds)
        });

        const responseBody: Response = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === AlertType.SUCCESS) {
            selectedTransactionGroup?.querySelectorAll(".tooltip").forEach(transaction => {
                transaction.remove();
            });

            toggleTransactionSelection(selectedTransactionGroup);
        }
    } catch (error) {
        console.error("There was an error removing a contract from transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}

async function removeContractFromTransaction(
    messages: Record<string, string>,
    transaction: { id: number },
    secondRow: HTMLElement
): Promise<void> {
    try {
        const transactionId = transaction.id;
        const url = `/transactions/${bankAccountId}/data/removeContractFromTransaction/${transactionId}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const dialog = document.querySelector("dialog");
        if (!dialog) return;

        if (!response.ok) {
            try {
                const responseBody = await response.json();
                showAlert(responseBody.alertType, responseBody.message, dialog);
            } catch {
                showAlert('error', messages["error_generic"], dialog);
            }
            return;
        }

        // Close the dialog if it exists
        dialog?.close();

        // Remove the secondRow from the DOM
        secondRow.remove();
    } catch (error) {
        console.error("Error removing contract:", error);
        showAlert('error', messages["error_generic"]);
    }
}

async function fillContracts(messages: Record<string, string>): Promise<void> {
    const contractData: Contract[] | undefined = await loadOnlyContracts(messages);

    createContractList(messages, contractData);
    updateContractAvailability();
}

function createGroupedTransactions(messages: Record<string, string>, transactions: Transaction[]): void {
    const transactionGroups = document.getElementById("transactionGroups");

    if (!transactionGroups) {
        console.error("Transaction groups container not found!");
        return;
    }

    if (transactions.length === 0) {
        const header = createAndAppendElement(transactionGroups, "div", "listContainerHeader", "",
            { style: "flex-direction: row; align-items: center" });
        createAndAppendElement(header, "i", "bi bi-info-circle-fill", "",
            { style: "font-size: 1.5rem; margin-right: 20px" });
        createAndAppendElement(header, "div", "normalText", messages["notTransactionSelected"]);
        return;
    }

    const currency = getCurrentCurrencySymbol();
    const groups = groupTransactions(transactions);

    groups.forEach((group, key) => {
        const listContainerHeader = createAndAppendElement(transactionGroups, "div", "listContainerHeader", "",
            { style: "margin-bottom: 20px" });
        createAndAppendElement(listContainerHeader, "h2", "", group.counterPartyName);

        const listContainer = createAndAppendElement(listContainerHeader, "div");
        listContainerHeader.dataset.counterPartyId = key.toString();

        listContainerHeader.addEventListener("click", () => toggleTransactionSelection(listContainerHeader));

        group.transactions.forEach(item => {
            const content = createAndAppendElement(listContainer, "div", "listItemSmall", "", {id: item.id.toString()});

            const left = createAndAppendElement(content, "div", "listContainerColumn");

            createAndAppendElement(left, "div", "normalText",
                `${messages["amount"]}: ${formatNumber(item.amount, currency)}      ${messages["date"]}: ${formatDateString(item.date)}`
            );

            let height = 70;

            if (item.contract) {
                createContractSection(
                    messages,
                    left,
                    item.contract.name,
                    item.contract.startDate,
                    item.contract.lastPaymentDate,
                    async (event) => {
                        event.stopPropagation();
                        await removeContractFromTransactionDialog(messages, item, left.querySelector(".tooltip"))
                    }
                );

                height = 120;
            }

            const removeButton = createAndAppendElement(content, "button", "removeButton bi bi-x-lg", "");
            removeButton.style.height = `${height}px`;

            removeButton.addEventListener("click", (event) => {
                event.stopPropagation();
                listContainer.removeChild(content);

                // If no transactions remain, remove the group
                if (!listContainer.children.length) {
                    if (listContainerHeader === selectedTransactionGroup) {
                        selectedTransactionGroup = null;
                        toggleTransactionSelection(selectedTransactionGroup);
                    }
                    transactionGroups.removeChild(listContainerHeader);
                }
            });
        });
    });
}

function groupTransactions(transactions: Transaction[]): Map<number, { transactions: Transaction[], counterPartyName: string }> {
    return transactions.reduce((map, transaction) => {
        const key = transaction.counterParty.id;

        if (!map.has(key)) {
            map.set(key, { transactions: [], counterPartyName: transaction.counterParty.name });
        }

        map.get(key)!.transactions.push(transaction);
        return map;
    }, new Map<number, { transactions: Transaction[], counterPartyName: string }>());
}

function toggleTransactionSelection(selectedElement: HTMLElement | null): void {
    if (!selectedElement) return;
    const isSameSelection = selectedTransactionGroup === selectedElement;

    // Deselect previous transaction and contract
    if (selectedTransactionGroup) selectedTransactionGroup.classList.remove("selected");
    if (selectedContract) {
        selectedContract.classList.remove("selected");
        selectedContract = null;
    }

    // Update selections
    selectedTransactionGroup = isSameSelection ? null : selectedElement;
    selectedCounterparty = isSameSelection ? null : selectedElement.dataset.counterPartyId ?? null;

    if (selectedTransactionGroup) {
        selectedTransactionGroup.classList.add("selected");
    }

    updateContractAvailability();
}

function updateContractAvailability(): void {
    const contractElements = document.querySelectorAll<HTMLElement>("#contractsContainer .listItem");

    contractElements.forEach(contract => {
        if (contract.dataset.counterPartyId === selectedCounterparty) {
            contract.classList.remove("disabled");
        } else {
            contract.classList.add("disabled");
        }
    });
}

function createContractSection(
    messages: Record<string, string>,
    leftColumn: HTMLElement,
    contractName: string,
    startDate: string,
    lastPaymentDate: string,
    removeButtonHandler: (event: Event) => void
): HTMLElement {
    // Remove old contract section if it exists
    let oldContract = leftColumn.querySelector(".tooltip");
    if (oldContract) {
        leftColumn.removeChild(oldContract);
    }

    // Add new contract details
    let newContractContainer = createAndAppendElement(leftColumn, "div", "flexContainer tooltip tooltipBottom");
    createAndAppendElement(newContractContainer, "div", "normalText", `${messages["contract"]}: ${contractName}`);
    createAndAppendElement(newContractContainer, "div", "tooltipText", `${messages["startDate"]}: ${startDate}   ${messages["lastPaymentDate"]}: ${lastPaymentDate}`);

    // Add remove contract button
    const removeContractButton = createAndAppendElement(newContractContainer, "button", "removeButton bi bi-x-lg", "", {
        style: "margin-left: 10px"
    });
    removeContractButton.addEventListener("click", removeButtonHandler);

    return newContractContainer;
}

async function removeContractFromTransactionDialog(
    messages: Record<string, string>,
    transaction: { id: number },
    secondRow: HTMLElement
): Promise<void> {
    showMessageBox(
        messages["removeContractFromTransaction"],
        "bi bi-file-earmark-fill",
        messages["removeContractFromTransactionMainText"],
        messages["yes"],
        "bi bi-check",
        messages["cancel"],
        "bi bi-x-lg",
        () => removeContractFromTransaction(messages, transaction, secondRow),
        closeDialog,
        messages["removeContractFromTransactionTooltip"]
    );
}

function getIdsOfTransactionGroup(messages: Record<string, string>): number[] {
    if (!selectedTransactionGroup) {
        showAlert("ERROR", messages["error_noTransactionGroupSelected"]);
        return [];
    }

    return Array.from(selectedTransactionGroup.querySelectorAll<HTMLElement>(".listItemSmall"))
        .map(transaction => Number(transaction.id))
        .filter(id => id !== 0);
}