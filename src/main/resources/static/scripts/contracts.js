async function buildChangeContract(cameFromUrl, transactions) {
    await loadURL("/changeContract", "/transactions")

    const messages = await fetchLocalization("changeContract");

    selectedTransactionGroup = null;
    selectedCounterparty = null;
    selectedContract = null;

    createGroupedTransactions(messages, transactions);
    await fillContracts(messages);

    document.getElementById("backButton").addEventListener("click", async () => await backToOtherView(cameFromUrl));
    document.getElementById("addGroupToContract").addEventListener("click", async () => await addGroupToContract(messages));
    document.getElementById("removeGroupFromContract").addEventListener("click", async () => await removeContractFromTransactions((messages)))
}

async function fillContracts(messages) {
    const contractData = await loadContracts(messages);
    const contractsContainer = document.getElementById("contractsContainer");

    if (!contractsContainer) {
        console.error("Contracts container not found!");
        return;
    }

    const currency = getCurrentCurrencySymbol();

    contractData.forEach(contract => {
        const listItem = createAndAppendElement(contractsContainer, "div", "listItem tooltip tooltipBottom");
        listItem.addEventListener("click", () => toggleContractSelection(listItem));
        listItem.id = contract.id;
        listItem.dataset.counterPartyId = contract.counterParty.id;

        const startDate = formatDateString(contract.startDate);
        const lastPaymentDate = formatDateString(contract.lastPaymentDate);
        const amount = formatNumber(contract.amount, currency)

        listItem.dataset.startDate = startDate;
        listItem.dataset.lastPaymentDate = lastPaymentDate;
        createAndAppendElement(listItem, "div", "normalText", `${contract.name}, ${messages["amount"]}: ${amount}`);
        createAndAppendElement(listItem, "div", "tooltipText", `${messages["startDate"]}: ${startDate}   ${messages["lastPaymentDate"]}: ${lastPaymentDate}`)
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
            {style: "margin-bottom: 20px"});
        createAndAppendElement(listContainerHeader, "h2", "", group.counterPartyName);

        const listContainer = createAndAppendElement(listContainerHeader, "div");
        listContainerHeader.dataset.counterPartyId = key;

        listContainerHeader.addEventListener("click", () => toggleTransactionSelection(listContainerHeader));

        group.transactions.forEach(item => {
            const content = createAndAppendElement(listContainer, "div", "listItemSmall", "", {id: item.id});

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
    selectedContract = toggleSelection(selectedElement, selectedContract, "selected");
}

function toggleTransactionSelection(selectedElement) {
    const isSameSelection = selectedTransactionGroup === selectedElement;

    // Deselect previous transaction and contract
    if (selectedTransactionGroup) selectedTransactionGroup.classList.remove("selected");
    if (selectedContract) {
        selectedContract.classList.remove("selected");
        selectedContract = null;
    }

    // Update selections
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
        const url = `/contracts/${bankAccountId}/data/removeContractFromTransaction/${transactionId}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'}
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
            map.set(key, {transactions: [], counterPartyName: transaction.counterParty.name});
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

        return await response.json();
    } catch (error) {
        console.error("There was an error loading the contracts:", error);
        showAlert('error', messages["error_generic"]);
    }
}

async function addGroupToContract(messages) {
    if (!selectedContract) {
        showAlert("ERROR", messages["error_noContractSelected"]);
        return;
    }

    const transactionIds = getIdsOfTransactionGroup();

    if (transactionIds.length === 0) {
        return;
    }

    const contractId = selectedContract.id;
    const contractName = selectedContract.querySelector(".normalText").innerText; // Extract contract name

    if (!contractId) {
        showAlert("ERROR", messages["error_noContractFound"]);
        return;
    }

    try {
        const response = await fetch(`/contracts/${bankAccountId}/data/addContractToTransactions/${contractId}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(transactionIds)
        });

        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === "SUCCESS") {
            transactionIds.forEach(transactionId => {

                const transactionElement = document.getElementById(transactionId);
                if (transactionElement) {
                    let leftColumn = transactionElement.querySelector(".listContainerColumn");

                    createContractSection(
                        messages,
                        leftColumn,
                        contractName,
                        selectedContract.dataset.startDate,
                        selectedContract.dataset.lastPaymentDate,
                        () => removeContractFromTransactionDialog(messages, {id: transactionId}, leftColumn.querySelector(".tooltip"))
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

function getIdsOfTransactionGroup(messages) {
    if (!selectedTransactionGroup) {
        showAlert("ERROR", messages["error_noTransactionGroupSelected"]);
        return [];
    }

    return Array.from(selectedTransactionGroup.querySelectorAll(".listItemSmall"))
        .map(transaction => Number(transaction.id))
        .filter(id => id !== 0);
}

async function removeContractFromTransactions(messages) {
    const transactionIds = getIdsOfTransactionGroup(messages);

    if (transactionIds.length === 0) {
        return;
    }

    try {
        const response = await fetch(`/contracts/${bankAccountId}/data/removeContractFromTransactions`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(transactionIds)
        });

        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === "SUCCESS") {
            selectedTransactionGroup.querySelectorAll(".tooltip").forEach(transaction => {
               transaction.remove();
            });

            toggleTransactionSelection(selectedTransactionGroup);
        }

    } catch (error) {
        console.error("There was an error removing a contract from transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function createContractSection(messages, leftColumn, contractName, startDate, lastPaymentDate, removeButtonHandler) {
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