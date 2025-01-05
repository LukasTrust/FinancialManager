document.addEventListener("DOMContentLoaded", async () => {
    const fields = [
        { addButtonId: "addCounterPartyStrings", inputId: "inputCounterPartyStrings", listId: "counterPartyStrings", listName: "Counter Party" },
        { addButtonId: "addAmountStrings", inputId: "inputAmountStrings", listId: "amountStrings", listName: "Amount" },
        { addButtonId: "addAmountAfterStrings", inputId: "inputAmountAfterStrings", listId: "amountAfterStrings", listName: "Amount After" },
        { addButtonId: "addDateStrings", inputId: "inputDateStrings", listId: "dateStrings", listName: "Date" }
    ];

    fields.forEach(field => {
        const addButton = document.getElementById(field.addButtonId);
        const inputField = document.getElementById(field.inputId);
        const stringList = document.getElementById(field.listId);

        addButton.addEventListener("click", () => {
            const inputValue = inputField.value.trim();
            if (inputValue) {
                addStringToList(inputValue, stringList, field.listName);
                inputField.value = ""; // Clear input field
            }
        });
    });
});

// Function to add a string to the list
function addStringToList(input, stringList, listName) {
    // Check if the string is already in the list
    // const existingItems = Array.from(stringList.children).map(item => item.textContent.trim());
    // if (existingItems.includes(input)) {
    //     showAlert('Warning', "This string is already in the list!");
    //     return;
    // }

    // Create a new list item
    const newString = document.createElement("div");
    newString.textContent = input;
    newString.className = "listItem";

    // Create a remove button
    const removeButton = document.createElement("button");
    removeButton.className = "removeButton bi bi-x-square";
    removeButton.addEventListener("click", () => {
        removeStringFromList(newString, stringList, listName);
    });

    // Append the button to the list item and the item to the list
    newString.appendChild(removeButton);

    stringList.appendChild(newString);

    // Show an alert message indicating the item was added
    showAlert('success', `'${input}' was added to the ${listName} list.`);
}

// Function to remove a string from the list
function removeStringFromList(item, stringList, listName) {
    stringList.removeChild(item);
    showAlert('success', `The item '${item.textContent}' was removed from the ${listName} list.`);
}