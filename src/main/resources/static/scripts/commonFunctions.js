function getBaseURL(type) {
    let url = `/${type.toString()}`;
    if (type !== Type.COUNTERPARTY && type !== Type.CATEGORY) {
        url += `/${bankAccountId}`;
    }
    url += `/data`;
    return url;
}
async function updateVisibility(messages, model, updatedContainer, moveToContainer, hide, type) {
    try {
        // Get all IDs
        const ids = getIdsFromContainer(updatedContainer);
        if (ids.length === 0) {
            showAlert("INFO", messages["noDataToMerge"], model);
            return;
        }
        const endpoint = hide ? "hide" : "unHide";
        let url = getBaseURL(type);
        url += `/${endpoint}`;
        const response = await fetch(url, {
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
        console.error("Type:", type);
        showAlert("ERROR", messages["error_generic"], model);
    }
}
async function updateField(id, field, newValue, messages, type) {
    try {
        let url = getBaseURL(type);
        url += `/${id}/change/${field}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ newValue })
        });
        if (!response.ok)
            await showAlertFromResponse(response);
    }
    catch (error) {
        console.error("Unexpected error in updateField:", error);
        console.error("Type:", type);
        showAlert('error', messages["error_generic"]);
    }
}
async function mergeData(model, messages, leftSide, updatedContainer, type) {
    try {
        if (type === Type.TRANSACTION) {
            return;
        }
        const headerId = getIdsFromContainer(leftSide)[0];
        const ids = getIdsFromContainer(updatedContainer);
        if (ids.length === 0) {
            showAlert("INFO", messages["noDataToMerge"], model);
            return;
        }
        let url = getBaseURL(type);
        url += `/merge/${headerId}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids),
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, model);
        if (response.ok) {
            removeElements(updatedContainer);
            if (type === Type.COUNTERPARTY) {
                updateCounterParty(responseBody.data, counterPartyData);
                updateCounterParty(responseBody.data, filteredCounterPartyData);
                removeMergedCounterParties(ids, messages);
            }
            else if (type === Type.CONTRACT) {
                updateContract(response.data, contractData);
                updateContract(response.data, filteredContractData);
                removeMergedContracts(ids, messages);
            }
        }
    }
    catch (error) {
        console.error("There was an error merging the counterParties:", error);
        showAlert('error', messages["error_generic"], model);
    }
}
//# sourceMappingURL=commonFunctions.js.map