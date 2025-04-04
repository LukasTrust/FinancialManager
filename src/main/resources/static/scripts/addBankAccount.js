async function buildAddBankAccount() {
    const isSavingsAccount = document.getElementById("isSavingsAccount");
    const hiddenInputs = document.getElementById("hiddenInputs");
    isSavingsAccount.addEventListener("change", () => {
        showHiddenInputs(hiddenInputs);
    });
    const messages = await loadLocalization("addBankAccount");
    if (!messages)
        return;
    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", async (event) => {
        event.preventDefault();
        const listIds = searchStringFields.map(field => field.listId);
        await submitAddNewBank(messages, isSavingsAccount.checked, listIds);
    });
    setUpSearchStringFields(messages);
}
async function submitAddNewBank(messages, isSavingsAccount, listIds) {
    const name = document.getElementById("name").value.trim();
    const description = document.getElementById("description").value.trim();
    const currencySymbol = document.getElementById("currencySymbol").value.trim();
    const currencySymbols = ["$", "€", "£", "¥", "₣", "₱", "₹", "₽", "₩", "₪", "₫", "₺"];
    if (!name) {
        showAlert("warning", messages["error_bankAccountName"]);
        return;
    }
    if (!currencySymbol) {
        showAlert("warning", messages["error_currencySymbol"]);
        return;
    }
    if (!currencySymbols.includes(currencySymbol)) {
        showAlert("warning", messages["error_unknownCurrencySymbol"]);
        return;
    }
    const data = { name, description, currencySymbol, type: isSavingsAccount ? "saving" : "checking" };
    if (isSavingsAccount) {
        const interestRate = document.getElementById("interestRate").value.trim();
        if (!interestRate) {
            showAlert("warning", messages["error_NoInterestRate"]);
            return;
        }
        data["interestRate"] = interestRate;
    }
    listIds.forEach(listId => {
        const listElement = document.getElementById(listId);
        if (listElement) {
            const listItems = Array.from(listElement.children)
                .map(item => { var _a; return (_a = item.textContent) === null || _a === void 0 ? void 0 : _a.trim(); })
                .filter(text => text && text.length > 0);
            data[listId] = listItems.length > 0 ? listItems : null;
        }
    });
    try {
        const response = await fetch("/addBankAccount", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Accept": "application/json" },
            body: JSON.stringify(data)
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS) {
            const bankAccount = responseBody.data;
            addBankAccountToSidebar(messages, bankAccount);
            document.getElementById("name").value = "";
            document.getElementById("description").value = "";
            document.getElementById("interestRate").value = "";
            listIds.forEach(listId => {
                const listElement = document.getElementById(listId);
                listElement.innerHTML = "";
            });
        }
    }
    catch (error) {
        console.error("Error creating bank account:", error);
        showAlert("error", messages["error_generic"]);
    }
}
function showHiddenInputs(hiddenInputs) {
    hiddenInputs.classList.toggle("hidden");
}
function addStringToList(messages, stringList, text, removeCallback) {
    const existingItems = Array.from(stringList.children).map(item => { var _a; return ((_a = item.textContent) === null || _a === void 0 ? void 0 : _a.trim()) || ""; });
    if (existingItems.includes(text)) {
        showAlert("Warning", messages["error_alreadyInList"]);
        return;
    }
    createListElement(stringList, text, {}, true, true, null, removeCallback);
}
//# sourceMappingURL=addBankAccount.js.map