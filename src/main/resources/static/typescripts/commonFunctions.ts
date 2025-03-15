async function updateVisibility(
    messages: Record<string, string>,
    model: HTMLElement,
    updatedContainer: HTMLElement,
    moveToContainer: HTMLElement,
    hide: boolean,
    type: Type
): Promise<void> {
    try {
        // Get all IDs
        const ids: number[] = getIdsFromContainer(updatedContainer);

        if (ids.length === 0) {
            showAlert("INFO", messages["noDataToMerge"], model);
            return;
        }

        const endpoint = hide ? "hide" : "unHide";
        const response = await fetch(`/${type.toString()}/data/${endpoint}`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(ids),
        });

        const responseBody: Response = await response.json();

        showAlert(responseBody.alertType, responseBody.message, model);

        if (responseBody.alertType === AlertType.SUCCESS) {
            // Animate and move elements
            moveElements(updatedContainer, moveToContainer);
            updateCachedDataAndUI(type, messages, ids);
        }
    } catch (error) {
        console.error("Unexpected error in updateVisibility:", error);
        console.error("Type:", type);
        showAlert("ERROR", messages["error_generic"], model);
    }
}

async function mergeData(model: HTMLElement, messages: Record<string, string>, leftSide: HTMLElement, updatedContainer: HTMLElement, type: Type): Promise<void> {
    try {
        if (type === Type.TRANSACTION) {
            return;
        }

        const headerId = getIdsFromContainer(leftSide)[0];

        const ids: number[] = getIdsFromContainer(updatedContainer);
        if (ids.length === 0) {
            showAlert("INFO", messages["noDataToMerge"], model);
            return;
        }

        const response = await fetch(`/${type.toString()}/data/merge/${headerId}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(ids),
        });

        const responseBody: Response = await response.json();

        showAlert(responseBody.alertType, responseBody.message, model);

        updateCounterParty(responseBody.data, counterPartyData);
        updateCounterParty(responseBody.data, filteredCounterPartyData);

        removeElements(updatedContainer);
        removeMergedCounterParties(ids, messages);
    } catch (error) {
        console.error("There was an error merging the counterParties:", error);
        showAlert('error', messages["error_generic"], model);
    }
}