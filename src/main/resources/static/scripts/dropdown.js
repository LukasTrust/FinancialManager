class CheckboxDropdown {
    constructor({ id, parent, items, preSelectedItems, defaultText, clearText, multiSelect = true, onCheck, onUncheck }) {
        this.checkboxes = [];
        this.id = id;
        this.items = items;
        this.multiSelect = multiSelect;
        this.defaultText = defaultText;
        this.clearText = clearText;
        this.onCheck = onCheck;
        this.onUncheck = onUncheck;
        this.container = this.createAndAppendElement(parent, "div", "dropdown");
        this.dropdownToggle = this.createAndAppendElement(this.container, "div", "dropdownToggle");
        this.dropdownOptions = this.createAndAppendElement(this.container, "div", "dropdownOptions");
        this.renderOptions(preSelectedItems);
        this.updateDisplay();
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
    renderOptions(preSelectedItems) {
        this.dropdownOptions.innerHTML = "";
        const clearBtn = this.createAndAppendElement(this.dropdownOptions, "button", "iconButton red marginTop marginBottom", this.clearText, { type: "button" });
        clearBtn.addEventListener("click", () => {
            this.clearSelection();
        });
        this.items.forEach(item => {
            const option = this.createAndAppendElement(this.dropdownOptions, "div", "dropdownOption");
            const checkbox = this.createAndAppendElement(option, "input", "tableCheckbox marginLeftBig marginRightBig", "", {
                type: "checkbox",
                id: item.id,
                value: item.value
            });
            if (preSelectedItems.find(preSelectedItem => preSelectedItem.id === item.id)) {
                checkbox.checked = true;
            }
            this.createAndAppendElement(option, "span", "", item.name);
            this.checkboxes.push(checkbox);
            option.addEventListener("click", (e) => {
                const target = e.target;
                if (target !== checkbox) {
                    checkbox.checked = !checkbox.checked;
                    checkbox.dispatchEvent(new Event("change"));
                }
                if (!this.multiSelect) {
                    this.checkboxes.forEach(cb => {
                        if (cb !== checkbox)
                            cb.checked = false;
                    });
                }
                this.updateDisplay();
            });
            checkbox.addEventListener("change", () => {
                var _a, _b;
                console.log("check");
                if (checkbox.checked) {
                    (_a = this.onCheck) === null || _a === void 0 ? void 0 : _a.call(this, item);
                }
                else {
                    (_b = this.onUncheck) === null || _b === void 0 ? void 0 : _b.call(this, item);
                }
                this.updateDisplay();
            });
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
                createListElement(this.dropdownToggle, item.name, { id: item.id }, true, true, null, () => {
                    const checkbox = this.checkboxes.find(cb => cb.id.toString() === item.id.toString());
                    if (checkbox) {
                        checkbox.checked = false;
                        checkbox.dispatchEvent(new Event("change"));
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