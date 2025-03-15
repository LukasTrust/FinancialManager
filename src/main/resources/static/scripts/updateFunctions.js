async function updateVisibility(messages, model, updatedContainer, moveToContainer, hide, type) {
    try {
        // Get all IDs
        const ids = getIdsFromContainer(updatedContainer);
        if (ids.length === 0) {
            showAlert("INFO", generalMessages["noDataToUpdate"], model);
            return;
        }
        let startPoint = null;
        switch (type) {
            case Type.CONTRACT:
                startPoint = "contracts";
                break;
            case Type.COUNTERPARTY:
                startPoint = "counterParty";
                break;
            case Type.TRANSACTION:
                startPoint = "transactions";
                break;
        }
        const endpoint = hide ? "hide" : "unHide";
        const response = await fetch(`/${startPoint}/data/${endpoint}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ids),
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, model);
        if (responseBody.alertType === AlertType.SUCCESS) {
            // Animate and move elements
            moveElements(updatedContainer, moveToContainer);
            updateCachedDataAndUI(type, messages, ids);
        }
    }
    catch (error) {
        console.error("Unexpected error in updateVisibility:", error);
        console.error("Type: ", type);
        showAlert("ERROR", generalMessages["error_generic"], model);
    }
}
//# sourceMappingURL=updateFunctions.js.map