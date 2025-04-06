async function loadKeyFigures(messages, startDate = null, endDate = null, solo = true) {
    try {
        let url = solo == true ? `/bankAccountOverview/${bankAccountId}/data/keyFigures` : `/dashboard/data/keyFigures`;
        const params = new URLSearchParams();
        if (startDate)
            params.append("startDate", startDate);
        if (endDate)
            params.append("endDate", endDate);
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
        const responseBody = await response.json(); // Enforce TypeScript type
        createKeyFigures(responseBody);
    }
    catch (error) {
        showAlert("ERROR", messages["error_generic"]);
        console.error("Error loading key figures", error);
    }
}
function createKeyFigures(keyFigures) {
    const keyFiguresContainer = document.getElementById("keyFiguresContainer");
    if (!keyFiguresContainer)
        return;
    keyFiguresContainer.innerHTML = '';
    const fragment = document.createDocumentFragment();
    const currency = getCurrentCurrencySymbol();
    for (const keyFigure of keyFigures) {
        if (!keyFigure)
            continue; // Skip invalid entries
        // Create Key Figure Box
        const keyFigureBox = createAndAppendElement(fragment, "div", "verticalContainer boxContainer");
        // Create Header
        const keyFigureHeader = createAndAppendElement(keyFigureBox, "div", "textHeader marginBottom");
        createAndAppendElement(keyFigureHeader, "span", "", keyFigure.name);
        // Tooltip
        const keyFigureTooltip = createAndAppendElement(keyFigureHeader, "div", "tooltip bi bi-info");
        createAndAppendElement(keyFigureTooltip, "span", "tooltipText", keyFigure.tooltip);
        // Value Container
        const classForValue = keyFigure.value >= 0 ? "positive" : "negative";
        const valueContainer = createAndAppendElement(keyFigureBox, "div", classForValue);
        const iconClass = keyFigure.value >= 0 ? "bi bi-arrow-up" : "bi bi-arrow-down";
        createAndAppendElement(valueContainer, "i", iconClass);
        createAndAppendElement(valueContainer, "span", "", formatNumber(keyFigure.value, currency), { style: "margin: 20px" });
    }
    keyFiguresContainer.appendChild(fragment);
}
//# sourceMappingURL=keyFigures.js.map