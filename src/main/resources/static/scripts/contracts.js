async function buildChangeContract(cameFromUrl, transactions) {
    await loadURL("/changeContract", "/transactions")

    const messages = await fetchLocalization("changeContract");

    selectedTransactionGroup = null;
    selectedCounterparty = null;
    selectedContract = null;

    createGroupedTransactions(messages, transactions);
    await fillContracts(messages);

    document.getElementById("backButton").addEventListener("click", async () => await backToOtherView(cameFromUrl));
}

async function fillContracts(messages) {
    await loadContracts(messages);
    const contractsContainer = document.getElementById("contractsContainer");

    if (!contractsContainer) {
        console.error("Contracts container not found!");
        return;
    }

    contractData.forEach(contract => {
        const listItem = createAndAppendElement(contractsContainer, "div", "listItem tooltip tooltipBottom");
        listItem.addEventListener("click", () => toggleContractSelection(listItem));
        listItem.dataset.counterPartyId = contract.counterParty.id;
        createAndAppendElement(listItem, "div", "normalText", contract.name);
        createAndAppendElement(listItem, "div", "tooltipText", `${messages["startDate"]}: ${contract.startDate}   ${messages["lastPaymentDate"]}: ${contract.lastPaymentDate}`)
    });

    updateContractAvailability();
}

function createGroupedTransactions(messages, transactions) {
    const transactionGroups = document.getElementById("transactionGroups");

    if (!transactionGroups) {
        console.error("Transaction groups container not found!");
        return;
    }

    const currency = getCurrentCurrencySymbol();
    const groups = groupTransactions(transactions);

    groups.forEach((group, key) => {
        const listContainerHeader = createAndAppendElement(transactionGroups, "div", "listContainerHeader", "",
            { style: "margin-bottom: 20px" });
        createAndAppendElement(listContainerHeader, "h2", "", group.counterPartyName);

        const listContainer = createAndAppendElement(listContainerHeader, "div");
        listContainerHeader.dataset.counterPartyId = key;

        listContainerHeader.addEventListener("click", () => toggleTransactionSelection(listContainerHeader));

        group.transactions.forEach(item => {
            const content = createAndAppendElement(listContainer, "div", "listItemSmall", "", { id: item.id });

            const left = createAndAppendElement(content, "div", "listContainerColumn");

            createAndAppendElement(left, "div", "normalText",
                `${messages["amount"]}: ${formatNumber(item.amount, currency)}      ${messages["date"]}: ${formatDateString(item.date)}`
            );

            let height = 70;

            if (item.contract) {
                const secondRow = createAndAppendElement(left, "div", "flexContainer tooltip tooltipBottom");
                createAndAppendElement(secondRow, "div", "normalText", `${messages["contract"]}: ${item.contract.name}`);
                createAndAppendElement(secondRow, "div", "tooltipText", `${messages["startDate"]}: ${item.contract.startDate}   ${messages["lastPaymentDate"]}: ${item.contract.lastPaymentDate}`)

                const removeContractButton = createAndAppendElement(secondRow, "button", "removeButton bi bi-x-lg", "",
                    { style: "margin-left: 10px" });
                removeContractButton.addEventListener("click", () => removeContractFromTransactionDialog(messages, item, secondRow));

                height = 120;
            }

            const removeButton = createAndAppendElement(content, "button", "removeButton bi bi-x-lg", "");
            removeButton.style.height = `${height}px`;
            removeButton.addEventListener("click", () => {
                listContainer.removeChild(content);

                // Check if the group is empty after removing a transaction
                if (listContainer.children.length === 0) {
                    transactionGroups.removeChild(listContainerHeader);
                }
            });

        });
    });
}

function updateContractAvailability() {
    const contractElements = document.querySelectorAll("#contractsContainer .listItem");

    contractElements.forEach(contract => {
        if (contract.dataset.counterPartyId === selectedCounterparty) {
            contract.classList.remove("disabled");
        } else {
            contract.classList.add("disabled");
        }
    });
}

function toggleContractSelection(selectedElement) {
    if (selectedContract === selectedElement) {
        selectedContract.classList.remove("selected");
        selectedContract = null;
    } else {
        if (selectedContract) selectedContract.classList.remove("selected");
        selectedContract = selectedElement;
        selectedContract.classList.add("selected");
    }
}

function toggleTransactionSelection(selectedElement) {
    const isSameSelection = selectedTransactionGroup === selectedElement;

    if (selectedTransactionGroup) {
        selectedTransactionGroup.classList.remove("selected");

        if (selectedContract) {
            selectedContract.classList.remove("selected");
            selectedContract = null;
        }
    }

    // Select new transaction if it's not the same as the previous one
    selectedTransactionGroup = isSameSelection ? null : selectedElement;
    selectedCounterparty = isSameSelection ? null : selectedElement.dataset.counterPartyId;

    if (selectedTransactionGroup) {
        selectedTransactionGroup.classList.add("selected");
    }

    updateContractAvailability();
}

async function removeContractFromTransactionDialog(messages, transaction, secondRow) {
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

async function removeContractFromTransaction(messages, transaction, secondRow) {
    try {
        const transactionId = transaction.id;
        const url = `/contracts/${bankAccountId}/data/updateTransaction/${transactionId}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const dialog = document.querySelector("dialog");

        if (!response.ok) {
            try {
                const responseBody = await response.json();
                showAlert(responseBody.alertType, responseBody.message, dialog);
            } catch {
                showAlert('error', messages["error_generic"], dialog);
            }
            return;
        }

        // Close the dialog
        dialog.close();

        // Remove the secondRow from the DOM
        secondRow.remove();

    } catch (error) {
        console.error("Error removing contract:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function groupTransactions(transactions) {
    return transactions.reduce((map, transaction) => {
        const key = transaction.counterParty.id;

        if (!map.has(key)) {
            map.set(key, { transactions: [], counterPartyName: transaction.counterParty.name });
        }

        map.get(key).transactions.push(transaction);
        return map;
    }, new Map());
}


async function loadContracts(messages) {
    try {
        const response = await fetch(`/contracts/${bankAccountId}/data`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const responseBody = await response.json();
            showAlert(responseBody.alertType, responseBody.message);
            showAlert("ERROR", messages["error_loadingContracts"]);
            return;
        }

        contractData = await response.json();
    } catch (error) {
        console.error("There was an error loading the contracts:", error);
        showAlert('error', messages["error_generic"]);
    }
}
