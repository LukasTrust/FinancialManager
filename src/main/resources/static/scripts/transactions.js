async function buildTransactions() {
    const messages = await fetchLocalization("transactions");

    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '") // Split by ', ' to separate the months
        .map(month => month.replace(/'/g, ''));

    await loadTransactions(messages);
}

async function loadTransactions(messages) {

    try {
        const response = await fetch(`/transactions/${bankAccountId}/data`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            showAlert("ERROR", messages["error_loadingTransactions"]);
            return;
        }

        const responseBody = await response.json();

        addRowsToTable(responseBody, messages);
    } catch (error) {
        console.error("There was a error the create bank request:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function addRowsToTable(data, messages) {
    try {
        const tableBody = document.getElementById("transactionTableBody");
        if (!tableBody) {
            showAlert("ERROR", messages["error_tableNotFound"])
            console.error("Error: Table body element with ID 'transactionTableBody' not found.");
            return;
        }

        const currency = getCurrentCurrencySymbol();

        data.forEach((transaction, index) => {
            if (!transaction || typeof transaction !== "object") {
                console.warn(`Warning: Skipping invalid transaction at index ${index}.`, transaction);
                return;
            }

            const newRow = createAndAppendElement(tableBody, "tr");

            // Checkbox cell
            const trCheckBox = createAndAppendElement(newRow, "td");
            const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
                type: "checkbox",
                id: transaction.id,
                style: "margin-left: 10px;",
            });

            checkBox.addEventListener("change", () => { updateRowStyle(newRow, checkBox)});

            newRow.addEventListener("click", (event) => {
                if (event.target.type === "checkbox") return; // Don't toggle when clicking checkbox
                checkBox.checked = !checkBox.checked;
                updateRowStyle(newRow, checkBox);
            });

            // Counterparty cell
            createAndAppendElement(newRow, "td", "", transaction.counterParty?.name, {
                style: "font-weight: bold;",
            });

            // Contract cell
            let contract = createAndAppendElement(newRow, "td");
            if (transaction.contract?.name) {
                createAndAppendElement(contract, "span", "highlightCell highlightCellPink",
                    transaction.contract.name);
            }

            // Amount before cell
            createAndAppendElement(newRow, "td", "rightAligned",
                formatNumber(transaction.amountInBankBefore, currency)
            );

            // Amount cell with positive/negative styling
            const amount = createAndAppendElement(newRow, "td", "rightAligned");
            const amountClass = transaction.amount >= 0 ? "positive" : "negative";
            createAndAppendElement(amount, "span", `rightAligned ${amountClass}`,
                formatNumber(transaction.amount, currency)
            );

            // Amount after cell
            createAndAppendElement(newRow, "td", "rightAligned",
                formatNumber(transaction.amountInBankAfter, currency)
            );

            // Date cell
            createAndAppendElement(newRow, "td", "rightAligned",
                formatDateString(transaction.date)
            );

            // Category cell
            let category = createAndAppendElement(newRow, "td");
            if (transaction.category?.name) {
                createAndAppendElement(category, "span", "highlightCell highlightCellOrange", transaction.category.name);
            }
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToTable:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function updateRowStyle(newRow, checkBox) {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}
