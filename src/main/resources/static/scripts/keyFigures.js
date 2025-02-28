async function loadKeyFigures(messages, startDate = null, endDate = null) {
    try {
        let url = `/bankAccountOverview/${bankAccountId}/data/keyFigures`;
        const params = new URLSearchParams();

        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            showAlert("ERROR", messages["error_loadingKeyFigures"]);
            return;
        }

        const responseBody = await response.json();

        createKeyFigures(responseBody)
    } catch (error) {
        showAlert("ERROR", messages["error_generic"]);
        console.error("Error loading key figures", error);
    }
}

function createKeyFigures(keyFigures) {
    const keyFiguresContainer = document.getElementById("keyFiguresContainer");
    keyFiguresContainer.innerHTML = '';

    if (!keyFiguresContainer) return;

    const fragment = document.createDocumentFragment(); // Batch DOM updates

    const currency = getCurrentCurrencySymbol();

    for (const keyFigure of keyFigures) {
        if (!keyFigure) continue; // Skip invalid entries

        // Create Key Figure Box
        const keyFigureBox = createAndAppendElement(fragment,"div", "boxContainer");

        // Create Header
        const keyFigureHeader = createAndAppendElement(keyFigureBox,"div", "keyFigureHeader");
        createAndAppendElement(keyFigureHeader,"span", "", keyFigure.name, {style: "cursor: pointer"});

        // Tooltip
        const keyFigureTooltip = createAndAppendElement(keyFigureHeader,"div", "tooltip bi bi-info");
        createAndAppendElement(keyFigureTooltip,"span", "tooltipText", keyFigure.tooltip);

        // Value Container
        const classForValue = keyFigure.value >= 0 ? "positive" : "negative";
        const valueContainer = createAndAppendElement(keyFigureBox,"div", classForValue,
            null,{ style: "padding: 10px"});

        const iconClass = keyFigure.value >= 0 ? "bi bi-arrow-up" : "bi bi-arrow-down";
        createAndAppendElement(valueContainer,"i", iconClass);

        createAndAppendElement(valueContainer,"span", "",
            formatNumber(keyFigure.value, currency), {style: "margin: 20px"});
    }

    keyFiguresContainer.appendChild(fragment);
}