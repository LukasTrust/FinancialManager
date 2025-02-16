function createDialogHeader(parent, text, icon) {
    const header = createAndAppendElement(parent, "h1", "flexContainer");
    createAndAppendElement(header, "i", icon);
    createAndAppendElement(header, "span", "", text);
    return header;
}

function createListSection(parent, title, transactions) {
    const container = createAndAppendElement(parent, "div", "flexContainerColumn","",
        {style: "width: 45%"});
    const header = createAndAppendElement(container, "div", "listContainerHeader");
    createAndAppendElement(header, "h2", "", title,
        {style: "margin: 10px"});
    createListContainer(header, transactions);
    return container;
}

function createListContainer(parent, transactions) {
    const listContainer = createAndAppendElement(parent, "div", "listContainerColumn",
        "",{style: "min-height: 420px; max-height: 420px;"});
    transactions.forEach(transaction => {
        createListElement(listContainer, transaction.counterParty.name, { id: transaction.id });
    });
    return listContainer;
}

function createDialogButton(parent, iconClass, text, alignment, callback) {
    const button = createAndAppendElement(parent, "button", "iconButton", "", {
        style: "margin-top: 20px;" + (alignment === "left" ? " margin-right: auto" : " margin-left: auto")
    });
    createAndAppendElement(button, "i", iconClass, "", {style: "margin-right: 10px"});
    createAndAppendElement(button, "span", "", text);
    button.addEventListener("click", callback);
    return button;
}