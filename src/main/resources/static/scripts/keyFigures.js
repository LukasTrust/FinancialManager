function createKeyFigures(keyFigures) {
    const keyFiguresContainer = document.getElementById("keyFiguresContainer");
    keyFiguresContainer.innerHTML = '';

    if (!keyFiguresContainer) return;

    const fragment = document.createDocumentFragment(); // Batch DOM updates

    for (const keyFigure of keyFigures) {
        if (!keyFigure) continue; // Skip invalid entries

        // Create Key Figure Box
        const keyFigureBox = createElement("div", "boxContainer");

        // Create Header
        const keyFigureHeader = createElement("div", "keyFigureHeader");
        const name = createElement("span", "", keyFigure.name);

        // Tooltip
        const keyFigureTooltip = createElement("div", "tooltip bi bi-info");
        const keyFigureTooltipText = createElement("span", "tooltipText", keyFigure.tooltip);
        keyFigureTooltip.appendChild(keyFigureTooltipText);

        keyFigureHeader.append(name, keyFigureTooltip);
        keyFigureBox.appendChild(keyFigureHeader);

        // Value Container
        const classForValue = keyFigure.value >= 0 ? "positive" : "negative";
        const valueContainer = createElement("div", classForValue);
        valueContainer.style.padding = "10px";

        const iconClass = keyFigure.value >= 0 ? "bi bi-arrow-up" : "bi bi-arrow-down";
        const icon = createElement("i", iconClass);

        const keyFigureText = createElement("span", "");
        keyFigureText.style.margin = "20px";
        keyFigureText.innerHTML = keyFigure.value + " €"

        valueContainer.appendChild(icon);
        valueContainer.appendChild(keyFigureText);

        keyFigureBox.appendChild(valueContainer);

        // Append the Key Figure Box to Fragment
        fragment.appendChild(keyFigureBox);
    }

    keyFiguresContainer.appendChild(fragment);
}