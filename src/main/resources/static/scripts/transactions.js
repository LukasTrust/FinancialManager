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
        const newRow = createAndAppendElement(tableBody,"tr");

        const trCheckBox = createAndAppendElement(newRow,"td");

        const checkBox = createAndAppendElement(trCheckBox,"input", "tableCheckbox", "", transaction.id);
        checkBox.type = "checkbox"
        checkBox.style.marginLeft = "10px";

        const counterParty = createAndAppendElement(newRow,"td", "", transaction.counterParty.name);
        counterParty.style.fontWeight = "bold";

        let contract = createAndAppendElement(newRow,"td");

        if (transaction.contract) {
            createAndAppendElement(contract,"span", "highlightCell highlightCellPink", transaction.contract.name);
        }

        createAndAppendElement(newRow,"td", "rightAligned",
            formatNumber(transaction.amountInBankBefore, currency));

        const amount = createAndAppendElement(newRow,"td", "rightAligned");

        const amountClass = transaction.amount >= 0 ? "positive" : "negative";

        createAndAppendElement(amount,"span", "rightAligned " + amountClass,
            formatNumber(transaction.amount, currency))

        createAndAppendElement(newRow,"td", "rightAligned",
            formatNumber(transaction.amountInBankAfter, currency));

        createAndAppendElement(newRow,"td", "rightAligned", formatDateString(transaction.date));

        let category = createAndAppendElement(newRow,"td");

        if (transaction.category){
            createAndAppendElement(category,"span", "highlightCell highlightCellOrange", transaction.contract.name);
        }
    });
}