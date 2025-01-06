document.addEventListener("DOMContentLoaded", async () => {
    const isSavingsAccount = document.getElementById("isSavingsAccount");
    const hiddenInputs = document.getElementById("hiddenInputs");

    isSavingsAccount.addEventListener("change", () => {
        showHiddenInputs(hiddenInputs)
    })

    const fields = [
        {
            addButtonId: "addCounterPartyStrings",
            inputId: "inputCounterPartyStrings",
            listId: "counterPartySearchStrings"
        },
        {addButtonId: "addAmountStrings", inputId: "inputAmountStrings", listId: "amountSearchStrings"},
        {
            addButtonId: "addAmountAfterStrings",
            inputId: "inputAmountAfterStrings",
            listId: "amountInBankAfterSearchStrings"
        },
        {addButtonId: "addDateStrings", inputId: "inputDateStrings", listId: "dateSearchStrings"},
        {
            addButtonId: "addInterestRateStrings",
            inputId: "inputinterestRateStrings",
            listId: "interestRateSearchStrings"
        }
    ];

    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", async () => {
        event.preventDefault();
        // Extract the listIds from the fields array
        const listIds = fields.map(field => field.listId);
        await submitAddNewBank(isSavingsAccount.checked, listIds);
    })

    fields.forEach(field => {
        const addButton = document.getElementById(field.addButtonId);
        const inputField = document.getElementById(field.inputId);
        const stringList = document.getElementById(field.listId);

        addButton.addEventListener("click", () => {
            const inputValue = inputField.value.trim();
            if (inputValue) {
                addStringToList(inputValue, stringList);
                inputField.value = ""; // Clear input field
            } else {
                showAlert('info', "Please enter a word to add to the search strings");
            }
        });
    });
});

async function submitAddNewBank(isSavingsAccount, listIds) {
    const name = document.getElementById("name").value.trim();
    const description = document.getElementById("description").value.trim();

    // Check if all fields are filled
    if (!name) {
        showAlert('warning', "Please enter a name for the bank account");
        return;
    }

    const data = {
        name,
        description,
        type: isSavingsAccount ? "saving" : "checking"
    };

    if (isSavingsAccount) {
        const interestRate = document.getElementById("interestRate").value.trim();

        if (!interestRate) {
            showAlert('warning', "Please enter a interest rate for the savings account");
            return;
        }

        data["interestRate"] = interestRate
    }

    // Loop over listIds and create a semicolon-separated string for each list
    listIds.forEach((listId, index) => {
        const listElement = document.getElementById(listId);

        if (listElement) {
            const listItems = Array.from(listElement.children)
                .map(item => item.textContent.trim()) // Extract text content
                .filter(text => text.length > 0); // Filter out empty items

            if (listItems.length > 0) {
                data[listId] = listItems; // Assign as an array
            } else {
                data[listId] = null; // Set to null if empty
            }
        }
    });

    try {
        const response = await fetch('/addBankAccount', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
            body: JSON.stringify(data),
        });

        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        // Clear when successful
        if (responseBody.alertType.toLowerCase() === 'success') {
            const bankAccount = responseBody.data;

            addBankAccountToSidebar(bankAccount.name, bankAccount.id, bankAccount.interestRate != null);

            document.getElementById("name").value = '';
            document.getElementById("description").value = '';
            document.getElementById("interestRate").value = '';

            listIds.forEach((listId, index) => {
                const listElement = document.getElementById(listId);

                listElement.innerHTML = '';
            });
        }

    } catch (error) {
        console.error("There was a problem with the create bank request:", error);
        showAlert('error', "An unexpected error occurred. Please try again");
    }
}

function showHiddenInputs(hiddenInputs) {
    hiddenInputs.classList.toggle("hidden");
}

// Function to add a string to the list
function addStringToList(input, stringList) {
    //Check if the string is already in the list
    const existingItems = Array.from(stringList.children).map(item => item.textContent.trim());
    if (existingItems.includes(input)) {
        showAlert('Warning', "This string is already in the list!");
        return;
    }

    // Create a new list item
    const newString = document.createElement("div");
    newString.textContent = input;
    newString.className = "listItem";

    // Create a remove button
    const removeButton = document.createElement("button");
    removeButton.className = "removeButton bi bi-x-square";
    removeButton.addEventListener("click", () => {
        removeStringFromList(newString, stringList);
    });

    // Append the button to the list item and the item to the list
    newString.appendChild(removeButton);
    stringList.appendChild(newString);
}

// Function to remove a string from the list
function removeStringFromList(item, stringList) {
    stringList.removeChild(item);
}