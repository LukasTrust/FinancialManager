async function buildManageCategories() {
    var _a, _b, _c;
    const messages = await loadLocalization("manageCategories");
    if (!messages)
        return;
    const type = Type.CATEGORY;
    await loadData(type, messages);
    await loadData(Type.COUNTERPARTY, messages);
    splitDataIntoPages(messages, type, categoryData);
    setUpSorting();
    (_a = document.getElementById("addButton")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", () => showAddCategoryDialog(messages));
    (_b = document.getElementById("searchBarInput")) === null || _b === void 0 ? void 0 : _b.addEventListener("input", () => searchTable(messages, type));
    (_c = document.getElementById("deleteButton")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", () => showDeleteCategoryDialog(messages));
}
async function addCategory(dialogContent, name, description, maxSpendingPerMonth, counterParties, messages) {
    try {
        const response = await fetch(`/categories/data/addCategory`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: name,
                description: description,
                maxSpendingPerMonth: maxSpendingPerMonth,
                counterParties: counterParties
            })
        });
        if (!response.ok) {
            await showAlertFromResponse(response, dialogContent);
            return false;
        }
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, dialogContent);
        if (responseBody.alertType == AlertType.SUCCESS) {
            const category = responseBody.data;
            if (categoryData.find(c => c.id === category.id) === undefined) {
                categoryData.push(category);
                if (filteredCategoryData.find(c => c.id === category.id) === undefined) {
                    filteredCategoryData.push(category);
                }
            }
            splitDataIntoPages(messages, Type.CATEGORY, filteredCategoryData);
            return true;
        }
        return false;
    }
    catch (error) {
        console.warn("There was an error adding the category", error);
        return false;
    }
}
async function addCounterPartyToCategory(categoryId, counterPartyId) {
    try {
        const response = await fetch(`/categories/data/addCounterPartyToCategory`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                categoryId: categoryId,
                counterPartyId: counterPartyId
            })
        });
        await showAlertFromResponse(response);
    }
    catch (error) {
        console.warn("There was an error adding the counter party to the category", error);
    }
}
async function removeCounterPartyFromCategory(categoryId, counterPartyId) {
    try {
        const response = await fetch(`/categories/data/removeCounterPartyFromCategory`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                categoryId: categoryId,
                counterPartyId: counterPartyId
            })
        });
        await showAlertFromResponse(response);
    }
    catch (error) {
        console.warn("There was an error removing the counter party from the category", error);
    }
}
function showDeleteCategoryDialog(messages) {
    const dialogContent = createDialogContent(messages["deleteHeader"], "bi bi bi-trash-fill", 0, 0);
    createAndAppendElement(dialogContent, "h2", "marginBottom marginLeftBig", messages["deleteInfo"]);
    const categories = getCheckedData(Type.CATEGORY);
    const listSection = createListSection(dialogContent, messages["leftHeader"], Type.CATEGORY, categories, false, true, false);
    if (!categories || categories.length === 0) {
        const childContainer = listSection.querySelector('div.flexGrow');
        createAndAppendElement(childContainer, "h2", "red marginTopBig", messages["noCategoriesToDelete"]);
    }
    const submitButton = createAndAppendElement(dialogContent, "button", "iconButton tooltip tooltipBottom marginTopBig");
    createAndAppendElement(submitButton, "i", "bi bi-trash-fill");
    createAndAppendElement(submitButton, "span", "normalText", messages["submitDelete"]);
    createAndAppendElement(submitButton, "span", "tooltipText", messages["submitDeleteTooltip"]);
    submitButton.addEventListener("click", async (event) => {
        event.preventDefault();
        await deleteCategories(dialogContent, listSection, messages);
    });
}
async function deleteCategories(dialog, listSection, messages) {
    try {
        const ids = getIdsFromContainer(listSection);
        if (ids.length === 0) {
            showAlert(AlertType.WARNING, messages["noCategoriesToDelete"], dialog);
            return;
        }
        const response = await fetch(`/categories/data/deleteCategories`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, dialog);
        if (response.ok) {
            removeElements(listSection);
            removeDeletedCategories(ids, messages);
        }
    }
    catch (error) {
        console.warn("There was an error deleting the categories", error);
    }
}
function removeDeletedCategories(categoryIds, messages) {
    categoryData = categoryData.filter(item => !categoryIds.includes(item.id));
    filteredCategoryData = filteredCategoryData.filter(item => !categoryIds.includes(item.id));
    splitDataIntoPages(messages, Type.CATEGORY, filteredCategoryData);
}
function categoryToListElementObjectArray(categories) {
    let listElementObjects = [];
    categories.forEach(category => {
        const listElementObject = {
            id: category.id,
            text: category.name,
            toolTip: category.description,
        };
        listElementObjects.push(listElementObject);
    });
    return listElementObjects;
}
function showAddCategoryDialog(messages) {
    const dialogContent = createDialogContent(messages["addHeader"], "bi bi bi-plus-circle", 0, 0, true);
    createAndAppendElement(dialogContent, "h2", "marginBottom marginLeftBig", messages["addInfo"]);
    const form = createAndAppendElement(dialogContent, "form", "flexGrow");
    const nameWrapper = createAndAppendElement(form, "div", "verticalContainer");
    createAndAppendElement(nameWrapper, "h3", "marginBottom", messages["name"]);
    const name = createInputBox(nameWrapper, "bi bi-pencil-fill", "name", "text", "", messages["name"], "marginBottomBig");
    const descriptionWrapper = createAndAppendElement(form, "div", "verticalContainer");
    createAndAppendElement(descriptionWrapper, "h3", "marginBottom", messages["description"]);
    const description = createInputBox(descriptionWrapper, "bi bi-pencil-fill", "description", "text", "", messages["description"], "marginBottomBig");
    const maxSpendingPerMonthWrapper = createAndAppendElement(form, "div", "verticalContainer");
    createAndAppendElement(maxSpendingPerMonthWrapper, "h3", "marginBottom", messages["maxSpendingPerMonth"]);
    const maxSpendingPerMonth = createInputBox(maxSpendingPerMonthWrapper, "bi bi-pencil-fill", "maxSpendingPerMonth", "number", "", messages["maxSpendingPerMonth"], "marginBottomBig");
    const dropdown = createDropBoxForCategory("counterPartyDropdown", form, [], messages);
    const submitButton = createAndAppendElement(form, "button", "iconButton tooltip tooltipBottom marginTopBig marginBottomBig");
    createAndAppendElement(submitButton, "i", "bi bi-plus-lg");
    createAndAppendElement(submitButton, "span", "normalText", messages["submitAdd"]);
    createAndAppendElement(submitButton, "span", "tooltipText", messages["submitAddTooltip"]);
    submitButton.addEventListener("click", async (event) => {
        event.preventDefault();
        const nameValue = name.value;
        const descriptionValue = description.value;
        const maxSpendingPerMonthValue = maxSpendingPerMonth.value;
        const selectedItems = dropdown.getSelectedItems();
        if (nameValue === "") {
            showAlert("ERROR", messages["error_missing_name"], dialogContent);
            return;
        }
        if (maxSpendingPerMonthValue !== "" && isNaN(parseFloat(maxSpendingPerMonthValue))) {
            showAlert("ERROR", messages["error_invalid_maxSpendingPerMonth"], dialogContent);
            return;
        }
        const maxSpendingPerMonthNumber = maxSpendingPerMonthValue !== "" ? parseFloat(maxSpendingPerMonthValue) : 0;
        const result = await addCategory(dialogContent, nameValue, descriptionValue, maxSpendingPerMonthNumber, selectedItems.map(item => item.id), messages);
        if (result) {
            name.value = "";
            description.value = "";
            maxSpendingPerMonth.value = "";
            dropdown.clearSelection();
        }
    });
}
function createDropBoxForCategory(id, parent, preSelectedItems, messages, left = false, onCheck, onUncheck) {
    const counterPartyWrapper = createAndAppendElement(parent, "div", "verticalContainer");
    const counterPartiesClass = left ? "marginBottom alignSelfStart" : "marginBottom";
    createAndAppendElement(counterPartyWrapper, "h3", counterPartiesClass, messages["counterPartySelection"]);
    return new CheckboxDropdown({
        id,
        parent: counterPartyWrapper,
        items: counterParties,
        preSelectedItems,
        defaultText: messages["selectOptionText"],
        clearText: messages["selectOptionClear"],
        multiSelect: true,
        onCheck,
        onUncheck,
    });
}
function addCategoriesTable(data, messages) {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody)
            return;
        const toolTip = messages["addCounterPartyTooltip"];
        data.forEach(category => {
            createCategoryRow(tableBody, category, toolTip, messages);
        });
    }
    catch (error) {
        console.error("Unexpected error in addCategoriesTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function createCategoryRow(tableBody, category, toolTip, messages) {
    if (!category || typeof category !== "object") {
        console.warn(`Warning: Skipping invalid category:`, category);
        return;
    }
    // Main row
    const newRow = createAndAppendElement(tableBody, "tr", "mainRow", "", {
        id: category.id.toString(),
        "data-sort-key": category.id.toString()
    });
    // Sub Row for Search Strings
    const subRow = createAndAppendElement(tableBody, "tr", "", "", {
        "data-pair": category.id.toString(),
    });
    createCheckBoxForTable(newRow, subRow, category.id, false);
    // Name cell
    const name = createAndAppendElement(newRow, "td");
    const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", category.name);
    debounceInputChange(nameInput, (id, newValue, messages) => updateField(id, "name", newValue, messages, Type.CATEGORY), category.id, messages);
    const description = createAndAppendElement(newRow, "td");
    const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "description", "text", category.description);
    debounceInputChange(descriptionInput, (id, newValue, messages) => updateField(id, "description", newValue, messages, Type.CATEGORY), category.id, messages);
    const maxSpendingPerMonth = createAndAppendElement(newRow, "td");
    const maxSpendingPerMonthInput = createInputBox(maxSpendingPerMonth, "bi bi-pencil-fill", "maxSpendingPerMonth", "number", category.description);
    debounceInputChange(maxSpendingPerMonthInput, (id, newValue, messages) => updateField(id, "maxSpendingPerMonth", newValue, messages, Type.CATEGORY), category.id, messages);
    const counterPartyCell = createAndAppendElement(subRow, "td", "", "", { colspan: "4" });
    createDropBoxForCategory(category.id.toString(), counterPartyCell, category.counterParties, messages, true, async (item) => {
        await addCounterPartyToCategory(category.id, Number(item.id));
    }, async (item) => {
        await removeCounterPartyFromCategory(category.id, Number(item.id));
    });
    addHoverToOtherElement(newRow, subRow);
}
function filterCategories(messages, searchString) {
    try {
        filteredCategoryData = categoryData.filter(category => {
            var _a, _b, _c, _d;
            return ((_a = category.name) === null || _a === void 0 ? void 0 : _a.toLowerCase().includes(searchString)) ||
                ((_b = category.description) === null || _b === void 0 ? void 0 : _b.toLowerCase().includes(searchString)) ||
                ((_c = category.maxSpendingPerMonth) === null || _c === void 0 ? void 0 : _c.toString().toLowerCase().includes(searchString)) ||
                ((_d = category.counterParties) === null || _d === void 0 ? void 0 : _d.some(counterParty => { var _a; return (_a = counterParty.name) === null || _a === void 0 ? void 0 : _a.toLowerCase().includes(searchString); }));
        });
        splitDataIntoPages(messages, Type.CATEGORY, filteredCategoryData);
    }
    catch (error) {
        console.error("Unexpected error in filterCounterParties:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
//# sourceMappingURL=manageCategories.js.map