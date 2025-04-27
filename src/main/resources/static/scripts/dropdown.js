class CheckboxDropdown {
    constructor({ parent, items, defaultText, clearText, multiSelect = true }) {
        this.checkboxes = [];
        this.items = items;
        this.multiSelect = multiSelect;
        this.defaultText = defaultText;
        this.container = this.createAndAppendElement(parent, "div", "dropdown");
        this.dropdownToggle = this.createAndAppendElement(this.container, "div", "dropdownToggle");
        this.dropdownOptions = this.createAndAppendElement(this.container, "div", "dropdownOptions");
        this.updateDisplay();
        this.renderOptions(clearText);
        this.setupEvents();
    }
    createAndAppendElement(parent, tagName, className, innerText = "", attributes = {}) {
        const element = document.createElement(tagName);
        element.className = className;
        if (innerText)
            element.innerText = innerText;
        for (const [key, value] of Object.entries(attributes)) {
            element.setAttribute(key, value);
        }
        parent.appendChild(element);
        return element;
    }
    renderOptions(clearText) {
        // Clear existing options
        this.dropdownOptions.innerHTML = "";
        // Add Clear button
        const clearBtn = this.createAndAppendElement(this.dropdownOptions, "button", "iconButton red marginTop marginBottom", clearText, { type: "button" });
        clearBtn.addEventListener("click", (e) => {
            this.clearSelection();
        });
        // Render item checkboxes
        this.items.forEach(item => {
            const option = this.createAndAppendElement(this.dropdownOptions, "div", "dropdownOption");
            const checkbox = this.createAndAppendElement(option, "input", "tableCheckbox marginLeftBig marginRightBig", "", {
                type: "checkbox",
                id: item.id,
                value: item.value
            });
            this.createAndAppendElement(option, "span", "", item.name);
            this.checkboxes.push(checkbox);
            option.addEventListener("click", (e) => {
                const target = e.target;
                if (target !== checkbox)
                    checkbox.checked = !checkbox.checked;
                if (!this.multiSelect) {
                    this.checkboxes.forEach(cb => {
                        if (cb !== checkbox)
                            cb.checked = false;
                    });
                }
                this.updateDisplay();
            });
            checkbox.addEventListener("change", () => this.updateDisplay());
        });
    }
    setupEvents() {
        this.dropdownToggle.addEventListener("click", () => {
            const isVisible = this.dropdownOptions.style.display === "block";
            this.dropdownOptions.style.display = isVisible ? "none" : "block";
        });
        window.addEventListener("click", (e) => {
            if (!this.container.contains(e.target)) {
                this.dropdownOptions.style.display = "none";
            }
        });
    }
    updateDisplay() {
        const selected = this.getSelectedItems();
        this.dropdownToggle.innerHTML = "";
        if (selected.length > 0) {
            selected.forEach(item => {
                createListElement(this.dropdownToggle, item.name, {}, true, true, null, () => {
                    const checkbox = this.checkboxes.find(cb => cb.value === item.value);
                    if (checkbox) {
                        checkbox.checked = false;
                        this.updateDisplay();
                    }
                }, true);
            });
        }
        else {
            createListElement(this.dropdownToggle, this.defaultText, { style: "padding: 5px" }, false, false, null, null, true);
        }
    }
    getSelectedItems() {
        return this.checkboxes
            .filter(cb => cb.checked)
            .map(cb => this.items.find(item => String(item.id) === cb.id))
            .filter((item) => item !== undefined);
    }
    clearSelection() {
        this.checkboxes.forEach(cb => cb.checked = false);
        this.updateDisplay();
    }
}
//# sourceMappingURL=dropdown.js.map