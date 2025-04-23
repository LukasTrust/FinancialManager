class CheckboxDropdown {
    private readonly container: HTMLElement;
    private items: { id: string; value: string; name: string }[];
    private readonly multiSelect: boolean;
    private readonly dropdownToggle: HTMLElement;
    private readonly dropdownOptions: HTMLElement;
    private checkboxes: HTMLInputElement[] = [];
    private readonly defaultText: string;

    constructor({ parent, items, defaultText, clearText, multiSelect = true }: Omit<DropdownOptions, 'subText'>) {
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

    private createAndAppendElement(parent: HTMLElement, tagName: string, className: string, innerText: string = "", attributes: Record<string, string> = {}): HTMLElement {
        const element = document.createElement(tagName);
        element.className = className;
        if (innerText) element.innerText = innerText;
        for (const [key, value] of Object.entries(attributes)) {
            element.setAttribute(key, value);
        }
        parent.appendChild(element);
        return element;
    }

    private renderOptions(clearText: string) {
        // Clear existing options
        this.dropdownOptions.innerHTML = "";

        // Add Clear button
        const clearBtn = this.createAndAppendElement(
            this.dropdownOptions,
            "button",
            "iconButton red marginTop marginBottom",
            clearText,
            { type: "button" }
        );
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
            }) as HTMLInputElement;

            this.createAndAppendElement(option, "span", "", item.name);

            this.checkboxes.push(checkbox);

            option.addEventListener("click", (e) => {
                const target = e.target as HTMLElement;
                if (target !== checkbox) checkbox.checked = !checkbox.checked;

                if (!this.multiSelect) {
                    this.checkboxes.forEach(cb => {
                        if (cb !== checkbox) cb.checked = false;
                    });
                }

                this.updateDisplay();
            });

            checkbox.addEventListener("change", () => this.updateDisplay());
        });
    }

    private setupEvents() {
        this.dropdownToggle.addEventListener("click", () => {
            const isVisible = this.dropdownOptions.style.display === "block";
            this.dropdownOptions.style.display = isVisible ? "none" : "block";
        });

        window.addEventListener("click", (e) => {
            if (!this.container.contains(e.target as Node)) {
                this.dropdownOptions.style.display = "none";
            }
        });
    }

    private updateDisplay() {
        const selected = this.getSelectedItems();

        this.dropdownToggle.innerHTML = "";

        if (selected.length > 0) {
            selected.forEach(item => {
                createListElement(
                    this.dropdownToggle,
                    item.name,
                    {},
                    true,
                    true,
                    null,
                    () => {
                        const checkbox = this.checkboxes.find(cb => cb.value === item.value);
                        if (checkbox) {
                            checkbox.checked = false;
                            this.updateDisplay();
                        }
                    },
                    true
                );
            });
        } else {
            createListElement(
                this.dropdownToggle,
                this.defaultText,
                {style: "padding: 5px"},
                false,
                false,
                null,
                null,
                true
            );
        }
    }

    public getSelectedItems(): { id: string; value: string; name: string }[] {
        return this.checkboxes
            .filter(cb => cb.checked)
            .map(cb => this.items.find(item => String(item.id) === cb.id))
            .filter((item): item is { id: string; value: string; name: string } => item !== undefined);
    }

    public clearSelection(): void {
        this.checkboxes.forEach(cb => cb.checked = false);
        this.updateDisplay();
    }
}
