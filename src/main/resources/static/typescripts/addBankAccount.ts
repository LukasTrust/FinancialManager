async function buildAddBankAccount(): Promise<void> {
    const isSavingsAccount = document.getElementById("isSavingsAccount") as HTMLInputElement;
    const hiddenInputs = document.getElementById("hiddenInputs") as HTMLElement;

    isSavingsAccount.addEventListener("change", () => {
        showHiddenInputs(hiddenInputs);
    });

    const fields = [
        { addButtonId: "addCounterPartyStrings", inputId: "inputCounterPartyStrings", listId: "counterPartySearchStrings" },
        { addButtonId: "addAmountStrings", inputId: "inputAmountStrings", listId: "amountSearchStrings" },
        { addButtonId: "addAmountAfterStrings", inputId: "inputAmountAfterStrings", listId: "amountInBankAfterSearchStrings" },
        { addButtonId: "addDateStrings", inputId: "inputDateStrings", listId: "dateSearchStrings" },
        { addButtonId: "addInterestRateStrings", inputId: "inputInterestRateStrings", listId: "interestRateSearchStrings" }
    ];

    const messages = await fetchLocalization("addBankAccount");
    if (!messages) return;

    const submitButton = document.getElementById("submitButton") as HTMLButtonElement;

    submitButton.addEventListener("click", async (event: Event) => {
        event.preventDefault();
        const listIds = fields.map(field => field.listId);
        await submitAddNewBank(messages, isSavingsAccount.checked, listIds);
    });

    fields.forEach(field => {
        const addButton = document.getElementById(field.addButtonId) as HTMLButtonElement;
        const inputField = document.getElementById(field.inputId) as HTMLInputElement;
        const stringList = document.getElementById(field.listId) as HTMLElement;

        addButton.addEventListener("click", () => {
            const inputValue = inputField.value.trim();

            if (inputValue) {
                addStringToList(messages, stringList, inputValue);
                inputField.value = "";
            } else {
                showAlert("info", messages["error_enterWord"]);
            }
        });
    });
}

async function submitAddNewBank(messages: Record<string, string>, isSavingsAccount: boolean, listIds: string[]): Promise<void> {
    const name = (document.getElementById("name") as HTMLInputElement).value.trim();
    const description = (document.getElementById("description") as HTMLInputElement).value.trim();
    const currencySymbol = (document.getElementById("currencySymbol") as HTMLInputElement).value.trim();
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

    const data: Record<string, any> = { name, description, currencySymbol, type: isSavingsAccount ? "saving" : "checking" };

    if (isSavingsAccount) {
        const interestRate = (document.getElementById("interestRate") as HTMLInputElement).value.trim();
        if (!interestRate) {
            showAlert("warning", messages["error_NoInterestRate"]);
            return;
        }

        data["interestRate"] = interestRate;
    }

    listIds.forEach(listId => {
        const listElement = document.getElementById(listId) as HTMLElement;

        if (listElement) {
            const listItems = Array.from(listElement.children)
                .map(item => item.textContent?.trim())
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

        const responseBody: Response = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType === AlertType.SUCCESS) {
            const bankAccount = responseBody.data;
            addBankAccountToSidebar(messages, bankAccount);

            (document.getElementById("name") as HTMLInputElement).value = "";
            (document.getElementById("description") as HTMLInputElement).value = "";
            (document.getElementById("interestRate") as HTMLInputElement).value = "";

            listIds.forEach(listId => {
                const listElement = document.getElementById(listId) as HTMLElement;
                listElement.innerHTML = "";
            });
        }
    } catch (error) {
        console.error("Error creating bank account:", error);
        showAlert("error", messages["error_generic"]);
    }
}

function showHiddenInputs(hiddenInputs: HTMLElement): void {
    hiddenInputs.classList.toggle("hidden");
}

function addStringToList(messages: Record<string, string>, stringList: HTMLElement, text: string): void {
    const existingItems = Array.from(stringList.children).map(item => item.textContent?.trim() || "");
    if (existingItems.includes(text)) {
        showAlert("Warning", messages["error_alreadyInList"]);
        return;
    }
    createListElement(stringList, text, {}, true, true);
}