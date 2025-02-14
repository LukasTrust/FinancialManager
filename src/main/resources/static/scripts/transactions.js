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

        addRowsToTable(responseBody);
    } catch (error) {
        console.error("There was a error the create bank request:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function addRowsToTable(data) {
    const tableBody = document.getElementById("transactionTableBody");

    const currency = getCurrentCurrencySymbol();

    data.forEach(transaction => {
        const newRow = createElement("tr");

        const trCheckBox = createElement("td");

        const checkBox = createElement("input", "tableCheckbox", "", transaction.id);
        checkBox.type = "checkbox"
        checkBox.style.marginLeft = "10px";
        trCheckBox.appendChild(checkBox);

        const counterParty = createElement("td", "", transaction.counterParty.name);
        counterParty.style.fontWeight = "bold";

        let contract = createElement("td");

        if (transaction.contract) {
            const contractText = createElement("span", "highlightCell highlightCellPink", transaction.contract.name);
            contract.appendChild(contractText);
        }

        const amountInBankBefore = createElement("td", "rightAligned",
            formatNumber(transaction.amountInBankBefore, currency));

        const amount = createElement("td", "rightAligned");

        const amountClass = transaction.amount >= 0 ? "positive" : "negative";

        const amountText = createElement("span", "rightAligned " + amountClass,
            formatNumber(transaction.amount, currency))

        amount.appendChild(amountText);

        const amountInBankAfter = createElement("td", "rightAligned",
            formatNumber(transaction.amountInBankAfter, currency));

        const date = createElement("td", "rightAligned", formatDateString(transaction.date));

        let category = createElement("td");

        if (transaction.category){
            const categoryText = createElement("span", "highlightCell highlightCellOrange", transaction.contract.name);
            category.appendChild(categoryText);
        }

        newRow.appendChild(trCheckBox);
        newRow.appendChild(counterParty);
        newRow.appendChild(contract);
        newRow.appendChild(amountInBankBefore);
        newRow.appendChild(amount);
        newRow.appendChild(amountInBankAfter);
        newRow.appendChild(date);
        newRow.appendChild(category);

        tableBody.appendChild(newRow);
    });
}