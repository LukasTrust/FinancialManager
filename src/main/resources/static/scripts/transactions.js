async function buildTransactions() {
    const messages = await fetchLocalization("transactions");

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

    data.forEach(transaction => {
        const newRow = createElement("tr");

        const trCheckBox = createElement("td");
        const checkBox = createElement("input", "", "", transaction.id);
        checkBox.type = "checkbox";
        trCheckBox.appendChild(checkBox);

        const counterParty = createElement("td", "", transaction.counterParty.name);

        let contract;

        if (transaction.contract) {
            contract = createElement("td", "", transaction.contract.name);
        } else {
            contract = createElement("td");
        }

        const amountInBankBefore = createElement("td", "", transaction.amountInBankBefore);

        const amount = createElement("td", "", transaction.amount);

        const amountInBankAfter = createElement("td", "", transaction.amountInBankAfter);

        const date = createElement("td", "", transaction.date);

        let category;

        if (transaction.category){
            category = createElement("td", "", transaction.category.name);
        }
        else{
            category = createElement("td");
        }

        newRow.appendChild(checkBox);
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