async function buildChangeContract(cameFromUrl, transactions) {
    var _a, _b, _c;
    await loadURL("/changeContract");
    const messages = await loadLocalization("changeContract");
    if (!messages)
        return;
    selectedTransactionGroup = null;
    selectedCounterparty = null;
    selectedContract = null;
    createGroupedTransactions(messages, transactions);
    await fillContracts(messages);
    (_a = document.getElementById("backButton")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", async () => await backToOtherView(cameFromUrl));
    (_b = document.getElementById("addGroupToContract")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", async () => await addGroupToContract(messages));
    (_c = document.getElementById("removeGroupFromContract")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", async () => await removeContractFromTransactions(messages));
}
async function loadOnlyContracts(messages) {
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
    }
    catch (error) {
        console.error("There was an error loading the contracts:", error);
        showAlert('error', messages["error_generic"]);
    }
}
async function addGroupToContract(messages) {
    if (!selectedContract) {
        showAlert("ERROR", messages["error_noContractSelected"]);
        return;
    }
    const transactionIds = getIdsOfTransactionGroup(messages);
    if (transactionIds.length === 0) {
        return;
    }
    const contractId = selectedContract.id;
    const elementText = selectedContract.querySelector(".normalText");
    const contractName = (elementText === null || elementText === void 0 ? void 0 : elementText.innerHTML) || "";
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
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS) {
            transactionIds.forEach(transactionId => {
                const transactionElement = document.getElementById(transactionId.toString());
                if (transactionElement) {
                    let leftColumn = transactionElement.querySelector(".verticalContainer");
                    const toolTip = leftColumn.querySelector(".tooltip");
                    if (selectedContract)
                        createContractSection(messages, leftColumn, contractName, selectedContract.dataset.startDate, selectedContract.dataset.lastPaymentDate, () => removeContractFromTransactionDialog(messages, { id: transactionId }, toolTip));
                    toggleTransactionSelection(selectedTransactionGroup);
                }
            });
        }
    }
    catch (error) {
        console.error("There was an error adding a contract to transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}
async function removeContractFromTransactions(messages) {
    const transactionIds = getIdsOfTransactionGroup(messages);
    if (transactionIds.length === 0) {
        return;
    }
    try {
        const response = await fetch(`/transactions/${bankAccountId}/data/removeContractFromTransactions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(transactionIds)
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS) {
            selectedTransactionGroup === null || selectedTransactionGroup === void 0 ? void 0 : selectedTransactionGroup.querySelectorAll(".tooltip").forEach(transaction => {
                transaction.remove();
            });
            toggleTransactionSelection(selectedTransactionGroup);
        }
    }
    catch (error) {
        console.error("There was an error removing a contract from transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}
async function removeContractFromTransaction(messages, transaction, secondRow) {
    try {
        const transactionId = transaction.id;
        const url = `/transactions/${bankAccountId}/data/removeContractFromTransaction/${transactionId}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const dialog = document.querySelector("dialog");
        if (!dialog)
            return;
        if (!response.ok) {
            try {
                const responseBody = await response.json();
                showAlert(responseBody.alertType, responseBody.message, dialog);
            }
            catch (_a) {
                showAlert('error', messages["error_generic"], dialog);
            }
            return;
        }
        // Close the dialog if it exists
        dialog === null || dialog === void 0 ? void 0 : dialog.close();
        // Remove the secondRow from the DOM
        secondRow.remove();
    }
    catch (error) {
        console.error("Error removing contract:", error);
        showAlert('error', messages["error_generic"]);
    }
}
async function fillContracts(messages) {
    const contractData = await loadOnlyContracts(messages);
    createContractList(messages, contractData);
    updateContractAvailability();
}
function createGroupedTransactions(messages, transactions) {
    const transactionGroups = document.getElementById("transactionGroups");
    if (!transactionGroups) {
        console.error("Transaction groups container not found!");
        return;
    }
    if (transactions.length === 0) {
        const header = createAndAppendElement(transactionGroups, "div", "horizontalContainer");
        createAndAppendElement(header, "i", "bi bi-info-circle-fill textHeader");
        createAndAppendElement(header, "div", "normalText", messages["notTransactionSelected"]);
        return;
    }
    const currency = getCurrentCurrencySymbol();
    const groups = groupTransactions(transactions);
    groups.forEach((group, key) => {
        const horizontalContainer = createAndAppendElement(transactionGroups, "div", "horizontalContainer");
        createAndAppendElement(horizontalContainer, "h2", "", group.counterPartyName);
        const listContainer = createAndAppendElement(horizontalContainer, "div");
        horizontalContainer.dataset.counterPartyId = key.toString();
        horizontalContainer.addEventListener("click", () => toggleTransactionSelection(horizontalContainer));
        group.transactions.forEach(item => {
            const content = createAndAppendElement(listContainer, "div", "listItemSmall", "", { id: item.id.toString() });
            const left = createAndAppendElement(content, "div", "verticalContainer");
            createAndAppendElement(left, "div", "normalText", `${messages["amount"]}: ${formatNumber(item.amount, currency)}      ${messages["date"]}: ${formatDateString(item.date)}`);
            let height = 70;
            if (item.contract) {
                createContractSection(messages, left, item.contract.name, item.contract.startDate, item.contract.lastPaymentDate, async (event) => {
                    event.stopPropagation();
                    await removeContractFromTransactionDialog(messages, item, left.querySelector(".tooltip"));
                });
                height = 120;
            }
            const removeButton = createAndAppendElement(content, "button", "removeButton bi bi-x-lg", "");
            removeButton.style.height = `${height}px`;
            removeButton.addEventListener("click", (event) => {
                event.stopPropagation();
                listContainer.removeChild(content);
                // If no transactions remain, remove the group
                if (!listContainer.children.length) {
                    if (horizontalContainer === selectedTransactionGroup) {
                        selectedTransactionGroup = null;
                        toggleTransactionSelection(selectedTransactionGroup);
                    }
                    transactionGroups.removeChild(horizontalContainer);
                }
            });
        });
    });
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
function toggleTransactionSelection(selectedElement) {
    var _a;
    if (!selectedElement)
        return;
    const isSameSelection = selectedTransactionGroup === selectedElement;
    // Deselect previous transaction and contract
    if (selectedTransactionGroup)
        selectedTransactionGroup.classList.remove("selected");
    if (selectedContract) {
        selectedContract.classList.remove("selected");
        selectedContract = null;
    }
    // Update selections
    selectedTransactionGroup = isSameSelection ? null : selectedElement;
    selectedCounterparty = isSameSelection ? null : (_a = selectedElement.dataset.counterPartyId) !== null && _a !== void 0 ? _a : null;
    if (selectedTransactionGroup) {
        selectedTransactionGroup.classList.add("selected");
    }
    updateContractAvailability();
}
function createContractSection(messages, leftColumn, contractName, startDate, lastPaymentDate, removeButtonHandler) {
    // Remove old contract section if it exists
    let oldContract = leftColumn.querySelector(".tooltip");
    if (oldContract) {
        leftColumn.removeChild(oldContract);
    }
    // Add new contract details
    let newContractContainer = createAndAppendElement(leftColumn, "div", "horizontalContainer tooltip tooltipBottom");
    createAndAppendElement(newContractContainer, "div", "normalText", `${messages["contract"]}: ${contractName}`);
    createAndAppendElement(newContractContainer, "div", "tooltipText", `${messages["startDate"]}: ${startDate}   ${messages["lastPaymentDate"]}: ${lastPaymentDate}`);
    // Add remove contract button
    const removeContractButton = createAndAppendElement(newContractContainer, "button", "removeButton bi bi-x-lg");
    removeContractButton.addEventListener("click", removeButtonHandler);
    return newContractContainer;
}
async function removeContractFromTransactionDialog(messages, transaction, secondRow) {
    showMessageBox(messages["removeContractFromTransaction"], "bi bi-file-earmark-fill", messages["removeContractFromTransactionMainText"], messages["yes"], "bi bi-check", messages["cancel"], "bi bi-x-lg", () => removeContractFromTransaction(messages, transaction, secondRow), closeDialog, messages["removeContractFromTransactionTooltip"]);
}
function getIdsOfTransactionGroup(messages) {
    if (!selectedTransactionGroup) {
        showAlert("ERROR", messages["error_noTransactionGroupSelected"]);
        return [];
    }
    return getIdsFromContainer(selectedTransactionGroup);
}
//# sourceMappingURL=changeContract.js.map