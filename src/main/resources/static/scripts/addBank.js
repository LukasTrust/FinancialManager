document.addEventListener("DOMContentLoaded", async () => {
    const fields = [
        {addButtonId: "addCounterPartyStrings", inputId: "inputCounterPartyStrings", listId: "counterPartyStrings"},
        {addButtonId: "addAmountStrings", inputId: "inputAmountStrings", listId: "amountStrings"},
        {addButtonId: "addAmountAfterStrings", inputId: "inputAmountAfterStrings", listId: "amountAfterStrings"},
        {addButtonId: "addDateStrings", inputId: "inputDateStrings", listId: "dateStrings"}
    ];

    fields.forEach(field => {
        const addButton = document.getElementById(field.addButtonId);
        const inputField = document.getElementById(field.inputId);
        const stringList = document.getElementById(field.listId);

        addButton.addEventListener("click", () => {
            const inputValue = inputField.value.trim();
            if (inputValue) {
                addStringToList(inputValue, stringList);
                inputField.value = ""; // Clear input field
            }
            else{
                showAlert('info', "Please enter a word to add to the search strings");
            }
        });
    });
});

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