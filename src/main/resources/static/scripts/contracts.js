async function buildChangeContract(cameFromUrl, transactions) {
    await loadURL("/changeContract", "/transactions")

    const messages = await fetchLocalization("changeContract");

    createGroupedTransactions(messages, transactions);

    document.getElementById("backButton").addEventListener("click", async () => await backToOtherView(cameFromUrl));
}

function createGroupedTransactions(messages, transactions) {
    let groups = groupTransactions(transactions); // Group transactions by counterparty
    const transactionGroups = document.getElementById("transactionGroups");

    const currency = getCurrentCurrencySymbol();

    groups.forEach((group, key) => {
        const listContainerHeader = createAndAppendElement(transactionGroups, "div", "listContainerHeader");
        createAndAppendElement(listContainerHeader, "h2", "", key, {style: "margin-top: 10px"});
        const listContainer = createAndAppendElement(listContainerHeader, "div", "listContainerColumn", "",
            {style: "overflow: visible;"});

        group.forEach(item => {
            const content = createAndAppendElement(listContainer, "div", "listItem", "",
                {id: item.id});
            const left = createAndAppendElement(content, "div", "flexContainerColumn");

            const firstRow = createAndAppendElement(left, "div", "listContainer");

            createAndAppendElement(firstRow, "div", "normalSpan",
                `${messages["amount"]}: ${formatNumber(item.amount, currency)}      ${messages["date"]}: ${formatDateString(item.date)}`,
                {style: "margin-right: 30px"});

            let height = 70;

            if (item.contract) {
                const secondRow = createAndAppendElement(left, "div", "listContainer");

                createAndAppendElement(secondRow, "div", "normalSpan", `${messages["contract"]}: ${item.contract.name}`);

                createAndAppendElement(secondRow, "button", "iconButton bi bi-x-lg", "",
                    { style: "background-color: #ff4757" },
                    { click: () => removeContractFromTransactionDialog(messages, item, secondRow) }
                );

                height = 120;
            }

            createAndAppendElement(content, "button", "removeButton bi bi-x-lg",
                null, {style: `height: ${height}px; overflow: visible;`},
                {click: () => listContainer.removeChild(content)
            });
        });
    });
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
        const key = transaction.counterParty.name;
        if (!map.has(key)) {
            map.set(key, []);
        }

        map.get(key).push(transaction);
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
